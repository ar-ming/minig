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

package fr.aliasource.webmail.common.cache;

import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.minig.imap.Address;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.ListConversationsCommand;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.conversation.PendingNotifications;
import fr.aliasource.webmail.common.conversation.VersionnedList;
import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Maintains an xml cache of conversations for a mail folder
 * 
 * @author tom
 * 
 */
public class FolderConversationCache extends
		FileCache<ConversationReferenceList> {

	private IFolder f;
	private SoftReference<Map<String, ConversationReference>> fatCache;

	private AtomicInteger unreadCount;
	private boolean validUnreadCount;

	protected FolderConversationCache(IAccount account, IFolder f) {
		super(account, "conversations",
				ConversationCache.getCleanFolderName(f),
				new ListConversationsCommand(account, f));
		this.f = f;
		fatCache = new SoftReference<Map<String, ConversationReference>>(null);
		unreadCount = new AtomicInteger(0);
		validUnreadCount = false;
	}

	private ListConversationsCommand lcc() {
		return (ListConversationsCommand) command;
	}

	private Map<String, ConversationReference> buildConvIdCache(
			ConversationReferenceList cl) {
		Map<String, ConversationReference> convIdCache = new HashMap<String, ConversationReference>();
		for (ConversationReference cr : cl.getPage()) {
			convIdCache.put(cr.getId().toLowerCase(), cr);
		}
		return Collections.synchronizedMap(convIdCache);
	}

	@Override
	protected ConversationReferenceList loadCacheFromDOM(Document doc) {
		VersionnedList<ConversationReference> page = new VersionnedList<ConversationReference>();
		ConversationsXmlSlicer slicer = new ConversationsXmlSlicer(page, 1,
				Integer.MAX_VALUE, 0);
		customParse(slicer);
		unreadCount.set(slicer.getUnreadCount());
		validUnreadCount = true;
		return new ConversationReferenceList(page, slicer.getLen());
	}

	@Override
	public void writeToCache(ConversationReferenceList data) throws Exception {
		VersionnedList<ConversationReference> refs = data.getPage();
		Document dom = DOMUtils.createDoc(getCacheNamespace(), "references");
		Element root = dom.getDocumentElement();
		root.setAttribute("version", "" + refs.getVersion());
		int unread = 0;

		for (ConversationReference ref : refs) {
			Element conv = DOMUtils.createElement(root, "conv");
			conv.setAttribute("id", ref.getId());
			conv.setAttribute("date", "" + ref.getLastMessageDate());
			conv.setAttribute("read", "" + ref.isRead());

			if (!ref.isRead()) {
				unread++;
			}

			conv.setAttribute("attach", "" + ref.isWithAttachments());
			conv.setAttribute("invitation", "" + ref.isWithInvitation());
			conv.setAttribute("star", "" + ref.isStarred());
			conv.setAttribute("answer", "" + ref.isAnswered());
			conv.setAttribute("hp", "" + ref.isHighPriority());

			String title = ref.getTitle();
			if (title == null) {
				title = "[No conversation topic]";
			}
			DOMUtils.createElementAndText(conv, "title", title);
			Element participants = DOMUtils.createElement(conv, "participants");
			Collection<Address> ads = ref.getParticipants();
			for (Address a : ads) {
				if (a != null) {
					Element adr = DOMUtils.createElement(participants, "a");
					adr.setAttribute("m", DOMUtils.stripNonValidXMLCharacters(a
							.getMail()));
					if (a.getDisplayName() != null) {
						adr
								.setAttribute("dn", DOMUtils
										.stripNonValidXMLCharacters(a
												.getDisplayName()));
					} else {
						adr.setAttribute("dn", DOMUtils
								.stripNonValidXMLCharacters(a.getMail()));
					}
				}
			}

			Element messages = DOMUtils.createElement(conv, "messages");
			Set<MessageId> ids = ref.getMessageIds();
			for (MessageId id : ids) {
				Element mid = DOMUtils.createElement(messages, "mid");
				mid.setAttribute("id", "" + id.getImapId());
				mid.setAttribute("r", "" + id.isRead());
				mid.setAttribute("s", "" + id.isStarred());
				mid.setAttribute("a", "" + id.isAnswered());
				mid.setAttribute("hp", "" + id.isAnswered());
				mid.setAttribute("sid", id.getSmtpId());
			}
			Element metas = DOMUtils.createElement(conv, "metas");
			for (String metaKey : ref.getMetadata().keySet()) {
				String val = ref.getMetadata().get(metaKey);
				if (val != null) {
					Element meta = DOMUtils.createElementAndText(metas, "meta",
							val);
					meta.setAttribute("type", metaKey);
				}
			}
		}
		unreadCount.set(unread);
		validUnreadCount = true;
		DOMUtils.serialise(dom, new FileOutputStream(getCacheFile()));
	}

	public void merge(Collection<Long> added, Collection<Long> removed) throws InterruptedException {
		if (logger.isDebugEnabled()) {
			logger.debug("merging " + added.size() + " added, "
					+ removed.size() + " removed...");
		}
		if (added.isEmpty() && removed.isEmpty()) {
			return;
		}

		ConversationReferenceList currentList = null;
		try {
			currentList = lcc().merge(getOldRefs(), added, removed);
			fatCache = new SoftReference<Map<String, ConversationReference>>(
					buildConvIdCache(currentList));
		} catch (Exception e) {
			logger.error("[" + account.getUserId() + "] - " + f.getName()
					+ " - Merge failed (" + added.size() + " added,  "
					+ removed.size() + " removed)" + e.getMessage(), e);
			return;
		}

		if (currentList != null
				&& currentList.getPendingNotifications() != null) {
			PendingNotifications notifications = currentList
					.getPendingNotifications();
			notifications.emitNotifications();
		} else {
			logger.warn("No pending notifications after merge");
		}

		updateLock.acquire();
		try {
			writeToCache(currentList);
		} catch (Exception e) {
			logger.error("merged cache writing failed (" + added.size()
					+ "added " + removed.size() + "removed)" + e.getMessage(),
					e);
		} finally {
			updateLock.release();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(f.getName() + ": merge success (" + added.size()
					+ "added " + removed.size() + "removed)");
		}
	}

	public int unreadCount() {
		if (!validUnreadCount) {
			getOldRefs();
		}
		return unreadCount.get();
	}

	private VersionnedList<ConversationReference> getOldRefs() {
		return getCachedData().getPage();
	}

	public synchronized ConversationReference find(String convId) {
		Map<String, ConversationReference> fcValue = fatCache.get();
		if (fcValue == null || fcValue.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("rebuilding conv cache for folder " + f.getName()
						+ ", SoftReference was collected.");
			}
			ConversationReferenceList currentList = getCachedData();
			fcValue = buildConvIdCache(currentList);
			fatCache = new SoftReference<Map<String, ConversationReference>>(
					fcValue);
		}
		synchronized (fcValue) {
			ConversationReference ret = fcValue.get(convId.toLowerCase());
			return ret;
		}
	}

	@Override
	protected boolean useDOMForLoad() {
		return false;
	}
}
