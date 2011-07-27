package org.minig.threading;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;


public class Threadable implements IThreadable {

	private String messageID;
	private boolean dummy;
	private long imapId;

	private Threadable child;
	private Threadable next;
	private String[] references;

	public Threadable(String messageID, long uid, String irt, String ref) {
		this.messageID = messageID;
		this.imapId = uid;
		Set<String> uniqRefs = new LinkedHashSet<String>();
		if (ref != null) {
			Iterable<String> refSplit = Splitter.on(CharMatcher.anyOf(", "))
											.omitEmptyStrings().trimResults().split(ref);
			for (String s : refSplit) {
				uniqRefs.add(s);
			}
		}
		if (!Strings.isNullOrEmpty(irt)) {
			uniqRefs.add(irt.trim());
		}
		this.references = uniqRefs.toArray(new String[uniqRefs.size()]);
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageId) {
		this.messageID = messageId;
	}

	public boolean isDummy() {
		return dummy;
	}

	public void setDummy(boolean dummy) {
		this.dummy = dummy;
	}

	public long getImapId() {
		return imapId;
	}

	public void setImapId(long imapId) {
		this.imapId = imapId;
	}

	public Threadable getChild() {
		return child;
	}

	public Threadable getNext() {
		return next;
	}

	@Override
	public String[] getReferences() {
		return references;
	}

	@Override
	public String getSimplifiedSubject() {
		return null;
	}

	@Override
	public IThreadable makeDummy() {
		Threadable ret = new Threadable(null, -1, "", "");
		ret.dummy = true;
		return ret;
	}

	@Override
	public void setChild(IThreadable kid) {
		this.child = (Threadable) kid;
	}

	@Override
	public void setNext(IThreadable next) {
		this.next = (Threadable) next;
	}

	@Override
	public boolean subjectIsReply() {
		return false;
	}

}
