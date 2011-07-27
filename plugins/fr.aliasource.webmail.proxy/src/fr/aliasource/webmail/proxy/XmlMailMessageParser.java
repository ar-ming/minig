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

package fr.aliasource.webmail.proxy;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.conversation.BodyFormattingRegistry;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.conversation.MailMessage;

public class XmlMailMessageParser {

	private Log logger;

	public XmlMailMessageParser() {
		logger = LogFactory.getLog(getClass());
	}

	public MailMessage parse(String xmlMessage) {
		MailMessage m = null;
		try {
			Document doc = DOMUtils.parse(new ByteArrayInputStream(xmlMessage
					.getBytes()));
			if (logger.isDebugEnabled()) {
				DOMUtils.logDom(doc);
			}
			Calendar cal = Calendar.getInstance();
			Element root = doc.getDocumentElement();
			Element mElement = DOMUtils.getUniqueElement(root, "m");
			m = parseMessage(cal, mElement);
		} catch (Exception e) {
			logger.error("cannot parse xml message", e);
		}

		return m;
	}

	private MailMessage parseMessage(Calendar cal, Element me) {
		String subject = DOMUtils.getElementText(me, "subject");
		String messageId = DOMUtils.getElementText(me, "messageId");
		MailBody body = parseMailBody(me);
		cal.setTimeInMillis(Long.parseLong(me.getAttribute("date")));
		Date d = cal.getTime();
		String[][] sender = DOMUtils.getAttributes(me, "from", new String[] {
				"addr", "displayName" });
		Address from = new Address(sender[0][1], sender[0][0]);
		Element recip = DOMUtils.getUniqueElement(me, "to");
		String[][] ra = DOMUtils.getAttributes(recip, "r", new String[] {
				"addr", "displayName" });
		List<Address> to = getAddresses(ra);

		recip = DOMUtils.getUniqueElement(me, "cc");
		ra = DOMUtils.getAttributes(recip, "r", new String[] { "addr",
				"displayName" });
		List<Address> cc = getAddresses(ra);

		recip = DOMUtils.getUniqueElement(me, "bcc");
		ra = DOMUtils.getAttributes(recip, "r", new String[] { "addr",
				"displayName" });
		List<Address> bcc = getAddresses(ra);

		Element attachements = DOMUtils.getUniqueElement(me, "attachements");
		String[][] aIds = DOMUtils.getAttributes(attachements, "a",
				new String[] { "id" });
		Map<String, String> attachs = new HashMap<String, String>();
		for (int i = 0; i < aIds.length; i++) {
			attachs.put(aIds[i][0], "");
		}

		MailMessage cm = new MailMessage(subject, body, attachs, d, from, to,
				cc, bcc, null, null);
		cm.setSmtpId(messageId);
		return cm;
	}

	private List<Address> getAddresses(String[][] ra) {
		ArrayList<Address> to = new ArrayList<Address>(ra.length);
		for (int i = 0; i < ra.length; i++) {
			if (ra[i][0] != null && ra[i][0].length() > 0) {
				to.add(new Address(ra[i][1], ra[i][0]));
			}
		}
		return to;
	}

	private MailBody parseMailBody(Element me) {
		MailBody b = new MailBody();
		NodeList body = me.getElementsByTagName("body");
		int len = body.getLength();
		for (int i = 0; i < len; i++) {
			Element be = (Element) body.item(i);
			String type = "text/plain";
			if (be.hasAttribute("type")) {
				type = be.getAttribute("type");
			}
			b.addConverted(type, DOMUtils.getElementText(be));
		}
		if (!b.availableFormats().contains("text/plain")) {
			BodyFormattingRegistry bfr = new BodyFormattingRegistry(b);
			bfr.format();
		}
		return b;
	}
}
