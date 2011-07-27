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

package org.minig.backend.settings.impl;

import org.minig.backend.settings.ISettingsProvider;
import org.minig.backend.settings.ISettingsProviderFactory;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.IAccount;

public class AccountSettingsFactory implements ISettingsProviderFactory {

	private IniProvider provider;

	public AccountSettingsFactory() {
		this.provider = new IniProvider(new AccountConfiguration());
	}

	@Override
	public ISettingsProvider getProvider(IAccount ac) {
		return provider;
	}

}
