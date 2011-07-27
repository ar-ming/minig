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

import fr.aliasource.utils.IniFile;

/**
 * This class is responsible of loading the minig backend config file.
 * 
 * @author tom
 * 
 */
public class ProxyConfiguration extends IniFile {

	public static final String BACKEND_CONFIG = "/etc/minig/backend_conf.ini";
	
	public static final String IMAP_URI = "backend.imap.uri";
	public static final String SIEVE_URI = "backend.sieve.uri";
	public static final String SMTP_URI = "backend.smtp.uri";
	public static final String SINGLE_DOMAIN = "backend.imap.singleDomain";
	public static final String DEFAULT_DOMAIN = "backend.defaultDomain";
	public static final String DOMAIN_IN_IMAP_LOGIN = "backend.imap.loginWithDomain";

	public ProxyConfiguration() {
		super(BACKEND_CONFIG);
	}

	public String getSetting(String settingName) {
		return super.getSetting(settingName);
	}

	@Override
	public String getCategory() {
		return "backend";
	}

}
