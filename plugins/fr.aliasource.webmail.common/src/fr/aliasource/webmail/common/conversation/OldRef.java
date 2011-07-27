package fr.aliasource.webmail.common.conversation;

public class OldRef {

	private String uidSeq;
	private boolean starred;
	private boolean read;
	private boolean answered;
	private boolean highPriority;

	public OldRef(boolean read, boolean starred, boolean answered, boolean highPriority, String uidSeq) {
		this.read = read;
		this.starred = starred;
		this.uidSeq = uidSeq;
		this.answered = answered;
		this.highPriority = highPriority;
	}

	public String getUidSeq() {
		return uidSeq;
	}

	public boolean isStarred() {
		return starred;
	}

	public boolean isRead() {
		return read;
	}

	public boolean isAnswered() {
		return answered;
	}

	public boolean isHighPriority() {
		return highPriority;
	}

}
