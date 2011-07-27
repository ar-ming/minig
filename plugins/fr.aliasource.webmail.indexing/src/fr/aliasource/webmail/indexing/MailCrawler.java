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

package fr.aliasource.webmail.indexing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minig.imap.Address;

import fr.aliasource.index.core.AbstractCrawler;
import fr.aliasource.index.core.ICrawler;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.MailIndexingParameters;
import fr.aliasource.webmail.common.conversation.BodyFormattingRegistry;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.AttachmentManager;
import fr.aliasource.webmail.common.message.MailMessageLoader;

/**
 * {@link ICrawler} implementation responsible of fetching emails conversations
 * for full text indexing
 * 
 * @author tom
 * 
 */
public class MailCrawler extends AbstractCrawler {

	private IAccount account;
	private String uid;
	private static final int MAX_UID_IN_CONV = 200;

	public MailCrawler(MailIndexingParameters mip) {
		super();
		this.account = mip.getAccount();
		this.uid = account.getUserId();
	}

	@Override
	public String getType() {
		return uid;
	}

	@Override
	public Map<String, String> fetchData(String id) {
		if (logger.isDebugEnabled()) {
			logger.debug("fetchData(" + id + ")");
		}

		IFolder folder = account.getFolder(id);

		ConversationReference c = null;
		c = account.getCache().getConversationCache().find(id);

		if (c == null) {
			logger.warn("id '" + id + "' not found in '" + folder.getName()
					+ "' for '" + uid + "'");

			return new HashMap<String, String>();
		} else {
			return fetchDataFromConversation(c);
		}

	}

	private Map<String, String> fetchDataFromConversation(
			ConversationReference c) {

		Map<String, String> ret = new HashMap<String, String>();

		if (skippedConversation(c)) {
			return ret;
		}

		long time = System.currentTimeMillis();

		Set<String> convSenders = new HashSet<String>();
		Set<String> convTo = new HashSet<String>();
		Set<String> convCc = new HashSet<String>();
		ArrayList<String> convAttachement = new ArrayList<String>();
		StringBuilder convContent = new StringBuilder(50000);

		String has = "";

		AttachmentManager am = account.getAttachementManager();

		MailMessageLoader mml = new MailMessageLoader(am, new IMAPFolder(
				c.getSourceFolder()));

		MailSplitter msplit = new MailSplitter();

		IStoreConnection sc = account.getStoreProtocol();

		try {
			mml.select(sc);
			for (MessageId mid : c.getMessageIds()) {
				MailMessage mm = mml.fetch(account, mid, sc, true);
				if (mm != null) {

					MailBody mb = mm.getBody();
					if (!mb.availableFormats().contains("text/plain")) {
						BodyFormattingRegistry bfr = new BodyFormattingRegistry(mb);
						bfr.format();
					}
					String body = mm.getBody().getValue("text/plain");
					if (body != null) {
						body = body.replace("*", "");
						convContent.append(body);
					} else {
						logger.warn("no text/plain for "+c.getTitle());
					}
					convContent.append(' ');

					// from:
					if (mm.getSender() != null) {
						if (mm.getSender().getDisplayName() != null) {
							convSenders.add(mm.getSender().getDisplayName());
						}
						if (mm.getSender().getMail() != null) {
							convSenders.add(msplit.getIndexedMailPart(mm
									.getSender()));
						}
					}

					// to:
					List<Address> aTo = mm.getTo();
					for (Address a : aTo) {
						if (a.getDisplayName() != null) {
							convTo.add(a.getDisplayName());
						}
						if (a.getMail() != null) {
							convTo.add(msplit.getIndexedMailPart(a));
						}
					}

					// cc:
					List<Address> aCc = mm.getCc();
					for (Address a : aCc) {
						if (a.getDisplayName() != null) {
							convCc.add(a.getDisplayName());
						}
						if (a.getMail() != null) {
							convCc.add(msplit.getIndexedMailPart(a));
						}
					}

					// filename:
					Map<String, String> attach = mm.getAttachements();
					for (String key : attach.keySet()) {
						if (!convAttachement.contains(attach.get(key))) {
							convAttachement.add(attach.get(key));
						}
					}

				}
			}
		} catch (Exception e) {
			logger.error("Error loading data from conv '" + c.getTitle()
					+ "': " + e.getMessage(), e);
		} finally {
			sc.destroy();
		}

		StringBuilder senders = new StringBuilder();
		for (String s : convSenders) {
			senders.append(s).append(' ');
		}
		StringBuilder to = new StringBuilder();
		for (String s : convTo) {
			to.append(s).append(' ');
		}
		StringBuilder cc = new StringBuilder();
		for (String s : convCc) {
			cc.append(s).append(' ');
		}
		StringBuilder attachements = new StringBuilder();
		for (String s : convAttachement) {
			attachements.append(s).append(' ');
		}
		if (c.isWithAttachments()) {
			has = "attachment";
		}
		if (c.isWithInvitation()) {
			has += " invitation";
		}

		String[] splitFolder = c.getSourceFolder().split("/");
		String in = splitFolder[splitFolder.length - 1] + " anywhere "
				+ c.getSourceFolder();
		String is = "unread";
		if (c.isRead()) {
			is = "read";
		}
		if (c.isStarred()) {
			is += " starred";
		}
		if (c.isHighPriority()) {
			is += " important";
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Timestamp ts = new Timestamp(c.getLastMessageDate());

		ret.put("id", c.getId());
		ret.put("type", uid);
		ret.put("body", convContent.toString());
		ret.put("subject", c.getTitle());
		ret.put("from", senders.toString());
		ret.put("to", to.toString());
		ret.put("cc", cc.toString());
		ret.put("filename", attachements.toString());
		ret.put("has", has);
		ret.put("in", in);
		ret.put("is", is);
		ret.put("date", df.format(ts));

		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug("Conversation data extracted in " + time + "ms ("
					+ c.getMessageIds().size() + " messages)");
		}

		ret.put("data", "");

		return ret;
	}

	private boolean skippedConversation(ConversationReference c) {
		// TODO allow definition of exclusion rules through plugins ?
		if (c.getTitle() != null && c.getTitle().contains("*SPAM*")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipped spam: " + c.getTitle());
			}
			return true;
		}
		if (c.getMessageIds().size() > MAX_UID_IN_CONV) {
			logger.warn("Warning conversation with title '" + c.getTitle()
					+ "' has more than " + MAX_UID_IN_CONV + " message ("
					+ c.getMessageIds().size() + ") : skipped.");
			return true;
		}
		return false;
	}
}
