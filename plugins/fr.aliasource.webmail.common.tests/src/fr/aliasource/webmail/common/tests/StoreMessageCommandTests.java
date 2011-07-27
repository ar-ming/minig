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

import fr.aliasource.webmail.common.IFindReference;
import fr.aliasource.webmail.common.IStoreMessage;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.message.SendParameters;
import fr.aliasource.webmail.common.message.StoreMessageCommand;

public class StoreMessageCommandTests extends WebmailTestCase {

	public void testStoreMessage() {
		try {
			IStoreMessage cmd = new StoreMessageCommand(account);

			IFolder dest = new IMAPFolder("INBOX");
			MailMessage toSend = getDummyMessage(new String[] { getMyMail() });
			String id = cmd.store(dest, toSend, new SendParameters());
			assertNotNull(id);

			IFindReference fr = account.getFindReference();
			ConversationReference ref = fr.find(id);
			assertNotNull(ref);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
}
