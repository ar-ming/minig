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

package fr.aliasource.webmail.proxy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.SortedMap;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.utils.FileUtils;
import fr.aliasource.utils.StringUtils;
import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.conversation.VersionnedList;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.proxy.api.Completion;
import fr.aliasource.webmail.proxy.api.IResponder;

/**
 * Implements XML serialisation of IMAP proxy responses
 * 
 * @author tom
 * 
 */
public final class ResponderImpl implements IResponder {

	private HttpServletResponse resp;
	private Log logger;

	public ResponderImpl(HttpServletResponse resp) {
		this.resp = resp;
		logger = LogFactory.getLog(getClass());
	}

	public void sendToken(String token) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/login", "token");
			doc.getDocumentElement().setAttribute("value", token);
			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendDom(Document doc) throws TransformerException, IOException {
		resp.setContentType("text/xml;charset=UTF-8");
		DOMUtils.serialise(doc, resp.getOutputStream());
	}

	public void denyAccess(String cause) {
		logger.warn("Denying access");
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		try {
			resp.getWriter().println("Access denied: " + cause);
		} catch (IOException e) {
			logger.error("Cannot write response");
		}
	}

	public void sendFolderList(List<IFolder> folders) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/folder_list", "folderList");
			Element root = doc.getDocumentElement();
			for (IFolder f : folders) {
				Element fe = DOMUtils.createElement(root, "folder");
				fe.setAttribute("displayName", f.getDisplayName());
				fe.setAttribute("name", f.getName());
				fe.setAttribute("subscribed", String.valueOf(f.isSubscribed()));
				fe.setAttribute("shared", String.valueOf(f.isShared()));
			}
			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendConversationsPage(
			ConversationReferenceList listConversations) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/conversations_page",
					"conversationsPage");
			Element root = doc.getDocumentElement();
			root.setAttribute("fullLength",
					"" + listConversations.getFullLength());
			VersionnedList<ConversationReference> page = listConversations
					.getPage();
			root.setAttribute("version", "" + page.getVersion());

			if (logger.isDebugEnabled()) {
				logger.debug(page.size() + " conversations.");
			}

			for (ConversationReference cr : page) {
				appendConversation(root, cr);
			}

			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void appendConversation(Element root, ConversationReference cr) {
		Element ce = DOMUtils.createElement(root, "cr");
		ce.setAttribute("id", cr.getId());
		ce.setAttribute("lastMessageDate", "" + cr.getLastMessageDate());
		ce.setAttribute("folder", cr.getSourceFolder());
		ce.setAttribute("read", "" + cr.isRead());
		ce.setAttribute("attach", "" + cr.isWithAttachments());
		ce.setAttribute("invitation", "" + cr.isWithInvitation());
		ce.setAttribute("star", "" + cr.isStarred());
		ce.setAttribute("answer", "" + cr.isAnswered());
		ce.setAttribute("hp", "" + cr.isHighPriority());

		if (cr.getPrev() != null) {
			ce.setAttribute("prev", cr.getPrev());
		}
		if (cr.getNext() != null) {
			ce.setAttribute("next", cr.getNext());
		}

		if (cr.getTitle() != null) {
			DOMUtils.createElementAndText(ce, "title", cr.getTitle());
		} else {
			DOMUtils.createElementAndText(ce, "title",
					"[No conversation topic]");
			if (logger.isDebugEnabled()) {
				logger.debug("Invalid conversation title for messages : ");
				for (MessageId mid : cr.getMessageIds()) {
					logger.debug("    * uid: " + mid.getImapId()
							+ " has an invalid title (" + cr.getSourceFolder()
							+ ")");
				}
			}
		}

		Element ps = DOMUtils.createElement(ce, "participants");
		for (Address a : cr.getParticipants()) {
			if (a != null && a.getMail() != null) {
				Element p = DOMUtils.createElement(ps, "p");
				String ma = StringUtils.stripAddressForbiddenChars(a.getMail());
				if (a.getDisplayName() != null) {
					p.setAttribute("displayName", a.getDisplayName());
				} else {
					p.setAttribute("displayName", ma);
				}
				p.setAttribute("addr", ma);
			}
		}

		Element mids = DOMUtils.createElement(ce, "mids");
		for (MessageId id : cr.getMessageIds()) {
			Element m = DOMUtils.createElement(mids, "m");
			m.setAttribute("id", "" + id.getImapId());
		}

		Element metas = DOMUtils.createElement(ce, "metas");
		for (String mKey : cr.getMetadata().keySet()) {
			String val = cr.getMetadata().get(mKey);
			if (val != null) {
				Element m = DOMUtils.createElementAndText(metas, "meta", val);
				m.setAttribute("type", mKey);
			}
		}
	}

	public void sendConversation(ConversationReference cr) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/conversations_page",
					"conversationsPage");
			Element root = doc.getDocumentElement();
			appendConversation(root, cr);
			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendMessages(MailMessage[] ms) {
		sendMessages(null, ms);
	}

	public void sendMessages(ConversationReference cr, MailMessage[] ms) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/messages", "messages");
			Element root = doc.getDocumentElement();

			if (cr != null) {
				DOMUtils.createElementAndText(root, "title", cr.getTitle());
				root.setAttribute("read", "" + cr.isRead());
			}

			for (int i = 0; i < ms.length; i++) {
				appendMessage(root, "m", ms[i]);
			}
			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void appendMessage(Element parentElement, String elementName,
			MailMessage m) {

		if (m == null) {
			logger.error("cannot append null message");
			return;
		}

		Element me = DOMUtils.createElement(parentElement, elementName);
		messageDetail(m, me);

		if (m.getForwardMessage() != null) {
			for (MailMessage mm : m.getForwardMessage()) {
				appendMessage(me, "fwd", mm);
			}
		}

	}

	/**
	 * @param m
	 * @param me
	 */
	private void messageDetail(MailMessage m, Element me) {

		me.setAttribute("date", ""
				+ (m.getDate() != null ? m.getDate().getTime() : 0));
		me.setAttribute("read", "" + m.isRead());
		me.setAttribute("starred", "" + m.isStarred());
		me.setAttribute("answered", "" + m.isAnswered());
		me.setAttribute("hp", "" + m.isHighPriority());
		me.setAttribute("uid", "" + m.getUid());
		String sub = m.getSubject();
		if (sub == null) {
			sub = "[Empty Subject]";
		}
		DOMUtils.createElementAndText(me, "subject", sub);

		Address sender = m.getSender();
		Element from = DOMUtils.createElement(me, "from");
		if (sender != null) {
			String ma = sender.getMail().replace("<", "").replace(">", "");
			from.setAttribute("addr", ma);
			from.setAttribute("displayName", "" + sender.getDisplayName());
		} else {
			from.setAttribute("addr", "from@is.not.set");
			from.setAttribute("displayName", "From not set");
		}

		addRecipients(DOMUtils.createElement(me, "to"), m.getTo());
		addRecipients(DOMUtils.createElement(me, "cc"), m.getCc());
		addRecipients(DOMUtils.createElement(me, "bcc"), m.getBcc());

		Element attachements = DOMUtils.createElement(me, "attachements");
		for (String attach : m.getAttachements().keySet()) {
			Element ae = DOMUtils.createElement(attachements, "a");
			ae.setAttribute("id", attach);
		}
		Element invitation = DOMUtils.createElement(me, "invitation");
		if (m.getInvitation() != null) {
			invitation.setTextContent("true");
		} else {
			invitation.setTextContent("false");
		}

		if (!m.getDispositionNotificationTo().isEmpty()) {
			Element dispositionNotification = DOMUtils.createElement(me, "disposition-notification");
			addRecipients(dispositionNotification, m.getDispositionNotificationTo());
		}
		
		if (m.getBody() != null) {
			formatBody(m, me);
		}
	}

	private void addRecipients(Element recipients,
			List<org.minig.imap.Address> recip) {
		if (recip != null) {
			for (org.minig.imap.Address a : recip) {
				Element p = DOMUtils.createElement(recipients, "r");
				p.setAttribute("displayName", a.getDisplayName());
				String ma = a.getMail().replace("<", "").replace(">", "");
				p.setAttribute("addr", ma);
			}
		}
	}

	private void formatBody(MailMessage m, Element me) {
		for (String format : m.getBody().availableFormats()) {
			Element body = DOMUtils.createElementAndText(me, "body", m
					.getBody().getValue(format));
			body.setAttribute("type", format);
		}
	}

	public void sendCompletions(List<Completion> possibleCompletions) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/completion", "completions");
			Element root = doc.getDocumentElement();
			for (Completion c : possibleCompletions) {
				Element comp = DOMUtils.createElement(root, "c");
				comp.setAttribute("d", c.getDisplayName());
				comp.setAttribute("v", c.getValue());
			}
			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendConversationIds(String[] convIds) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/conv_ids", "convIds");
			Element root = doc.getDocumentElement();
			for (String cid : convIds) {
				Element comp = DOMUtils.createElement(root, "c");
				comp.setAttribute("id", cid);
			}
			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendSummary(SortedMap<IFolder, Integer> summary) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/folder_list", "folderList");
			Element root = doc.getDocumentElement();
			for (IFolder f : summary.keySet()) {
				Element fe = DOMUtils.createElement(root, "folder");
				fe.setAttribute("displayName", f.getDisplayName());
				fe.setAttribute("name", f.getName());
				fe.setAttribute("unread", summary.get(f).toString());
			}

			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendStream(InputStream in) {
		try {
			FileUtils.transfer(in, resp.getOutputStream(), true);
		} catch (IOException e) {
			logger.error("Error responding with stream", e);
		}
	}

	@Override
	public void sendError(String string) {
		try {
			Document doc = DOMUtils.createDoc("http://minig.org/xsd/error",
					"error");
			Element root = doc.getDocumentElement();

			DOMUtils.createElementAndText(root, "reason", string);

			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendString(String string) {
		try {
			Document doc = DOMUtils.createDoc("http://minig.org/xsd/string",
					"string");
			Element root = doc.getDocumentElement();

			DOMUtils.createElementAndText(root, "value", string);

			sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendNothingChanged() {
		resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
	}
}
