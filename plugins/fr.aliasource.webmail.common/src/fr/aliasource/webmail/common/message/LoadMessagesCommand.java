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

package fr.aliasource.webmail.common.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;

import com.google.common.collect.ImmutableList;

import fr.aliasource.webmail.common.ILoadMessages;
import fr.aliasource.webmail.common.IMAPAccount;
import fr.aliasource.webmail.common.conversation.BodyFormattingRegistry;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.truncation.TruncationService;

public class LoadMessagesCommand implements ILoadMessages {

	private static int TRUNCATION_SIZE = 61440;
	private IMAPAccount account;
	private Log logger;

	public LoadMessagesCommand(IMAPAccount account) {
		this.logger = LogFactory.getLog(getClass());
		this.account = account;
	}

	@Override
	public MailMessage load(IFolder f, MessageId messageIds) {
		MailMessage[] messages = load(f, ImmutableList.of(messageIds));
		if (messages.length != 1) {
			return null;
		} else {
			return messages[0];
		}
	}
	
	public MailMessage[] load(IFolder f, List<MessageId> messageIds) {
		return load(f, messageIds, false);
	}

	public MailMessage[] load(IFolder f, List<MessageId> messageIds,
			boolean truncate) {
		List<MessageId> mids = new ArrayList<MessageId>(messageIds.size());

		// do not load complete conversations with more than 100 messages as it
		// will generate a DOM to big to fit in memory
		if (messageIds.size() > 100) {
			for (int i = messageIds.size() - 100; i < messageIds.size(); i++) {
				mids.add(messageIds.get(i));
			}
		} else {
			mids = messageIds;
		}
		List<MailMessage> lm = new ArrayList<MailMessage>(mids.size());
		MailMessageLoader loader = new MailMessageLoader(
				account.getAttachementManager(), f);
		IStoreConnection protocol = account.getStoreProtocol();
		try {
			loader.select(protocol);

			for (MessageId mid : mids) {
				try {
					long time = System.currentTimeMillis();
					MailMessage m = loader.fetch(account, mid, protocol, false);
					time = System.currentTimeMillis() - time;
					if (m != null) {
						lm.add(m);
						Collection<FlagsList> fl = protocol.uidFetchFlags(Arrays.asList(mid.getImapId()));
						if (!fl.isEmpty()) {
							FlagsList fl0 = fl.iterator().next();
							m.setRead(fl0.contains(Flag.SEEN));
							m.setStarred(fl0.contains(Flag.FLAGGED));
							m.setAnswered(fl0.contains(Flag.ANSWERED));
							if (logger.isDebugEnabled()) {
								logger.debug("[" + account.getUserId()
										+ "] Fetch of uid+flags "
										+ mid.getImapId() + " (r: "
										+ m.isRead() + " s: " + m.isStarred()
										+ " a: " + m.isAnswered() + ") took "
										+ time + "ms.");
							}
						}
					}
				} catch (Throwable e) {
					logger.error(
							"[" + account.getUserId()
									+ "] error fetching message with id "
									+ mid.getImapId() + ": " + e.getMessage(),
							e);
				}
			}
		} catch (Exception se) {
			logger.error("Cannot select folder " + f != null ? f.getName() : "", se);
		} finally {
			protocol.destroy();
		}

		MailMessage[] ret = lm.toArray(new MailMessage[lm.size()]);

		if (truncate) {
			for (MailMessage m : ret) {
				String format = "";
				if (m.getBody().availableFormats().contains("text/plain")) {
					format = "text/plain";
				} else if (m.getBody().availableFormats().contains("text/html")) {
					format = "text/html";
				} else {
					format = m.getBody().availableFormats().iterator().next();
				}

				String body = m.getBody().getValue(format);

				// limit to plain, trying to truncate html would be too hard
				if (body != null && body.length() > TRUNCATION_SIZE
						&& "text/plain".equals(format)) {
					try {
						String trunc = TruncationService.getInstance()
								.truncate(format, body, TRUNCATION_SIZE);
						trunc += "\n...";
						m.getBody().clear();
						m.getBody().addConverted("text/truncatedPlain", trunc);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}

		for (MailMessage mm : ret) {
			formatFwdMessage(mm);
		}

		Arrays.sort(ret);

		return ret;
	}

	private void formatFwdMessage(MailMessage mm) {
		BodyFormattingRegistry bfr = new BodyFormattingRegistry(mm,
				account.getAttachementManager(), mm.getAttachements());
		bfr.format();
		if (mm.getForwardMessage() != null) {
			for (MailMessage fwd : mm.getForwardMessage()) {
				formatFwdMessage(fwd);
			}
		}

	}
}
