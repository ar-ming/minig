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

package fr.aliasource.webmail.book;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BookManagerTests {

	@Test
	public void testActivator() {
		BookActivator ba = BookActivator.getDefault();
		assertNotNull(ba);
	}

	@Test
	public void testGet() {
		BookManager bm = BookActivator.getDefault().getBookManager();
		assertNotNull(bm);
	}

	@Test
	public void testGetGroup() {
		BookManager bm = BookActivator.getDefault().getBookManager();
		assertNotNull(bm);
		List<ContactGroup> l = bm.getGroups("thomas@zz.com", "aliacom");
		System.out.println("groups size: " + l.size());
		for (ContactGroup cg : l) {
			System.out.println("group: " + cg.getDisplayName());
		}

		assertTrue(l.size() > 0);
		assertTrue(l.size() == 3); // all + ldap + obm public
	}
	
	@Test
	public void testCount() {
		BookManager bm = BookActivator.getDefault().getBookManager();
		assertNotNull(bm);
		ContactGroup allGroup = bm.getGroups("thomas@zz.com", "aliacom").get(0);
		assertNotNull(allGroup);
		System.out.println("all group count: " + allGroup.getCount());
	}
}
