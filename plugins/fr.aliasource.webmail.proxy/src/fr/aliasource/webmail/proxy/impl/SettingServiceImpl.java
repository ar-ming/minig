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

package fr.aliasource.webmail.proxy.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.settings.Signature;
import fr.aliasource.webmail.proxy.api.ISettingService;

/**
 * 
 * @author adrien
 *
 */
public class SettingServiceImpl implements ISettingService {

	private IAccount account;

	@SuppressWarnings("unused")
	private static  final Log logger = LogFactory.getLog(SettingServiceImpl.class);
	

	protected IAccount getAccount() {
		return account;
	}
	
	public SettingServiceImpl(IAccount ac) {
		this.account = ac;
	}

	@Override
	public void saveSignature(List<Signature> signatures) throws Exception {
		getAccount().getCache().getSignatureCache().writeToCache(signatures);
	}
}
