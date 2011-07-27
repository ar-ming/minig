/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;
import fr.aliasource.webmail.proxy.impl.PSource;
import fr.aliasource.webmail.proxy.impl.ResponderImpl;
import fr.aliasource.webmail.proxy.impl.TimeOutMap;

/**
 * Webmail IMAP proxy entry point. This is a similar to struts action servlet,
 * but I don't want to introduce a multi-megabyte dependency on struts / spring
 * for such a simple thing.
 * 
 * @author tom
 * 
 */
public class Controller extends HttpServlet {

	private static final long serialVersionUID = 6588093631535528404L;

	private Map<String, IProxy> clients;

	private Log logger;

	private Map<String, IControlledAction> controlledActions;

	private ProxyFactory proxyFactory;

	private PushHandler pushHandler;

	public Controller() {
		logger = LogFactory.getLog(getClass());
		clients = new TimeOutMap<String, IProxy>(6 * 60 * 1000);
		controlledActions = new HashMap<String, IControlledAction>();
		logger.info("Controller created.");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String token = req.getParameter("token");
		IResponder responder = new ResponderImpl(resp);
		if (token != null && clients.containsKey(token)) {
			processConnectedRequest(clients.get(token), req, responder);
		} else {
			validateLoginRequest(req, responder);
		}
	}

	void registerControlledAction(IControlledAction ica) {
		ica.init(proxyFactory.getConfiguration());
		controlledActions.put(ica.getUriMapping(), ica);
	}

	/**
	 * Verifies login & creates imap caching proxy if valid.
	 * 
	 * @param req
	 * @param responder
	 */
	private void validateLoginRequest(HttpServletRequest req,
			IResponder responder) {
		if ("/login.do".equals(req.getRequestURI())) {
			loginHandling(req, responder, false);
		} else if ("/firstIndexing.do".equals(req.getRequestURI())) {
			IProxy p = loginHandling(req, responder, true);
			if (p != null) {
				try {
					Thread.sleep(20 * 1000);
				} catch (InterruptedException e) {
				}
				logout(p);
				responder.sendString("First indexing complete.");
			} else {
				logger.error("null proxy from loginHandling", new Throwable());
			}
			logger.info("leaving first indexing");
		} else {
			responder.denyAccess("not a login request");
		}
	}

	void logout(IProxy p) {
		logger
				.info("**************************** LOGOUT CODE called ***************************");
		int ref = p.releaseClientReference();
		if (ref <= 0) {
			clients.remove(p.getToken());
			p.stop();
		} else {
			logger.info("[" + p.getAccount().getUserId()
					+ "] not releasing backend-session, ref count: " + ref);
		}
	}

	private IProxy loginHandling(HttpServletRequest req, IResponder responder,
			boolean firstIndexing) {
		String l = req.getParameter("login");
		String d = req.getParameter("domain");
		String p = req.getParameter("password");
		IProxy proxy = null;

		if (l != null && p != null && l.length() > 0 && p.length() > 0) {
			String existingToken = getExistingProxy(l + "@" + d, p);
			if (existingToken != null) {
				logger.info("reusing existing proxy for " + l + "@" + d);
				if (!firstIndexing) {
					responder.sendToken(existingToken);
				}
			} else {
				proxy = proxyFactory.newProxy();
				if (proxy.doLogin(l, d, p)) {
					String token = generateToken(l, d, p);
					proxy.setToken(token);
					if (!firstIndexing) {
						clients.put(token, proxy);
						responder.sendToken(token);
					}
				} else {
					responder.denyAccess("IMAP login refused for '" + l + "@"
							+ d + "'");
				}
			}
		} else {
			responder.denyAccess("login request refused for '" + l + "@" + d
					+ "'");
		}
		return proxy;
	}

	private String getExistingProxy(String userId, String password) {
		for (String token : clients.keySet()) {
			IProxy p = clients.get(token);
			if (userId.equals(p.getAccount().getUserId())
					&& p.checkPassword(password)) {
				p.addClientReference();
				return token;
			}
		}
		return null;
	}

	private String generateToken(String l, String d, String p) {
		return l + "@" + d + "@"
				+ Math.round(System.currentTimeMillis() * Math.random());
	}

	private void processConnectedRequest(IProxy p, HttpServletRequest req,
			IResponder responder) {
		// prevent expiration
		clients.put(p.getToken(), p);
		
		String u = req.getRequestURI();
		if ("/push".equals(u)) {
			pushHandler.handlePush(p, req, responder);
			return;
		}
		
		IControlledAction ica = controlledActions.get(u);
		if (ica != null) {
			ica.execute(p, new PSource(req), responder);
		} else {
			responder.denyAccess("[" + p.getToken()
					+ "] no controlled action for uri " + req.getRequestURI());
		}
	}

	Map<String, IProxy> getClients() {
		return clients;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		proxyFactory = ProxyActivator.getDefault().getProxyFactory();
		pushHandler = new PushHandler();
		new ActionRegistry(this).registerActions();
	}

}
