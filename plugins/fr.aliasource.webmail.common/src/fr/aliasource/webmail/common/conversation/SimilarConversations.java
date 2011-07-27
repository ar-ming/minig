package fr.aliasource.webmail.common.conversation;

import java.util.Collection;
import java.util.TreeSet;

public class SimilarConversations {

	public static OldRef get(ConversationReference old) {
		return new OldRef(old.isRead(), old.isStarred(), old.isAnswered(), old.isHighPriority(), 
				buildSeqString(old.getUidSequence()));
	}

	public static boolean are(OldRef one, ConversationReference two) {

		if (one.isRead() != two.isRead()) {
			return false;
		}
		if (one.isStarred() != two.isStarred()) {
			return false;
		}
		if (one.isAnswered() != two.isAnswered()) {
			return false;
		}
		if (one.isHighPriority() != two.isHighPriority()) {
			return false;
		}

		Collection<Long> twoSeq = two.getUidSequence();
		String ts = buildSeqString(twoSeq);

		if (one.getUidSeq().equals(ts)) {
			return true;
		} else {
			return false;
		}
	}

	private static String buildSeqString(Collection<Long> oneSeq) {
		TreeSet<Long> sortedSeq = new TreeSet<Long>(oneSeq);
		StringBuilder ret = new StringBuilder();
		for (long l : sortedSeq) {
			ret.append(l);
			ret.append('/');
		}
		return ret.toString();
	}

}
