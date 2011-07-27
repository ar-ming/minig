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

import org.minig.imap.idle.IIdleCallback;
import org.minig.imap.idle.IdleLine;

public class IdleClientLoginTests extends IMAPTestCase {

	public class DummyCallback implements IIdleCallback {

		@Override
		public void receive(IdleLine line) {
			System.err.println("line: " + line);
		}

		public void disconnectedCallBack() {
			System.err.println("disconnected");
		}

	}

	public void testConstructor() {
		create();
	}

	private IdleClient create() {
		return new IdleClient(confValue("imap"), 143, confValue("login"),
				confValue("password"));
	}

	public void testLoginLogout() {
		IdleClient sc = create();
		try {
			boolean ok = sc.login(true);
			assertTrue(ok);
			sc.select("INBOX");
			sc.startIdle(new DummyCallback());
			sc.stopIdle();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("error on login");
		} finally {
			try {
				sc.logout();
			} catch (Throwable e) {
				e.printStackTrace();
				fail("error on logout");
			}
		}
	}

	public void testLoginLogoutSpeed() throws IMAPException {
		IdleClient sc = create();
		int COUNT = 1000;
		long time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			boolean ok = sc.login(true);
			assertTrue(ok);
			sc.select("INBOX");
			sc.startIdle(new DummyCallback());
			sc.stopIdle();
			sc.logout();
		}

		time = System.currentTimeMillis() - time;
		System.out.println(COUNT + " iterations in " + time + "ms. "
				+ (time / COUNT) + "ms avg, " + 1000 / (time / COUNT)
				+ " per sec.");
	}

}
