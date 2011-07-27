package fr.aliasource.webmail.common.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.conversation.SetConversationReference;
import fr.aliasource.webmail.common.conversation.VersionnedList;

public final class ConversationsXmlSlicer extends DefaultHandler {

	private static final Log logger = LogFactory
			.getLog(ConversationsXmlSlicer.class);

	private VersionnedList<ConversationReference> ret;
	private int startIdx;
	private int endIdx;
	private int curIdx;
	private ConversationReference cr;
	private int len;
	private String currentMetaType;

	private StringBuilder storedString;
	private long wantedVersion;

	private List<String> ids;
	private int unread;

	public ConversationsXmlSlicer(VersionnedList<ConversationReference> ret,
			int page, int pageLength, long wantedVersion) {
		this.ids = new ArrayList<String>(2 * (Math.abs(pageLength) + 1));
		this.ret = ret;
		this.startIdx = (page - 1) * pageLength;
		this.endIdx = startIdx + pageLength - 1;
		this.curIdx = 0;
		this.len = 0;
		this.unread = 0;
		this.wantedVersion = wantedVersion;
		storedString = new StringBuilder();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (curIdx < startIdx) {
			return;
		}
		if (curIdx > endIdx) {
			return;
		}
		storedString.append(new String(ch, start, length));
	}

	@Override
	public void endDocument() throws SAXException {
		Iterator<ConversationReference> it = ret.iterator();
		for (int j = startIdx; j < endIdx && it.hasNext(); j++) {
			// logger.info("startIdx: " + startIdx + " endIdx: " + endIdx +
			// " j: "
			// + j);
			ConversationReference conv = it.next();
			if (j > 0) {
				conv.setPrev(ids.get(j - 1));
			}
			if (j < ids.size() - 1) {
				conv.setNext(ids.get(j + 1));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("conv")) {
			curIdx++;
		}

		if (curIdx < startIdx) {
			return;
		}
		if (curIdx > endIdx) {
			return;
		}
		if (localName.equals("title")) {
			cr.setTitle(storedString.toString());
		} else if (localName.equals("meta")) {
			cr.addMetadata(currentMetaType, storedString.toString());
		}
		storedString = new StringBuilder();
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (curIdx == 0 && localName.equals("references")) {
			long v = Long.parseLong(attributes.getValue("version"));
			ret.setVersion(v);
			if (v == wantedVersion) {
				curIdx = Integer.MAX_VALUE;
			}
		} else if (localName.equals("conv")) {
			len++;
			ids.add(attributes.getValue("id"));
		}

		if (curIdx > endIdx) {
			return;
		}
		if (curIdx < startIdx) {
			return;
		}

		// logger
		// .info("local: " + localName + " curIdx: " + curIdx
		// + " startIdx: " + startIdx + " end: " + endIdx
		// + " len: " + len);

		if (localName.equals("conv")) {
			String id = attributes.getValue("id");
			String f = id.substring(0, id.lastIndexOf('/'));
			cr = new SetConversationReference(attributes.getValue("id"), null, f);
			cr.setLastMessageDate(Long.parseLong(attributes.getValue("date")));
			cr.setRead("true".equals(attributes.getValue("read")));
			if (!cr.isRead()) {
				unread++;
			}

			cr.setStarred("true".equals(attributes.getValue("star")));
			cr.setAnswered("true".equals(attributes.getValue("answer")));
			cr.setHighPriority("true".equals(attributes.getValue("hp")));
			cr.setWithInvitation("true".equals(attributes
					.getValue("invitation")));
			cr.setWithAttachments("true".equals(attributes.getValue("attach")));
			ret.add(cr);
		} else if (localName.equals("a")) {
			cr.addParticipant(new Address(attributes.getValue("dn"), attributes
					.getValue("m")));
		} else if (localName.equals("mid")) {
			MessageId mid = new MessageId(Long.parseLong(attributes
					.getValue("id")));
			mid.setRead("true".equals(attributes.getValue("r")));
			mid.setStarred("true".equals(attributes.getValue("s")));
			mid.setAnswered("true".equals(attributes.getValue("a")));
			mid.setHighPriority("true".equals(attributes.getValue("hp")));
			mid.setSmtpId(attributes.getValue("sid"));
			cr.addMessage(mid);
		} else if (localName.equals("meta")) {
			currentMetaType = attributes.getValue("type");
		}
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		logger.error(e.getMessage());
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		logger.fatal(e.getMessage());
	}

	public int getLen() {
		return len;
	}

	public int getUnreadCount() {
		return unread;
	}
}
