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

package org.minig.imap;

public class LoginTests extends IMAPTestCase {

	public void testConstructor() {
		new StoreClient(confValue("imap"), 143, confValue("login"),
				confValue("password"));
	}

	public void testLoginLogout() {
		StoreClient sc = new StoreClient(confValue("imap"), 143,
				confValue("login"), confValue("password"));
		try {
			boolean ok = sc.login();
			assertTrue(ok);
		} catch (IMAPException e) {
			e.printStackTrace();
			fail("error on login");
		} finally {
			try {
				sc.logout();
			} catch (IMAPException e) {
				e.printStackTrace();
				fail("error on logout");
			}
		}
	}

	public void testLoginLogoutSpeed() throws IMAPException {
		StoreClient sc = new StoreClient(confValue("imap"), 143,
				confValue("login"), confValue("password"));
		int COUNT = 1000;
		long time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			boolean ok = sc.login();
			assertTrue(ok);
			sc.logout();
		}

		time = System.currentTimeMillis() - time;
		System.out.println(COUNT + " iterations in " + time + "ms. "
				+ (time / COUNT) + "ms avg, " + 1000 / (time / COUNT)
				+ " per sec.");
	}

}
