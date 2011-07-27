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

import java.nio.ByteBuffer;

import org.minig.imap.impl.Base64;

public class SieveLoginTests extends SieveTestCase {

	public void testConstructor() {
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);
	}

	public void testB64Decode() {
		String value = "dGhvbWFzQHp6LmNvbQB0aG9tYXNAenouY29tAGFsaWFjb20=";
		ByteBuffer decoded = Base64.decode(value);
		byte[] data = decoded.array();
		System.out.println("Decoded: " + new String(data) + " len: "
				+ data.length);
		for (byte b : data) {
			System.out.println("byte: 0x" + Integer.toHexString(b)
					+ (b > 0 ? " string: " + new String(new byte[] { b }) : ""));
		}
		
		value = "dGhvbWFzAHRob21hcwBhbGlhY29t";
		decoded = Base64.decode(value);
		data = decoded.array();
		System.out.println("Decoded: " + new String(data) + " len: "
				+ data.length);
		for (byte b : data) {
			System.out.println("byte: 0x" + Integer.toHexString(b)
					+ (b > 0 ? " string: " + new String(new byte[] { b }) : ""));
		}
	
	}

	public void testLoginLogout() {
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);

		try {
			boolean ret = sc.login();
			assertTrue(ret);
			sc.logout();
		} catch (Throwable t) {
			t.printStackTrace();
			fail("should not get an exception");
		}
	}

	public void testUnauthenticate() {
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);

		try {
			boolean ret = sc.login();
			assertTrue(ret);
			sc.unauthenticate();
			sc.logout();
		} catch (Throwable t) {
			t.printStackTrace();
			fail("should not get an exception");
		}
	}

	public void testLoginLogoutPerf() {
		final int IT_COUNT = 10000;
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);

		for (int i = 0; i < 1000; i++) {
			sc.login();
			sc.logout();
		}

		long time = System.currentTimeMillis();
		for (int i = 0; i < IT_COUNT; i++) {
			sc.login();
			sc.logout();
		}
		time = System.currentTimeMillis() - time;
		System.out.println(IT_COUNT + " sieve connections done in "
				+ ((time + 0.1) / 1000.0) + " seconds.");
	}

}
