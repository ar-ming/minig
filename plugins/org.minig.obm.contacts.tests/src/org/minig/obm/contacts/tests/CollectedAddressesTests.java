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

package org.minig.obm.contacts.tests;

import java.util.List;

import junit.framework.TestCase;
import fr.aliasource.webmail.book.BookActivator;
import fr.aliasource.webmail.book.BookManager;
import fr.aliasource.webmail.book.ContactGroup;
import fr.aliasource.webmail.book.MinigContact;

public class CollectedAddressesTests extends TestCase {

	private static final String UID = "thomas@zz.com";
	private static final String GID = "obm_contact";

	private ContactGroup getGroup(BookManager mgr, String gid, String uid) {
		List<ContactGroup> groups = mgr.getGroups(uid, "aliacom");
		ContactGroup collected = null;
		for (ContactGroup cg : groups) {
			System.err.println("contact group id: " + cg.getId());
			if (gid.equals(cg.getId())) {
				collected = cg;
			}
			System.out.println("cg: " + cg.getId() + " name: "
					+ cg.getDisplayName());
		}
		return collected;
	}

	public void testCollectedBookConstructor() {
		BookManager mgr = BookActivator.getDefault().getBookManager();
		assertNotNull(mgr);
		ContactGroup collected = getGroup(mgr, GID, UID);
		assertNotNull(collected);
	}

	public void testCountAndList() {
		BookManager mgr = BookActivator.getDefault().getBookManager();
		ContactGroup collected = getGroup(mgr, GID, UID);
		int count = collected.getCount();
		List<MinigContact> all = mgr.findAll(UID, "aliacom", collected.getId());
		System.out.println("count: " + count + " all: " + all.size());
		assertEquals(count, all.size());
	}

}
