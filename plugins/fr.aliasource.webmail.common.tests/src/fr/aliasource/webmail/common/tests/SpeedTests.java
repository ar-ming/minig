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
import java.util.Properties;

import junit.framework.TestCase;
import fr.aliasource.webmail.common.AccountFactory;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * Setup the test environnement
 * 
 * @author tom
 * 
 */
public class SpeedTests extends TestCase {

	private AccountFactory factory;

	private String confValue(String key) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"data/test.properties");
		Properties props = new Properties();
		if (is != null) {
			try {
				props.load(is);
				is.close();
				return props.getProperty(key);
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public void testLoginLogout() throws IOException, StoreException,
			InterruptedException {
		IAccount account = factory.getAccount(confValue("login"),
				confValue("password"), "");
		account.close();
	}

	public void testLoginLogoutSpeed() throws IOException, StoreException,
			InterruptedException {

		String l = confValue("login");
		String p = confValue("password");
		IAccount account = factory.getAccount(l, p, "");
		account.close();

		long time = System.currentTimeMillis();
		for (int i = 0; i < 25; i++) {
			account = factory.getAccount(l, p, "");
			account.close();
		}
		time = System.currentTimeMillis() - time;
		System.err.println("Time is " + time + "ms.");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new AccountFactory(confValue("smtp"), confValue("imap"),
				confValue("imap"));
	}

	@Override
	protected void tearDown() throws Exception {
		factory = null;
		super.tearDown();
	}

}
