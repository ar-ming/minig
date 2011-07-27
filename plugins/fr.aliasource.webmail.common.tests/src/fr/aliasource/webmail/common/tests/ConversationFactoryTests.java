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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.Address;
import org.minig.imap.impl.MailThread;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.conversation.ConversationFactory;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.conversation.PendingNotifications;
import fr.aliasource.webmail.common.conversation.RawMessage;
import fr.aliasource.webmail.common.conversation.SetConversationReference;
import fr.aliasource.webmail.common.conversation.VersionnedList;
import fr.aliasource.webmail.common.folders.IMAPFolder;

public class ConversationFactoryTests extends WebmailTestCase {

	public void testConstructor() {
		ConversationFactory cf = new ConversationFactory(account,
				new IMAPFolder("INBOX"), new HashSet<String>(),
				new AccountConfiguration());
		assertNotNull(cf);
	}

	public void testMerge() throws InterruptedException {
		ConversationFactory cf = new ConversationFactory(account,
				new IMAPFolder("INBOX"), new HashSet<String>(),
				new AccountConfiguration());

		// (3) => (4 3)

		VersionnedList<ConversationReference> oldRefs = new VersionnedList<ConversationReference>();
		ConversationReference old = new SetConversationReference("INBOX/3",
				"re: toto", "INBOX");
		old.addMessage(new MessageId(3));
		oldRefs.add(old);

		List<RawMessage> added = new LinkedList<RawMessage>();
		RawMessage rm = new RawMessage(4L, "4@inbox", "inReplyTo", "toto",
				false, new Date().getTime(), new Address("foo@bar.com"), false,
				false, false, new ArrayList<Address>(0), new ArrayList<Address>(0), false, false);
		added.add(rm);

		List<MailThread> mts = new LinkedList<MailThread>();
		MailThread mt = new MailThread();
		mt.add(4L);
		mt.add(3L);
		mts.add(mt);

		PendingNotifications pn = new PendingNotifications(new IMAPFolder(
				"INBOX"));

		System.err.println("before merge.. oldRefs.size: " + oldRefs);
		VersionnedList<ConversationReference> result = cf.merge(oldRefs, added,
				new ArrayList<Long>(0), mts, pn);
		assertNotNull(result);
		System.err.println("after merge. result.size: " + result.size());

		for (ConversationReference cr : result) {
			System.err.println("cr: " + cr.getId() + " size: "
					+ cr.getMessageIds().size());
			for (MessageId mid : cr.getMessageIds()) {
				System.err.println(" - " + mid.getImapId());
			}
		}
		assertEquals(mts.size(), result.size());
	}

}
