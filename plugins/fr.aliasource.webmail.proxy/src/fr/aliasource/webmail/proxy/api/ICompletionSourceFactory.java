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

package fr.aliasource.webmail.proxy.api;

import fr.aliasource.webmail.proxy.ProxyConfiguration;

/**
 * Completion plugins entry point. This interface is implemented by plugins
 * providings completion sources.
 * 
 * @author tom
 * 
 */
public interface ICompletionSourceFactory {

	/**
	 * First method called. This allows completions sources to put their
	 * settings in the proxy configuration.
	 * 
	 * @param pc
	 */
	void init(ProxyConfiguration pc);

	/**
	 * Returns true if this factory can provide a completion source for the
	 * given type
	 * 
	 * @param completionType
	 * @return
	 */
	boolean supports(String completionType);

	/**
	 * Returns a completion source suitable for the given completion type.
	 * 
	 * @param type
	 * @return
	 */
	ICompletionSource getInstance(String type);

	/**
	 * Last method called. allows cleanup (jdbc pool, ldap pool, whatever).
	 */
	void shutdown();

}
