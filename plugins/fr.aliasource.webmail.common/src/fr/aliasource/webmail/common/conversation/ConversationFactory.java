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

package fr.aliasource.webmail.common.conversation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.impl.MailThread;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.Activator;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Groups messages as conversations, looking at close subjects. This class
 * merges updates, added and removed conversations, and notifies conversation
 * listeners for index updates, etc.
 * 
 * @author tom
 * 
 */
public class ConversationFactory {

	private List<IConversationListener> listeners;
	private ConversationIdAllocator allocator;
	private Log logger = LogFactory.getLog(ConversationFactory.class);
	private IFolder folder;
	private IAccount account;

	public ConversationFactory(IAccount account, IFolder folder,
			Set<String> skipSet, AccountConfiguration aconf) {
		this.account = account;
		listeners = new LinkedList<IConversationListener>();
		if (!skipSet.contains(folder.getName().toLowerCase())) {
			for (IConversationListenerFactory clf : Activator.getDefault()
					.getListenerFactories()) {
				listeners.add(clf.createListener(account));
			}
		}

		boolean participantsAreRecipients = aconf.getSetting(
				account.getUserId(), AccountConfiguration.SENT).equals(
				folder.getName())
				|| aconf.getSetting(account.getUserId(),
						AccountConfiguration.DRAFTS).equals(folder.getName())
				|| aconf.getSetting(account.getUserId(),
						AccountConfiguration.TEMPLATES)
						.equals(folder.getName());

		this.allocator = new ConversationIdAllocator(folder,
				participantsAreRecipients);
		this.folder = folder;
	}

	/**
	 * Computes the new list of conversation references, taking into account
	 * which messages got added or removed.
	 */
	public VersionnedList<ConversationReference> merge(
			VersionnedList<ConversationReference> oldRefs,
			List<RawMessage> added, Collection<Long> removed, List<MailThread> threads,
			PendingNotifications notifs) throws InterruptedException {

		if (logger.isDebugEnabled()) {
			logger.debug("[" + account.getUserId() + "][" + folder.getName()
					+ "] merge started...(a: " + added.size() + " r: "
					+ removed.size() + " threads: " + threads.size() + ")");
		}
		long time = System.currentTimeMillis();

		Set<ConversationReference> updatedConversations = new HashSet<ConversationReference>();
		Set<ConversationReference> removedConversations = new HashSet<ConversationReference>();
		Set<ConversationReference> addedConversations = new HashSet<ConversationReference>();

		Map<Long, ThreadRoot> oldIdx = new HashMap<Long, ThreadRoot>(oldRefs
				.size() * 5);
		Map<String, OldRef> oldItems = new HashMap<String, OldRef>();
		List<ThreadRoot> roots = new ArrayList<ThreadRoot>(oldRefs.size());
		Map<Long, RawMessage> rawIdx = new HashMap<Long, RawMessage>();
		Set<ThreadRoot> newRefs = new HashSet<ThreadRoot>(threads.size());

		for (ConversationReference cr : oldRefs) {
			oldItems.put(cr.getId(), SimilarConversations.get(cr));
			ThreadRoot t = new ThreadRoot(allocator, cr);
			roots.add(t);
			for (MessageId mid : cr.getMessageIds()) {
				oldIdx.put(mid.getImapId(), t);
			}
		}

		for (RawMessage rm : added) {
			rawIdx.put(rm.getImapId(), rm);
		}

		// process removals
		for (long l : removed) {
			Iterator<ThreadRoot> it = roots.iterator();
			while (it.hasNext()) {
				ThreadRoot tr = it.next();
				tr.doRemove(l);
				oldIdx.remove(l);
				if (tr.isDead()) {
					removedConversations.add(tr.getCr());
					it.remove();
				}
			}
		}

		Set<Long> existingMails = new HashSet<Long>(2 * threads.size());
		for (MailThread mt : threads) {
			Iterator<Long> it = mt.iterator();
			while (it.hasNext()) {
				long l = it.next();
				existingMails.add(l);
			}
		}
		Iterator<ThreadRoot> it = roots.iterator();
		while (it.hasNext()) {
			ThreadRoot tr = it.next();
			Set<MessageId> trIds = tr.imapIds();
			Iterator<MessageId> ids = trIds.iterator();
			while (ids.hasNext()) {
				long l = ids.next().getImapId();
				if (!existingMails.contains(l)) {
					ids.remove();
					oldIdx.remove(l);
					if (tr.isDead()) {
						removedConversations.add(tr.getCr());
						it.remove();
					}
				}
			}
		}

		newRefs.addAll(roots);
		// end removals processing

		// rebuild conversation list from threads returned by Cyrus
		for (MailThread mt : threads) {
			Collections.sort(mt);
			logThread(mt);
			ThreadRoot t = null;
			for (long l : mt) {
				RawMessage raw = rawIdx.get(l);
				if (t == null) {
					t = oldIdx.get(l);
					if (t != null && raw != null) { // old one, flag change
						mergeChange(t, raw);
						updatedConversations.add(t.getCr());
					} else if (raw != null) {
						t = newConv(raw);
						addedConversations.add(t.getCr());
						newRefs.add(t);
					}
				} else if (raw != null) {
					mergeChange(t, raw);
					if (!updatedConversations.contains(t.getCr())
							&& !addedConversations.contains(t.getCr())) {
						updatedConversations.add(t.getCr());
					}
				}
			}
		}

		// prepare notifications
		notifs.setListeners(listeners);
		for (ConversationReference cref : addedConversations) {
			notifs.added(cref);
		}
		for (ConversationReference cref : removedConversations) {
			notifs.removed(cref);
		}

		int upd = 0;
		int similar = 0;
		for (ConversationReference cref : updatedConversations) {
			OldRef prevCr = oldItems.get(cref.getId());
			if (!SimilarConversations.are(prevCr, cref)) {
				notifs.updated(cref);
				upd++;
			} else {
				similar++;
			}
		}
		logger.info("merge updates: real updates: " + upd + " similar: "
				+ similar);

		VersionnedList<ConversationReference> ret = new VersionnedList<ConversationReference>();
		for (ThreadRoot tr : newRefs) {
			ret.add(tr.getCr());
		}
		ret.setVersion(oldRefs.incrementAndGet());

		Collections.sort(ret, new ConversationComparator());

		time = System.currentTimeMillis() - time;
		logger.info("[" + account.getUserId() + "][" + folder.getName()
				+ "] merge result: " + addedConversations.size() + " / "
				+ updatedConversations.size() + " / "
				+ removedConversations.size() + " (total conv: " + ret.size()
				+ ") in " + time + "ms.");

		if (ret.size() != threads.size()) {
			logger.warn("computed conversation count (" + ret.size()
					+ ") does match cyrus thread count (" + threads.size()
					+ ")");
		}

		return ret;
	}

	private void mergeChange(ThreadRoot t, RawMessage raw) {
		if (t.contains(raw.getImapId())) {
			t.mergeFlagUpdate(allocator, raw);
		} else {
			t.mergeMessage(allocator, raw);
		}
	}

	private void logThread(MailThread mt) {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (long l : mt) {
				sb.append(l);
				sb.append(" ");
			}
			logger.debug("dealing with thread: " + sb.toString());
		}
	}

	private ThreadRoot newConv(RawMessage rawMessage) {
		return new ThreadRoot(allocator, rawMessage);
	}

}
