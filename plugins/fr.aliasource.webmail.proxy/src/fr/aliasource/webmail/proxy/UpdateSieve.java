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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.SieveClient;

import fr.aliasource.webmail.common.LocatorRegistry;


public class UpdateSieve extends HttpServlet {

	private static final long serialVersionUID = -8820464821850365012L;
	private static final Log logger = LogFactory.getLog(UpdateSieve.class);

	private String sieveLocatorUri;
	private LocatorRegistry registry;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String login = req.getParameter("login");
		String password = req.getParameter("password");
		String scriptName = req.getParameter("scriptName");
		String content = req.getParameter("content");

		logger.info("starting sieve script update for user " + login);

		String[] loginSplitter = login.split("@");
		String l = loginSplitter[0];
		String d = null;
		if (loginSplitter.length == 2) {
			d = loginSplitter[1];
		}

		String host = registry.getHostName(l, d, sieveLocatorUri);
		SieveClient sc = new SieveClient(host, 2000, login, password);
		sc.login();
		sc.putscript(scriptName, new ByteArrayInputStream(content.getBytes()));
		sc.logout();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("doGet on updateSieve servlet.");
		resp.setContentType("text/plain");
		resp
				.getWriter()
				.println(
						"UpdateSieve servlet should be called with a POST HTTP request.");
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
