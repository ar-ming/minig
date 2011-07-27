package fr.aliasource.webmail.common.threads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.SearchQuery;
import org.minig.imap.impl.MailThread;
import org.minig.threading.IThreadable;
import org.minig.threading.Threadable;
import org.minig.threading.Threader;

import fr.aliasource.webmail.common.conversation.HeadersHelper;
import fr.aliasource.webmail.common.conversation.RawMessageList;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

public class ThreadFetcher {

	private IStoreConnection sp;
	private Log logger = LogFactory.getLog(getClass());

	public ThreadFetcher(IStoreConnection sp) {
		this.sp = sp;
	}

	public List<MailThread> fetchThreads() throws IOException, StoreException {
		return sp.uidThreads();
	}

	public List<MailThread> fetchSoftwareThreads(RawMessageList rml)
			throws IOException, StoreException {
		long time = System.currentTimeMillis();
		List<MailThread> ret = new LinkedList<MailThread>();

		Map<Long, IMAPHeaders> headers = rml.getHeaders();
		Collection<Long> uids = sp.uidSearch(new SearchQuery());

		if (uids.isEmpty()) {
			return ret;
		}
		if (headers.size() < uids.size()) {
			headers = HeadersHelper.headers(sp, uids,
					new String[] { "references" });
		}

		List<IThreadable> vit = new ArrayList<IThreadable>(headers.size() + 1);
		int i = 0;
		for (IMAPHeaders heads : headers.values()) {
			String mid = heads.getRawHeader("message-id");
			if (mid == null) {
				mid = "<" + System.nanoTime() + "-" + (i++) + "@minig.org>";
			}
			String irt = heads.getRawHeader("in-reply-to");
			String refs = heads.getRawHeader("references");
			if (logger.isDebugEnabled()) {
				logger.debug("uid: " + heads.getUid() + " mid: " + mid
						+ " irt: " + irt + " refs: " + refs);
			}
			Threadable t = new Threadable(mid, heads.getUid(), irt, refs);
			vit.add(t);
		}

		Threader threader = new Threader();
		Threadable result = (Threadable) threader.thread(vit);
		time = System.currentTimeMillis() - time;
		ret = convertResult(result);
		return ret;
	}

	private List<MailThread> convertResult(Threadable result) {
		List<MailThread> mts = new LinkedList<MailThread>();
		for (Threadable t = result; t != null; t = t.getNext()) {
			MailThread mt = new MailThread();
			mt.add(t.getImapId());
			addBranch(mt, t.getChild());
			mts.add(mt);
		}
		return mts;
	}

	private void addBranch(MailThread mt, Threadable t) {
		if (t == null) {
			return;
		}
		mt.add(t.getImapId());
		addBranch(mt, t.getChild());
		addBranch(mt, t.getNext());
	}

}
