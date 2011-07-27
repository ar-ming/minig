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

package fr.aliasource.webmail.common;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.RunnableExtensionLoader;
import fr.aliasource.webmail.common.impl.IMAPLocator;
import fr.aliasource.webmail.common.impl.SMTPLocator;

public class LocatorRegistry {

	private List<IServerLocator> locators;
	private Log logger;

	LocatorRegistry() {
		logger = LogFactory.getLog(getClass());
		locators = new LinkedList<IServerLocator>();
		registerLocators();
	}

	private void registerLocators() {
		// std locators
		locators.add(new IMAPLocator());
		locators.add(new SMTPLocator());

		// plugin locators
		RunnableExtensionLoader<IServerLocator> rel = new RunnableExtensionLoader<IServerLocator>();
		List<IServerLocator> isls = rel.loadExtensions(Activator.PLUGIN_ID,
				"serverlocator", "server_locator", "implementation");
		for (IServerLocator isl : isls) {
			locators.add(isl);
		}
	}

	public String getHostName(String login, String domain, String locatorUri) {
		int schemeIdx = locatorUri.indexOf("://");
		String scheme = locatorUri.substring(0, schemeIdx);
		String urlPart = locatorUri.substring(schemeIdx + "://".length());

		for (IServerLocator locator : locators) {
			if (locator.supportsUriScheme(scheme)) {
				String ret = locator.getHostName(login, domain, urlPart);
				logger.info("Locator resolved " + locatorUri + " for " + login
						+ "@" + domain + " as " + ret);
				return ret;
			}
		}
		logger.error("Server URI with unsupported scheme : " + locatorUri);
		return null;

	}
}
