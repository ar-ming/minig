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

import java.util.List;

import fr.aliasource.webmail.common.cache.SubscribedFolderCache;
import fr.aliasource.webmail.common.folders.IFolder;

public class SubscribedFolderCacheTests extends WebmailTestCase {

	public void testUpdate() {
		SubscribedFolderCache fc = new SubscribedFolderCache(account);
		try {
			fc.update();
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testListSubscribed() {
		SubscribedFolderCache fc = new SubscribedFolderCache(account);
		try {
			List<IFolder> l = fc.getData();
			assertTrue(l.size() > 0);
			for (IFolder f : l) {
				System.out.println("sub: " + f.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

}
