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

import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.conversation.ListConversationsCommand;
import fr.aliasource.webmail.common.folders.IMAPFolder;

public class ListConversationsCommandTests extends WebmailTestCase {

	public void testList() {
		try {
			ListConversationsCommand lfc = new ListConversationsCommand(account, new IMAPFolder("INBOX"));

			ConversationReferenceList l = lfc.getData();
			assertTrue(l.getFullLength() > 0);
			System.out.println("conversation count: "+l.getFullLength());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testListJunk() {
		try {
			ListConversationsCommand lfc = new ListConversationsCommand(account, new IMAPFolder("Junk"));

			ConversationReferenceList l = lfc.getData();
			assertTrue(l.getFullLength() > 0);
			System.out.println("conversation count: "+l.getFullLength());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testListArchives() {
		try {
			ListConversationsCommand lfc = new ListConversationsCommand(account, new IMAPFolder("archives"));

			ConversationReferenceList l = lfc.getData();
			assertTrue(l.getFullLength() > 0);
			System.out.println("conversation count: "+l.getFullLength());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}


}
