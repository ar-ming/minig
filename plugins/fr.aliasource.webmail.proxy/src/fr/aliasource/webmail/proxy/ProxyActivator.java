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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


public class ProxyActivator extends Plugin {

	private Log logger = LogFactory.getLog(getClass());

	private static ProxyActivator defaultActivator;
	private ProxyFactory proxyFactory;

	// The plug-in ID
	public static final String PLUGIN_ID = "fr.aliasource.webmail.proxy";

	public ProxyActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		defaultActivator = this;
		proxyFactory = new ProxyFactory();
		logger.info("Proxy plugin activated.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		defaultActivator = null;
		proxyFactory = null;
		logger.info("Proxy plugin stopped.");
		super.stop(context);
	}

	public static ProxyActivator getDefault() {
		return defaultActivator;
	}

	public ProxyFactory getProxyFactory() {
		return proxyFactory;
	}

}
