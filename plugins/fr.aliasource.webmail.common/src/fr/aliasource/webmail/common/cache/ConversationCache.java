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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.aliasource.index.core.Hit;
import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IFindReference;
import fr.aliasource.webmail.common.IListConversations;
import fr.aliasource.webmail.common.ISetFlags;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.conversation.VersionnedList;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * Maintains an xml cache of conversations for each mail folder
 * 
 * @author tom
 * 
 */
public class ConversationCache implements IListConversations, ISetFlags,
		IFindReference {

	private IAccount account;
	private Log logger;
	private Map<IFolder, FolderConversationCache> fccs;

	public ConversationCache(IAccount account) {
		this.logger = LogFactory.getLog(getClass());
		this.account = account;
		fccs = new HashMap<IFolder, FolderConversationCache>();
	}

	private FolderConversationCache cache(IFolder f) {
		FolderConversationCache fcc = fccs.get(f);
		if (fcc == null) {
			fcc = new FolderConversationCache(account, f);
			fccs.put(f, fcc);
		}
		return fcc;
	}

	public static String getCleanFolderName(IFolder f) {
		return f.getName().toLowerCase().replaceAll(" ", "__").replaceAll(
				File.separator, ".");
	}

	public void fastUpdate(IFolder f, Collection<Long> addedUids, Collection<Long> removedUids)
			throws InterruptedException {
		if (addedUids.isEmpty() && removedUids.isEmpty()) {
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(account.getUserId() + " fastUpdate(" + f.getName()
					+ ", " + addedUids.size() + " added, " + removedUids.size()
					+ " removed)");
		}

		cache(f).merge(addedUids, removedUids);
	}

	/**
	 * Creates conversation cache for the given folder if it does not exist.
	 * 
	 * Returns the list of cached conversations.
	 * 
	 * @param f
	 * @return list of conversations for the given folder
	 */
	public ConversationReferenceList list(IFolder f, int pageNum, int pageLength) {
		// Document dom = cache(f).getDOM();
		VersionnedList<ConversationReference> page = new VersionnedList<ConversationReference>();
		// int len = 0;
		// if (dom != null) {
		// len = cache(f).loadReferencesCache(dom, page, pageNum, pageLength);
		// }
		ConversationsXmlSlicer slicer = new ConversationsXmlSlicer(page,
				pageNum, pageLength, 0); // fetch version
		cache(f).customParse(slicer);
		return new ConversationReferenceList(page, slicer.getLen());
	}

	public int unreadCount(IFolder f) {
		return cache(f).unreadCount();
	}

	/**
	 * Finds the conversation id of a message id
	 */
	public String findConversationId(IFolder f, MessageId mid) {
		Document refs = cache(f).getDOM();

		String ret = null;
		String toFind = "" + mid.getImapId();
		if (refs != null) {
			NodeList mids = refs.getElementsByTagName("mid");
			int len = mids.getLength();
			Element foundMid = null;
			for (int i = 0; i < len; i++) {
				Element mide = (Element) mids.item(i);
				if (toFind.equals(mide.getAttribute("id"))) {
					foundMid = mide;
					break;
				}
			}
			if (foundMid != null) {
				Element conv = (Element) foundMid.getParentNode()
						.getParentNode();
				ret = conv.getAttribute("id");
			}
		} else {
			logger.warn("No ref cache for " + f.getName());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("findConversationId(" + f.getName() + ", "
					+ mid.getImapId() + ") => " + ret);
		}
		return ret;
	}

	/**
	 * Loads the conversation with the given id.
	 */
	public ConversationReference find(String convId) {
		IFolder f = getOrigFolder(convId);
		ConversationReference cr = null;
		if (f != null) {
			FolderConversationCache cache = cache(f);
			cr = cache.find(convId);
		}
		if (cr == null) {
			logger.warn("conversation with id '" + convId
					+ "' not found in folder " + f.getName());
		}
		return cr;
	}

	public IFolder getOrigFolder(String conversationId) {
		return account.getFolder(conversationId);
	}

	@Override
	public void setFlags(Set<String> conversationIds, String flag)
			throws InterruptedException, IOException, StoreException {
		updateFlags(conversationIds, flag, true);
	}

	private void updateFlags(Set<String> conversationIds, String flag,
			boolean set) throws InterruptedException, IOException,
			StoreException {
		Set<IFolder> folders = new HashSet<IFolder>();
		Map<IFolder, Set<MessageId>> ids = new HashMap<IFolder, Set<MessageId>>();
		for (String convId : conversationIds) {
			IFolder folder = getOrigFolder(convId);
			folders.add(folder);
			Set<MessageId> mids = ids.get(folder);
			if (mids == null) {
				mids = new HashSet<MessageId>();
				ids.put(folder, mids);
			}
			ConversationReference cr = find(convId);

			if (cr == null) {
				continue;
			}
			mids.addAll(cr.getMessageIds());
			logger.info((set ? "" : "un") + "set " + flag + " on " + convId);
		}

		FlagsList flags = getIMAPFlags(flag, set);

		for (IFolder folder : folders) {
			Set<MessageId> mids = ids.get(folder);
			List<Long> added = new ArrayList<Long>(mids.size());
			for (final MessageId mid: mids) {
				added.add(mid.getImapId());
			}
			IStoreConnection proto = account.getStoreProtocol();
			try {
				proto.select(folder.getName());
				logger.info("flag change: " + (set ? "+" : "-") + flag + " on "
						+ added.size() + " uids in " + folder.getName());
				proto.uidStore(added, flags, set);
			} finally {
				proto.destroy();
			}
			cache(folder).merge(added, new ArrayList<Long>(0));
		}
	}

	private FlagsList getIMAPFlags(String flag, boolean set) {
		FlagsList flags = new FlagsList();
		if (ISetFlags.READ.equals(flag)) {
			flags.add(Flag.SEEN);
		}
		if (ISetFlags.STAR.equals(flag)) {
			flags.add(Flag.FLAGGED);
		}
		if (ISetFlags.ANSWERED.equals(flag)) {
			flags.add(Flag.ANSWERED);
		}
		return flags;
	}

	@Override
	public void unsetFlags(Set<String> conversationIds, String flag)
			throws InterruptedException, IOException, StoreException {
		updateFlags(conversationIds, flag, false);
	}

	@Override
	public void setFlags(String query, String flag)
			throws InterruptedException, IOException, StoreException {
		updateFolderConversationsFlags(query, flag, true);

	}

	@Override
	public void unsetFlags(String query, String flag)
			throws InterruptedException, IOException, StoreException {
		updateFolderConversationsFlags(query, flag, false);
	}

	private void updateFolderConversationsFlags(String query, String flag,
			boolean set) throws InterruptedException, IOException,
			StoreException {

		Set<String> uids = new HashSet<String>();
		SearchDirector sd = account.getSearchDirector();
		List<Hit> results = sd.findByType(account.getUserId(), query);
		for (Hit hit : results) {
			uids.add(hit.getPayload().get("id").toString());
		}
		updateFlags(uids, flag, set);

	}
}
