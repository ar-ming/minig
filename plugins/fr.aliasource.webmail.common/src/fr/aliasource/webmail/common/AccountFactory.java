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

import java.io.IOException;

import fr.aliasource.webmail.common.imap.StoreException;


/**
 * Entry point to the webmail API
 * 
 * @author tom
 *
 */
public class AccountFactory {

	private String smtpHost;
	private String imapHost;
	private String sieveHost;
	
	private static AccountConfiguration accountConf;
	
	static {
		accountConf = new AccountConfiguration();
	}

	public AccountFactory(String smtpHost, String imapHost, String sieveHost) {
		this.smtpHost = smtpHost;
		this.imapHost = imapHost;
		this.sieveHost = sieveHost;
	}

	public IAccount getAccount(String userName, String password, String domain)
			throws IOException, StoreException, InterruptedException {
		return new IMAPAccount(imapHost, smtpHost, userName, password, domain, accountConf, sieveHost);
	}

}
