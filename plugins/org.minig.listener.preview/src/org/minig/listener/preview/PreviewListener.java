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

package org.minig.listener.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.BodyFormattingRegistry;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.IConversationListener;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.MailMessageLoader;

/**
 * Computes previews of conversation : shows the begining of the first
 * non-quoted text of the last message of a conversation
 * 
 * @author tom
 * 
 */
public class PreviewListener implements IConversationListener {

	private IAccount account;
	private static final Log logger = LogFactory.getLog(PreviewListener.class);
	private PreviewExtractor extractor;
	private Set<String> skipped;

	private static final String NO_PREVIEW = "No preview available";

	public PreviewListener(IAccount account, Set<String> skipped) {
		this.account = account;
		this.extractor = new PreviewExtractor();
		this.skipped = skipped;
	}

	@Override
	public void conversationCreated(IFolder folder, ConversationReference cref) {
		String p = computePreview(folder, lastMessageId(cref));
		cref.addMetadata("preview", p);
	}

	private String computePreview(IFolder folder, MessageId lastMessageId) {
		if (isSkippedFolder(folder)) {
			return NO_PREVIEW;
		}

		String ret = NO_PREVIEW;

		MailMessageLoader loader = new MailMessageLoader(account
				.getAttachementManager(), folder);
		loader.setPickupPlain(false);
		IStoreConnection isc = account.getStoreProtocol();
		try {
			loader.select(isc);
			MailMessage ml = loader.fetch(account, lastMessageId, isc, true);
			if (ml != null) {
				MailBody mb = ml.getBody();
				if (!mb.availableFormats().contains("text/plain")) {
					BodyFormattingRegistry bfr = new BodyFormattingRegistry(mb);
					bfr.format();
				}
				String plain = ml.getBody().getValue("text/plain");
				if (plain != null) {
					plain = StringEscapeUtils.escapeHtml(plain);
					ret = extractor.extract(plain);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			isc.destroy();
		}
		return ret;
	}

	private boolean isSkippedFolder(IFolder folder) {
		String n = folder.getName().toLowerCase();
		return skipped.contains(n);
	}

	@Override
	public void conversationRemoved(IFolder folder, ConversationReference cref) {
	}

	@Override
	public void conversationUpdated(IFolder folder, ConversationReference cref) {
		String p = computePreview(folder, lastMessageId(cref));
		cref.addMetadata("preview", p);
	}

	private MessageId lastMessageId(ConversationReference cref) {
		// cyrus id are supposed to grow up, so the biggest id should be a good
		// approx. of the last message
		Set<MessageId> ids = cref.getMessageIds();
		List<MessageId> mids = new ArrayList<MessageId>(ids.size());
		mids.addAll(ids);
		Collections.sort(mids, new Comparator<MessageId>() {
			public int compare(MessageId arg0, MessageId arg1) {
				return new Long(arg0.getImapId()).compareTo(new Long(arg1
						.getImapId()));
			}
		});
		return mids.get(mids.size() - 1);
	}

}
