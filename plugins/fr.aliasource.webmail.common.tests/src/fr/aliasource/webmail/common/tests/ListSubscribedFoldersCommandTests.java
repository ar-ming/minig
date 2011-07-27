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

import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.ListSubscribedFoldersCommand;

public class ListSubscribedFoldersCommandTests extends WebmailTestCase {

	public void testListSubscribed() {
		try {
			ListSubscribedFoldersCommand lfc = new ListSubscribedFoldersCommand(account);

			List<IFolder> l = lfc.getData();
			for (IFolder i : l) {
				System.out.println("folder '" + i.getDisplayName() + "' ["
						+ i.getName() + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

}
