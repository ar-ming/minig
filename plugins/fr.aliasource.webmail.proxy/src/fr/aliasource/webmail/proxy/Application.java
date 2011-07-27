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

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class Application implements IApplication {

	private Log logger = LogFactory.getLog(getClass());

	public Object start(IApplicationContext context) throws Exception {
		logger.info("MiniG backend starting...");
		Hashtable<String, Object> settings = new Hashtable<String, Object>();
		settings.put(JettyConstants.HTTP_PORT, 8081);
		settings.put(JettyConstants.CONTEXT_PATH, "");
		settings.put(JettyConstants.CUSTOMIZER_CLASS,
				"org.minig.jetty.JettyPimper");

		File sslP12 = new File("/etc/minig/minig.p12");
		if (sslP12.exists()) {
			settings.put(JettyConstants.HTTPS_ENABLED, true);
			settings.put(JettyConstants.HTTPS_PORT, 8083);
			settings.put(JettyConstants.SSL_KEYSTORETYPE, "PKCS12");
			settings.put(JettyConstants.SSL_KEYSTORE, sslP12.getAbsolutePath());
			settings.put(JettyConstants.SSL_PASSWORD, "password");
			settings.put(JettyConstants.SSL_KEYPASSWORD, "password");
			settings.put(JettyConstants.SSL_PROTOCOL, "SSL");
			settings.put(JettyConstants.SSL_WANTCLIENTAUTH, false);
			settings.put(JettyConstants.SSL_NEEDCLIENTAUTH, false);
		} else {
			logger
					.info("/etc/minig/minig.p12 not found, backend ssl support not activated");
		}

		System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", ""
				+ (20 * 1024 * 1024));

		JettyConfigurator.startServer("minig_backend", settings);

		loadBundle("org.eclipse.equinox.http.registry");

		return EXIT_OK;
	}

	private void loadBundle(String bundleName) throws BundleException {
		Bundle bundle = Platform.getBundle(bundleName);
		if (bundle != null) {
			if (bundle.getState() == Bundle.RESOLVED) {
				bundle.start(Bundle.START_TRANSIENT);
			}
		}
	}

	public void stop() {
		logger.info("MiniG backend stopped.");
	}

}
