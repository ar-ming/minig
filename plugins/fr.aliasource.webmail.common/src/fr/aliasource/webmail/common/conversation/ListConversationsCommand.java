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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.SearchQuery;
import org.minig.imap.impl.MailThread;
import org.minig.imap.mime.MimeTree;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.IDirectCommand;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;
import fr.aliasource.webmail.common.threads.ThreadFetcher;

/**
 * Fetch message headers from IMAP folders and group them as conversations.
 * Caching is handled at a higher level.
 * 
 * @author tom
 * 
 */
public class ListConversationsCommand implements
		IDirectCommand<ConversationReferenceList> {

	private IAccount account;
	private ConversationFactory conversationFactory;
	// private List<ConversationReference> oldRefs;
	private IFolder f;
	private Log logger = LogFactory.getLog(getClass());
	private boolean isShowRecipientsFolder;

	private static final String[] HEADS_NO_ENV = new String[] { "X-Priority",
			"references" };
	private static final String[] HEADS_SENT_NO_ENV = new String[] {
			"X-Priority", "references" };

	public ListConversationsCommand(IAccount account, IFolder f) {
		this.account = account;

		AccountConfiguration aconf = new AccountConfiguration();
		String skipList = aconf.getSetting(account.getUserId(),
				AccountConfiguration.SKIPPED_FOLDERS);
		String[] skipped = skipList.split(",");
		Set<String> skipSet = new HashSet<String>();
		for (String s : skipped) {
			skipSet.add(s.toLowerCase());
		}

		this.conversationFactory = new ConversationFactory(account, f, skipSet,
				aconf);
		this.f = f;
		// this.oldRefs = new LinkedList<ConversationReference>();
		String u = account.getUserId();
		String n = f.getName();
		this.isShowRecipientsFolder = aconf.getSetting(u,
				AccountConfiguration.SENT).equals(n)
				|| aconf.getSetting(u, AccountConfiguration.DRAFTS).equals(n)
				|| aconf.getSetting(u, AccountConfiguration.TEMPLATES)
						.equals(n);
	}

	private RawMessageList getServerData(List<ConversationReference> oldRefs,
			Collection<Long> all) throws InterruptedException, IOException,
			StoreException {

		long listTime = System.currentTimeMillis();
		List<RawMessage> rawMessages = new ArrayList<RawMessage>(
				all.size() + 50);
		RawMessageList ret = new RawMessageList(rawMessages);
		IStoreConnection proto = account.getStoreProtocol();
		try {
			proto.select(f.getName());

			Collection<Long> fullUidList = proto.uidSearch(new SearchQuery());
			ret.setUids(fullUidList);

			long time = System.currentTimeMillis();
			Map<Long, IMAPHeaders> headers = new HashMap<Long, IMAPHeaders>();
			if (!all.isEmpty()) {
				headers = HeadersHelper.headers(proto, all,
						(isShowRecipientsFolder ? HEADS_SENT_NO_ENV
								: HEADS_NO_ENV));
			}
			if (logger.isDebugEnabled()) {
				time = System.currentTimeMillis() - time;
				logger
						.info("Fetched " + headers.size() + " headers in "
								+ time);
			}

			time = System.currentTimeMillis();
			Collection<FlagsList> flags = Collections.emptyList();
			if (!fullUidList.isEmpty()) {
				flags = proto.uidFetchFlags(fullUidList);
			}
			if (logger.isDebugEnabled()) {
				time = System.currentTimeMillis() - time;
				logger.info("Fetched flags in " + time + "ms.");
			}
			HashSet<Long> unseen = new HashSet<Long>();
			HashSet<Long> seen = new HashSet<Long>();
			HashSet<Long> starred = new HashSet<Long>();
			HashSet<Long> answered = new HashSet<Long>();
			for (FlagsList ifl : flags) {
				if (ifl == null) {
					continue;
				}
				if (!ifl.contains(Flag.SEEN)) {
					unseen.add(ifl.getUid());
				} else {
					seen.add(ifl.getUid());
				}
				if (ifl.contains(Flag.FLAGGED)) {
					starred.add(ifl.getUid());
				}
				if (ifl.contains(Flag.ANSWERED)) {
					answered.add(ifl.getUid());
				}
			}
			HashSet<Long> withAttachments = new HashSet<Long>();
			HashSet<Long> withInvitations = new HashSet<Long>();
			Collection<MimeTree> mts = Collections.emptyList();

			time = System.currentTimeMillis();
			if (!all.isEmpty()) {
				mts = proto.uidFetchBodystructure(all);
				for (MimeTree mt : mts) {
					if (hasAttachments(mt)) {
						withAttachments.add(mt.getUid());
					}
					if (hasInvitation(mt)) {
						withInvitations.add(mt.getUid());
					}
				}
			}
			if (logger.isDebugEnabled()) {
				time = System.currentTimeMillis() - time;
				logger.info("Fetched body structures in " + time + "ms.");
			}

			for (IMAPHeaders h : headers.values()) {
				if (h == null || h.getRawHeaders().isEmpty()) {
					continue;
				}
				RawMessage msg = parseHeaders(unseen, withAttachments, starred,
						answered, h, h.getUid(), withInvitations);
				rawMessages.add(msg);
			}
			ret.setHeaders(headers);

			for (ConversationReference cr : oldRefs) {
				if (cr.isRead()) {
					break;
				}
				if (allSeen(cr, seen, proto)) {
					if (logger.isDebugEnabled()) {
						logger.info("Detected '" + cr.getTitle()
								+ "' was read by another client");
					}
					List<RawMessage> ra = rawMessages(cr, unseen, starred,
							answered, proto, headers);
					rawMessages.addAll(ra);
				}
			}

		} finally {
			proto.destroy();
		}

		if (logger.isInfoEnabled()) {
			listTime = System.currentTimeMillis() - listTime;
			logger.info("[" + account.getUserId() + "][" + f.getName()
					+ "] fetched " + rawMessages.size() + " in " + listTime
					+ "ms.");
		}

		return ret;
	}

	private List<RawMessage> rawMessages(ConversationReference cr,
			HashSet<Long> unseen, HashSet<Long> starred,
			HashSet<Long> answered, IStoreConnection store,
			Map<Long, IMAPHeaders> fetchedHeaders) throws IOException,
			StoreException {
		ArrayList<RawMessage> ra = new ArrayList<RawMessage>(cr.getMessageIds()
				.size());
		Collection<Long> uids = cr.getUidSequence();
		HashMap<Long, IMAPHeaders> h = HeadersHelper.headers(store, uids,
				(isShowRecipientsFolder ? HEADS_SENT_NO_ENV : HEADS_NO_ENV));
		HashSet<Long> withAttachments = new HashSet<Long>();
		HashSet<Long> withInvitations = new HashSet<Long>();
		Collection<MimeTree> mts = store.uidFetchBodystructure(uids);
		for (MimeTree mt : mts) {
			if (hasAttachments(mt)) {
				withAttachments.add(mt.getUid());
			}
			if (hasInvitation(mt)) {
				withInvitations.add(mt.getUid());
			}
		}

		for (IMAPHeaders hi : h.values()) {
			ra.add(parseHeaders(unseen, withAttachments, starred, answered, hi,
					hi.getUid(), withInvitations));
		}

		return ra;
	}

	private boolean allSeen(ConversationReference cr, HashSet<Long> seen,
			IStoreConnection store) throws IOException, StoreException {
		for (MessageId id : cr.getMessageIds()) {
			if (!seen.contains(id.getImapId())) {
				return false;
			}
		}
		return true;
	}

	private boolean hasAttachments(MimeTree mt) throws IOException,
			StoreException {
		return mt.hasAttachments();
	}

	private boolean hasInvitation(MimeTree mt) {
		return mt.hasInvitation();
	}

	public ConversationReferenceList getData() throws InterruptedException,
			IOException, StoreException {
		Collection<Long> all = Collections.emptyList();

		IStoreConnection proto = account.getStoreProtocol();
		try {
			proto.select(f.getName());
			all = proto.uidSearch(new SearchQuery());
		} finally {
			proto.destroy();
		}
		return merge(new VersionnedList<ConversationReference>(), all,
				new ArrayList<Long>(0));
	}

	private RawMessage parseHeaders(HashSet<Long> unseen,
			HashSet<Long> withAttachments, HashSet<Long> starred,
			HashSet<Long> answered, IMAPHeaders head, long uid,
			HashSet<Long> withInvitations) {
		boolean read = !unseen.contains(uid);
		boolean attach = withAttachments.contains(uid);
		boolean invitation = withInvitations.contains(uid);
		boolean answer = answered.contains(uid);
		boolean star = starred.contains(uid);
		String subject = head.getSubject();
		String smtpId = head.getRawHeader("message-id");
		String pri = head.getRawHeader("x-priority");
		boolean prio = pri != null && pri.trim().startsWith("1");
		// generate a stable uid when none is found in the message
		if (smtpId == null) {
			smtpId = "<" + subject.hashCode() + "-" + uid
					+ "@generated.minig.org>";
		}

		String inReplyTo = head.getRawHeader("in-reply-to");
		Address a = null;
		List<Address> ccA = Collections.emptyList();
		List<Address> toA = Collections.emptyList();
		if (isShowRecipientsFolder) {
			toA = head.getTo();
			ccA = head.getCc();
		} else {
			a = head.getFrom();
		}

		RawMessage msg = new RawMessage(uid, smtpId, inReplyTo, subject, read,
				head.getDate().getTime(), a, attach, star, invitation, toA,
				ccA, answer, prio);
		return msg;
	}

	/**
	 * Updated the current conversation with given added & removed message ids
	 */
	public ConversationReferenceList merge(
			VersionnedList<ConversationReference> oldRefs, Collection<Long> added,
			Collection<Long> removed) throws InterruptedException, IOException,
			StoreException {
		if (logger.isInfoEnabled()) {
			logger.info("merge with oldRefs.size: " + oldRefs.size()
					+ " added: " + added.size());
		}
		ConversationReferenceList crl = null;
		PendingNotifications notifs = new PendingNotifications(f);
		if (!added.isEmpty() || !removed.isEmpty()) {
			RawMessageList addedM = getServerData(oldRefs, added);
			List<MailThread> threads = null;
			IStoreConnection sp = account.getStoreProtocol();
			try {
				sp.select(f.getName());
				threads = new ThreadFetcher(sp).fetchSoftwareThreads(addedM);
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			} finally {
				sp.destroy();
			}
			VersionnedList<ConversationReference> merged = conversationFactory
					.merge(oldRefs, addedM, removed, threads, notifs);
			crl = new ConversationReferenceList(merged, merged.size());
			account.getCache().getSummaryCache().update();
		} else {
			crl = new ConversationReferenceList(oldRefs, oldRefs.size());
		}
		crl.setPendingNotifications(notifs);
		return crl;
	}

}
