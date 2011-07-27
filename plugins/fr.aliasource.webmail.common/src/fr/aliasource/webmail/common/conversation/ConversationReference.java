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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.minig.imap.Address;

public abstract class ConversationReference {

	private String id;
	private String title;
	private Set<MessageId> messages;
	private Collection<Address> participants;
	private long lastMessageDate;
	private boolean read;
	private boolean starred;
	private boolean answered;
	private boolean highPriority;
	private String sourceFolder;
	private boolean withAttachments;
	private boolean withInvitation;
	private Map<String, String> metadata;
	private String prev;
	private String next;

	protected ConversationReference(String id, String title, String sourceFolder) {
		super();
		this.id = id;
		this.title = title;
		this.messages = new TreeSet<MessageId>();
		this.read = true;
		this.participants = initParticipants();
		this.sourceFolder = sourceFolder;
		this.metadata = new HashMap<String, String>();
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void addMessage(MessageId mid) {
		messages.add(mid);
	}
	
	public void removeMessage(MessageId mid) {
		messages.remove(mid);
	}

	public void addParticipant(Address a) {
		participants.add(a);
	}

	public Set<MessageId> getMessageIds() {
		return messages;
	}

	public Collection<Address> getParticipants() {
		return participants;
	}
	
	protected abstract Collection<Address> initParticipants();

	@Override
	public boolean equals(Object obj) {
		ConversationReference ref = (ConversationReference) obj;
		return id.equals(ref.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public long getLastMessageDate() {
		return lastMessageDate;
	}

	public void setLastMessageDate(long lastMessageDate) {
		this.lastMessageDate = lastMessageDate;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public Collection<Long> getUidSequence() {
		List<Long> mids = new ArrayList<Long>(getMessageIds().size());
		for (MessageId mid : getMessageIds()) {
			mids.add(mid.getImapId());
		}
		return mids;
	}

	public boolean isWithAttachments() {
		return withAttachments;
	}

	public void setWithAttachments(boolean attachments) {
		this.withAttachments = attachments;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void addMetadata(String type, String value) {
		metadata.put(type, value);
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public boolean isWithInvitation() {
		return withInvitation;
	}

	public void setWithInvitation(boolean withInvitation) {
		this.withInvitation = withInvitation;
	}

	public void setTitle(String storedString) {
		this.title = storedString;
	}

	public void setPrev(String string) {
		this.prev = string;
	}

	public void setNext(String string) {
		this.next = string;
	}

	public String getNext() {
		return next;
	}

	public String getPrev() {
		return prev;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public boolean isHighPriority() {
		return highPriority;
	}

	public void setHighPriority(boolean highPriority) {
		this.highPriority = highPriority;
	}

}
