package fr.aliasource.webmail.common.conversation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.minig.imap.Envelope;
import org.minig.imap.IMAPHeaders;

import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

public class HeadersHelper {

	public static HashMap<Long, IMAPHeaders> headers(IStoreConnection sc, Collection<Long> uid,
			String[] nonEnvHeaders) throws IOException, StoreException {

		Collection<Envelope> envs = sc.uidFetchEnvelopes(uid);
		Collection<IMAPHeaders> hs = sc.uidFetchHeaderFields(uid, nonEnvHeaders); 
		HashMap<Long, IMAPHeaders> map = new HashMap<Long, IMAPHeaders>();
		for (IMAPHeaders h : hs) {
			map.put(h.getUid(), h);
		}

		for (Envelope e : envs) {
			long id = e.getUid();
			IMAPHeaders h = map.get(id);
			Map<String, String> raw = h.getRawHeaders();
			raw.put("in-reply-to", e.getInReplyTo());
			h.setTo(e.getTo());
			h.setCc(e.getCc());
			h.setBcc(e.getBcc());
			h.setSubject(e.getSubject());
			h.setDate(e.getDate());
			h.setFrom(e.getFrom());
			raw.put("message-id", e.getMessageId());
		}

		
		return map;
	}

}
