package fr.aliasource.webmail.common.conversation;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;

public class ThreadRoot {

	private static final Log logger = LogFactory.getLog(ThreadRoot.class);

	private ConversationReference cr;

	/**
	 * Create from an existing conversation reference
	 * 
	 * @param cr
	 */
	public ThreadRoot(ConversationIdAllocator allocator,
			ConversationReference cr) {
		this.cr = cr;
	}

	/**
	 * Creates a new conversation from a new message
	 * 
	 * @param allocator
	 * @param rm
	 */
	public ThreadRoot(ConversationIdAllocator allocator, RawMessage rm) {
		this(allocator, new SetConversationReference(allocator.allocateUnique(),
				rm.getSubject(), allocator.getfName()));
		mergeMessage(allocator, rm);
	}

	public ConversationReference getCr() {
		return cr;
	}

	public void doRemove(Long imapId) {
		cr.getMessageIds().remove(new MessageId(imapId));
	}

	public Set<MessageId> imapIds() {
		return cr.getMessageIds();
	}
	
	public boolean isDead() {
		return cr.getMessageIds().isEmpty();
	}

	public MessageId mergeMessage(ConversationIdAllocator allocator,
			RawMessage raw) {
		MessageId mid = getId(raw);
		cr.addMessage(mid);
		updateConv(allocator, raw, mid);
		return mid;
	}

	public void mergeFlagUpdate(ConversationIdAllocator allocator,
			RawMessage raw) {

		MessageId mid = null;
		for (MessageId m : cr.getMessageIds()) {
			if (m.getImapId() == raw.getImapId()) {
				mid = m;
				break;
			}
		}

		if (mid != null) {
			mid.setRead(raw.isRead());
			mid.setStarred(raw.isStarred());
			mid.setAnswered(raw.isAnswered());
			mid.setHighPriority(raw.isHighPriority());

			updateConv(allocator, raw, mid);

			if (cr.isRead() != mid.isRead()) {
				resyncReadFlag();
			}
			if (cr.isStarred() != mid.isStarred()) {
				resyncStarredFlag();
			}
			if (cr.isAnswered() != mid.isAnswered()) {
				resyncAnsweredFlag();
			}
			if (cr.isHighPriority() != mid.isHighPriority()) {
				resyncPriorityFlag();
			}

			if (logger.isDebugEnabled()) {
				logger.debug("merged flag update raw.read: " + raw.isRead()
						+ " cr.read: " + cr.isRead());
			}

		} else {
			logger.warn("merge FAILED, did not find message with messageId "
					+ raw.getSmtpId());
		}
	}

	private void resyncReadFlag() {
		cr.setRead(true);
		for (MessageId m : cr.getMessageIds()) {
			if (!m.isRead()) {
				cr.setRead(false);
				break;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.info("resyncRead: " + cr.isRead());
		}
	}

	private void resyncStarredFlag() {
		cr.setStarred(false);
		for (MessageId m : cr.getMessageIds()) {
			if (m.isStarred()) {
				cr.setStarred(true);
				break;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.info("resyncStarred: " + cr.isStarred());
		}
	}

	private void resyncAnsweredFlag() {
		cr.setAnswered(false);
		for (MessageId m : cr.getMessageIds()) {
			if (m.isAnswered()) {
				cr.setAnswered(true);
				break;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.info("resyncAnswered: " + cr.isAnswered());
		}
	}

	private void resyncPriorityFlag() {
		cr.setHighPriority(false);
		for (MessageId m : cr.getMessageIds()) {
			if (m.isHighPriority()) {
				cr.setHighPriority(true);
				break;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.info("resyncPriority: " + cr.isHighPriority());
		}
	}

	private MessageId getId(RawMessage raw) {
		MessageId mid = new MessageId(raw.getImapId());
		mid.setSmtpId(raw.getSmtpId());
		mid.setRead(raw.isRead());
		mid.setStarred(raw.isStarred());
		mid.setAnswered(raw.isAnswered());
		mid.setHighPriority(raw.isHighPriority());
		return mid;
	}

	private void updateConv(ConversationIdAllocator allocator, RawMessage raw,
			MessageId mid) {
		if (!raw.isRead()) {
			cr.setRead(false);
		}
		if (raw.isStarred()) {
			cr.setStarred(true);
		}
		if (raw.isAnswered()) {
			cr.setAnswered(true);
		}
		if (raw.isHighPriority()) {
			cr.setHighPriority(true);
		}
		if (raw.isWithAttachments()) {
			cr.setWithAttachments(true);
		}
		if (raw.isWithInvitation()) {
			cr.setWithInvitation(true);
		}
		if (!allocator.isParticipantsAreRecipients()) {
			cr.addParticipant(raw.getFrom());
		} else {
			if (raw.getTo() != null) {
				for (Address a : raw.getTo()) {
					cr.addParticipant(a);
				}
			}
			if (raw.getCc() != null) {
				for (Address a : raw.getCc()) {
					cr.addParticipant(a);
				}
			}
		}
		cr.setLastMessageDate(raw.getDate());
	}

	public boolean contains(long imapId) {
		return cr.getMessageIds().contains(new MessageId(imapId));
	}

	@Override
	public boolean equals(Object obj) {
		return cr.getId().equalsIgnoreCase(((ThreadRoot) obj).cr.getId());
	}

	@Override
	public int hashCode() {
		return cr.getId().hashCode();
	}

}
