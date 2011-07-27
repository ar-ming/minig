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

import java.util.LinkedList;
import java.util.List;

import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IListConversations;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;

public class ConversationCacheTests extends WebmailTestCase {

	public void testList() {
		ConversationCache cc = new ConversationCache(account);
		try {
			ConversationReferenceList cr = cc.list(new IMAPFolder("INBOX"), 1, Integer.MAX_VALUE);
			assertTrue(cr.getFullLength() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testCachedList() {
		IListConversations lc = account.getListConversations();
		try {
			ConversationReferenceList crl = lc.list(new IMAPFolder("INBOX"), 1, 25);
			System.out.println("crl full: " + crl.getFullLength()
					+ " pageLen: " + crl.getPage().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testLoad() {
		ConversationCache cc = new ConversationCache(account);
		IFolder f = new IMAPFolder("INBOX");
		try {
			ConversationReferenceList cr = cc.list(f, 1, Integer.MAX_VALUE);
			assertTrue(cr.getFullLength() > 0);

			for (ConversationReference c : cr.getPage()) {
				long time = System.currentTimeMillis();
				ConversationReference found = cc.find(c.getId());
				time = System.currentTimeMillis() - time;
				System.out.println("conv ref found in " + time + "ms");

				time = System.currentTimeMillis();
				List<MessageId> lm = new LinkedList<MessageId>();
				lm.addAll(found.getMessageIds());
				MailMessage[] ms = account.getLoadMessages().load(f, lm);
				assertNotNull(ms);
				time = System.currentTimeMillis() - time;
				System.out.println("messages loaded in " + time + "ms.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
}
