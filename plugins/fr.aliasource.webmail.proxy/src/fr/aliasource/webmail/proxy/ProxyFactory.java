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

import fr.aliasource.webmail.common.Activator;
import fr.aliasource.webmail.common.LocatorRegistry;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.impl.CompletionRegistry;
import fr.aliasource.webmail.proxy.impl.ProxyImpl;

public class ProxyFactory {

	private ProxyConfiguration conf;
	private CompletionRegistry completionRegistry;
	private LocatorRegistry locatorRegistry;

	public ProxyFactory() {
		conf = new ProxyConfiguration();
		locatorRegistry = Activator.getDefault().getLocatorRegistry();
		completionRegistry = new CompletionRegistry(conf, locatorRegistry);
	}

	public IProxy newProxy() {
		return new ProxyImpl(conf, completionRegistry, locatorRegistry);
	}

	public ProxyConfiguration getConfiguration() {
		return conf;
	}

	public LocatorRegistry getLocatorRegistry() {
		return locatorRegistry;
	}

}
