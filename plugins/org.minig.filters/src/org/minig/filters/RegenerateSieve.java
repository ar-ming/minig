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

package org.minig.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.cache.IDatastore;
import org.minig.cache.JDBCDataStore;
import org.minig.imap.SieveClient;
import org.minig.obmsync.service.MinigForward;
import org.minig.obmsync.service.MinigVacation;
import org.minig.obmsync.service.SettingService;

import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.webmail.common.FilterStore;
import fr.aliasource.webmail.common.LocatorRegistry;
import fr.aliasource.webmail.proxy.ProxyActivator;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.ProxyFactory;

/**
 * Forces a sieve script regeneration Does not trigger mail indexing process
 * 
 * @author tom
 * 
 */
public class RegenerateSieve extends HttpServlet {

	private static final long serialVersionUID = -1991781018244258566L;
	private static final Log logger = LogFactory.getLog(RegenerateSieve.class);

	private String sieveLocatorUri;
	private LocatorRegistry registry;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		long time = System.currentTimeMillis();
		String login = req.getParameter("login");
		String password = req.getParameter("password");

		logger.info("[" + login + "] replacing active sieve script");

		String[] loginSplitter = login.split("@");
		String l = loginSplitter[0];
		String d = null;
		if (loginSplitter.length == 2) {
			d = loginSplitter[1];
		}

		String host = registry.getHostName(l, d, sieveLocatorUri);

		// vacation & forward are needed for script regen
		SettingService ss = null;
		MinigForward mf = null;
		MinigVacation mv = null;
		try {
			ss = new SettingService(login, password);
			mf = ss.getEmailForwarding();
			mv = ss.getVacationSettings();
		} catch (Exception e) {
			logger.error("error loading vac/fwd for sieve regen", e);
		}

		SieveClient sc = new SieveClient(host, 2000, login, password);
		FilterStore fs = new FilterStore(sc);
		IDatastore ds = new JDBCDataStore();
		int cacheId = ds.getCacheId(login);
		Connection con = null;
		try {
			con = ds.getConnection();
			String scriptContent = new SieveScriptBuilder().createScript(con,
					cacheId, mv, mf);
			fs.login();
			fs.replaceActiveScript(new ByteArrayInputStream(scriptContent
					.getBytes()));
			fs.logout();

			resp.setStatus(200);
			resp.setContentType("text/plain");
			PrintWriter w = resp.getWriter();
			w.println("OK");
			w.flush();
		} catch (Exception e) {
			resp.setStatus(500);
			resp.setContentType("text/plain");
			PrintWriter w = resp.getWriter();
			w
					.println("KO - look in /var/log/minig/minig-backend.log for detailed error report.");
			w.flush();
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, null, null);
		}
		time = System.currentTimeMillis() - time;
		logger
				.info("[" + login + "] active script replaced in " + time
						+ "ms.");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("doGet on RegenerateSieve servlet.");
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
