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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.AccountFactory;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LocatorRegistry;
import fr.aliasource.webmail.proxy.ProxyConfiguration;

/**
 * Starts the webmail server processes for a user
 * 
 * @author tom
 * 
 */
public class LoginImpl {

	private IAccount account;
	private String imapUri;
	private String smtpUri;
	private LocatorRegistry locator;
	private boolean singleDomain;
	private Log logger;
	private String defaultDomain;
	private String sieveUri;
	private boolean domainInImapLogin;

	public LoginImpl(ProxyConfiguration conf, LocatorRegistry locator) {
		logger = LogFactory.getLog(getClass());
		this.imapUri = conf.getSetting(ProxyConfiguration.IMAP_URI);
		this.smtpUri = conf.getSetting(ProxyConfiguration.SMTP_URI);
		this.sieveUri = conf.getSetting(ProxyConfiguration.SIEVE_URI);
		this.singleDomain = "true".equals(conf
				.getSetting(ProxyConfiguration.SINGLE_DOMAIN));
		this.defaultDomain = conf.getSetting(ProxyConfiguration.DEFAULT_DOMAIN);
		this.domainInImapLogin = !"false".equals(conf
				.getSetting(ProxyConfiguration.DOMAIN_IN_IMAP_LOGIN));
		logger.info("imap: " + imapUri + " smtp: " + smtpUri
				+ " defaultDomain: " + defaultDomain);
		this.locator = locator;
	}

	public boolean doLogin(String login, String domain, String password) {
		if (account == null) {
			String completeLogin = login;
			if (!singleDomain) {
				completeLogin += "@" + domain;
			}
			String imap = locator.getHostName(login, domain, imapUri);
			String smtp = locator.getHostName(login, domain, smtpUri);
			String sieve = locator.getHostName(login, domain, sieveUri);
			try {
				AccountFactory ac = new AccountFactory(smtp, imap, sieve);
				if (singleDomain) {
					account = ac.getAccount(completeLogin, password,
							defaultDomain);
				} else if (domainInImapLogin) {
					account = ac.getAccount(completeLogin, password, domain);
				} else {
					account = ac.getAccount(login, password, domain);
				}

				account.getCache().start();
				return true;
			} catch (Exception e) {
				logger.error("Login refused for '" + completeLogin
						+ "' with password '" + password + "' on " + imap
						+ " (" + e.getMessage() + ")", e);
			}
		} else {
			logger.info("Already logged in");
			return true;
		}
		return false;
	}

	public IAccount getAccount() {
		return account;
	}

}
