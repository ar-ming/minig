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

import java.util.List;

import org.minig.imap.Address;


/**
 * Intermediate class used by the conversation grouping algorithm
 * 
 * @author tom
 * 
 */
public class RawMessage {

	private long imapId;
	private String subject;
	private Address from;
	private boolean read;
	private long date;
	private boolean withAttachments;
	private boolean starred;
	private boolean answered;
	private boolean withInvitation;
	private List<Address> cc;
	private List<Address> to;
	private String smtpId;
	private String inReplyTo;
	private boolean highPriority;

	public RawMessage(long imapId, String smtpId, String inReplyTo,
			String subject, boolean read, long date, Address from,
			boolean attach, boolean starred, boolean withInvitation,
			List<Address> to, List<Address> cc, boolean answered, boolean prio) {
		this.imapId = imapId;
		this.smtpId = smtpId;
		this.inReplyTo = inReplyTo;
		this.subject = subject;
		this.read = read;
		this.date = date;
		this.from = from;
		this.withAttachments = attach;
		this.starred = starred;
		this.answered = answered;
		this.withInvitation = withInvitation;
		this.cc = cc;
		this.to = to;
		this.highPriority = prio;
	}

	public long getImapId() {
		return imapId;
	}

	public String getSubject() {
		return subject;
	}

	public boolean isRead() {
		return read;
	}

	public long getDate() {
		return date;
	}

	public Address getFrom() {
		return from;
	}

	public boolean isWithAttachments() {
		return withAttachments;
	}

	public boolean isStarred() {
		return starred;
	}

	public boolean isWithInvitation() {
		return withInvitation;
	}

	public List<Address> getCc() {
		return cc;
	}

	public List<Address> getTo() {
		return to;
	}

	public String getSmtpId() {
		return smtpId;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public boolean isAnswered() {
		return answered;
	}

	public boolean isHighPriority() {
		return highPriority;
	}

}
