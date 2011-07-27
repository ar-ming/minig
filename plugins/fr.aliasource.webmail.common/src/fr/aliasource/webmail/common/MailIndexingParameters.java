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

import fr.aliasource.index.core.IIndexingParameters;
import fr.aliasource.utils.IniFile;
import fr.aliasource.webmail.common.IAccount;

public class MailIndexingParameters extends IniFile implements IIndexingParameters {

	public static final String INDEXING_CONF = "/etc/minig/indexing_conf.ini";
	private IAccount account;
	
	public MailIndexingParameters(IAccount account) {
		super(INDEXING_CONF);
		this.account = account;
	}
	
	@Override
	public String getPropertyValue(String property) {
		return getSetting(property);
	}

	public IAccount getAccount() {
		return account;
	}

	@Override
	public String getCategory() {
		return "indexing";
	}

}
