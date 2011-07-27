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

package fr.aliasource.webmail.common.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.minig.imap.Address;

import fr.aliasource.webmail.common.AccountFactory;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.conversation.MailMessage;

/**
 * Setup the test environnement TODO : use greenmail here !!!!
 * 
 * @author tom
 * 
 */
public abstract class WebmailTestCase extends TestCase {

	protected IAccount account;
	protected Random random;
	protected String MY_MAIL;
	
	private String confValue(String key) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"data/test.properties");
		Properties props = new Properties();
		if (is != null) {
			try {
				props.load(is);
				return props.getProperty(key);
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	protected String getMyMail() {
		return MY_MAIL;
	}

	protected MailMessage getDummyMessage(String[] to) {
		MailMessage m = new MailMessage("€uro subject " + random.nextInt(999)
				+ " bébé plutot long pour tester si le folding des sujets fonctionne", new MailBody("text/plain", "random body "
				+ random.nextInt(999) + "\naccents: éé €€ èè"), new HashMap<String,String>(),
				new Date(), null, null, null, null, null, null);
		Address[] recip = new Address[to.length];
		for (int i = 0; i < recip.length; i++) {
			recip[i] = new Address(to[i]);
		}
		m.setSender(new Address(MY_MAIL));
		return m;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		random = new Random(System.currentTimeMillis());
		AccountFactory factory = new AccountFactory(confValue("smtp"), confValue("imap"), confValue("imap"));
		account = factory.getAccount(confValue("login"), confValue("password"),"");
		MY_MAIL = confValue("mail");
	}

	@Override
	protected void tearDown() throws Exception {
		account.close();
		random = null;
		super.tearDown();
	}

}
