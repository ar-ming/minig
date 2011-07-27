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

import java.util.Arrays;
import java.util.List;

import fr.aliasource.utils.IniFile;

/**
 * This class is responsible of loading the minig account config file.
 * 
 * @author tom
 * 
 */
public class AccountConfiguration extends IniFile {

	public static final String ACCOUNT_CONFIG = "/etc/minig/account_conf.ini";

	public static final String SENT = "account.folders.sent";
	public static final String DRAFTS = "account.folders.drafts";
	public static final String TEMPLATES = "account.folders.templates";
	public static final String TRASH = "account.folders.trash";
	public static final String SPAM = "account.folders.spam";
	public static final List<String> DEFAULT_FOLDERS = Arrays.asList(SENT, DRAFTS, TEMPLATES, TRASH, SPAM);
	public static final String SKIPPED_FOLDERS = "account.folders.skipped";

	public AccountConfiguration() {
		super(ACCOUNT_CONFIG);
	}

	public String getSetting(String loginAtDomain, String settingName) {
		return getSetting(settingName);
	}

	@Override
	public String getCategory() {
		return "account_configuration";
	}

}
