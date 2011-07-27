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
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.SieveClient;
import org.minig.imap.sieve.SieveScript;

import fr.aliasource.webmail.common.LocatorRegistry;


public class GetActiveSieve extends HttpServlet {

	private static final long serialVersionUID = -8820464821850365012L;
	private static final Log logger = LogFactory.getLog(GetActiveSieve.class);

	private String sieveLocatorUri;
	private LocatorRegistry registry;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		long time = System.currentTimeMillis();
		String login = req.getParameter("login");
		String password = req.getParameter("password");

		logger.info("fetching active sieve script for user " + login);

		String[] loginSplitter = login.split("@");
		String l = loginSplitter[0];
		String d = null;
		if (loginSplitter.length == 2) {
			d = loginSplitter[1];
		}

		String host = registry.getHostName(l, d, sieveLocatorUri);
		SieveClient sc = new SieveClient(host, 2000, login, password);
		sc.login();
		List<SieveScript> scripts = sc.listscripts();
		SieveScript activeScript = null;
		for (SieveScript ss : scripts) {
			if (ss.isActive()) {
				activeScript = ss;
				break;
			}
		}
		if (activeScript == null) {
			sc.logout();
			resp.setContentType("text/plain");
			PrintWriter writer = resp.getWriter();
			writer.println("NONE");
			writer.flush();
		} else {
			String content = sc.getScript(activeScript.getName());
			sc.logout();
			resp.setContentType("text/plain");
			PrintWriter writer = resp.getWriter();
			writer.println(activeScript.getName());
			writer.println(content);
			writer.flush();
		}
		time = System.currentTimeMillis() - time;
		logger.info("active script fetched in " + time + "ms.");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("doGet on getActiveSieve servlet.");
		doPost(req, resp);
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		ProxyFactory pf = ProxyActivator.getDefault().getProxyFactory();
		registry = pf.getLocatorRegistry();
		sieveLocatorUri = pf.getConfiguration().getSetting(
				ProxyConfiguration.SIEVE_URI);
	}

}
