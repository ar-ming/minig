package org.minig.backend.xmpp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.aliasource.index.core.AbstractCrawler;
import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.MailIndexingParameters;

public class ChatCrawler extends AbstractCrawler {

	private String type;
	private IAccount ac;

	public ChatCrawler(MailIndexingParameters mip) {
		ac = mip.getAccount();
		type = ac.getUserId() + "/chat";
	}

	private void parse(Map<String, String> data, Document doc) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			DOMUtils.serialise(doc, out);
			String s = out.toString();
			data.put("data", s);
		} catch (TransformerException e1) {
		}

		DateFormat solrDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		Element root = doc.getDocumentElement();
		Date chatDate = new Date(Long.parseLong(root.getAttribute("ts")));
		data.put("date", solrDate.format(chatDate));

		NodeList nl = root.getElementsByTagName("item");
		StringBuilder body = new StringBuilder();
		Set<String> who = new HashSet<String>();

		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String from = e.getAttribute("from");
			who.add(from);
			String rawTxt = e.getTextContent();
			body.append(rawTxt);
			body.append("\n");
		}

		data.put("body", body.toString());
		StringBuilder subject = new StringBuilder("Chat with ");
		StringBuilder fromTo = new StringBuilder();
		Iterator<String> it = who.iterator();
		for (int i = 0; it.hasNext(); i++) {
			String s = it.next();
			if (i > 0) {
				subject.append(", ");
			}
			int idx = s.indexOf("@");
			if (idx > 0) {
				String cut = s.substring(0, idx);
				subject.append(cut);
				fromTo.append(cut);
				fromTo.append(s.substring(idx + 1));
			} else {
				subject.append(s);
			}
			fromTo.append(" ");
			fromTo.append(s);
		}
		data.put("subject", subject.toString());
		String fTo = fromTo.toString();
		data.put("from", fTo);
		data.put("to", fTo);
	}

	private String path() {
		return ac.getCache().getCachePath() + "/chats";
	}

	@Override
	protected Map<String, String> fetchData(String id) {
		logger.info("Should crawl " + id);

		Map<String, String> data = new HashMap<String, String>();
		data.put("id", id);
		data.put("is", "chat");
		data.put("type", ac.getUserId());
		data.put("has", "");
		data.put("filename", "");
		data.put("cc", "");
		data.put("in", "");

		Document doc = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(path() + "/" + id + ".xml");
			doc = DOMUtils.parse(in);
			parse(data, doc);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}

		return data;
	}

	@Override
	public String getType() {
		return type;
	}

}
