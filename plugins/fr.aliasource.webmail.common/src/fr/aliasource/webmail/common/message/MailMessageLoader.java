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

package fr.aliasource.webmail.common.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.command.parser.HeadersParser;
import org.minig.imap.impl.Base64;
import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.MimePart;
import org.minig.imap.mime.MimeTree;
import org.minig.mime.QuotedPrintableDecoderInputStream;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.webmail.common.Activator;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.MailHeadersFilter;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * Creates a {@link MailMessage} from a {@link MessageId}.
 * 
 * @author tom
 * 
 */
public class MailMessageLoader {

	// private static final String[] HEADS_LOAD = new String[] { "Subject",
	// "From", "Date", "To", "Cc", "Bcc", "X-Mailer", "User-Agent",
	// "Message-ID" };

	private static final BodyParam formatFlowed = new BodyParam("format", "flowed");
	
	private IFolder f;
	private AttachmentManager atMgr;
	private boolean pickupPlain;
	private Map<String, String> attach;
	private int nbAttachments;
	private MimeTree tree;
	private InputStream invitation;
	private BodySelector bodySelector;

	private static final Log logger = LogFactory
			.getLog(MailMessageLoader.class);

	/**
	 * Creates a message loader with the given {@link AttachmentManager} for the
	 * given {@link IFolder}
	 * 
	 * @param atMgr
	 * @param f
	 */
	public MailMessageLoader(AttachmentManager atMgr, IFolder f) {
		this.f = f;
		this.atMgr = atMgr;
		this.pickupPlain = false;
		this.nbAttachments = 0;
		this.tree = null;
		this.bodySelector = new BodySelector(pickupPlain);
	}

	public void select(IStoreConnection protocol) throws IOException,
			StoreException {
		protocol.select(f.getName());
	}

	public MailMessage fetch(IAccount account, MessageId mid, IStoreConnection protocol,
			boolean bodyOnly) throws IOException, StoreException {
		Collection<Long> set = Arrays.asList(mid.getImapId());

		Collection<MimeTree> mts = protocol.uidFetchBodystructure(set);
		tree = mts.iterator().next();

		MailMessage mm = fetchOneMessage(tree, account, protocol, bodyOnly, false);

		// do load messages forwarded as attachments into the indexers, as it
		// ignores them
		if (!bodyOnly) {
			fetchQuotedText(tree, mm, account, protocol, bodyOnly);
			fetchForwardMessages(tree, mm, account, protocol, bodyOnly);
		}
		return mm;
	}

	private void fetchQuotedText(MimeTree tree, MailMessage mailMessage,
			IAccount account, IStoreConnection protocol, boolean bodyOnly) throws IOException,
			StoreException {
		Iterator<MimePart> it = tree.getChildren().iterator();
		while (it.hasNext()) {
			MimePart m = it.next();
			if (m.getMimeType() != null) {
				fetchFlowed(mailMessage, account, protocol, bodyOnly, m);
			} else {
				Iterator<MimePart> mIt = m.getChildren().iterator();
				while (mIt.hasNext()) {
					MimePart mp = mIt.next();
					if (mp.getMimeType() != null) {
						fetchFlowed(mailMessage, account, protocol, bodyOnly, mp);
					}
				}
			}
		}
	}

	private void fetchFlowed(MailMessage mailMessage,
			IAccount account, IStoreConnection protocol, boolean bodyOnly, MimePart m)
			throws IOException, StoreException {
		if (formatFlowed.equals(m.getBodyParam("format"))) {
			MailMessage mm = fetchOneMessage(m, account, protocol, bodyOnly, false);
			if (!mailMessage.getBody().equals(mm.getBody())) {
				for (String format : mm.getBody().availableFormats()) {
					if (mm.getBody().getValue(format) != null) {
						mailMessage.getBody().addMailPart(format,
								mm.getBody().getValue(format));
					}
				}
			}
		}
	}

	private void fetchForwardMessages(MimePart t, MailMessage mailMessage,
			IAccount account, IStoreConnection protocol, boolean bodyOnly) throws IOException,
			StoreException {

		Iterator<MimePart> it = t.getChildren().iterator();
		while (it.hasNext()) {
			MimePart m = it.next();
			if (m.getMimeType() != null) {
				fetchNested(mailMessage, account, protocol, bodyOnly, m);
			} else {
				Iterator<MimePart> mIt = m.getChildren().iterator();
				while (mIt.hasNext()) {
					MimePart mp = mIt.next();
					if (mp.getMimeType() != null) {
						fetchNested(mailMessage, account, protocol, bodyOnly, mp);
					}
				}
			}
		}
	}

	private void fetchNested(MailMessage mailMessage,
			IAccount account, IStoreConnection protocol, boolean bodyOnly, MimePart m)
			throws IOException, StoreException {
		if (m.getFullMimeType().equalsIgnoreCase("message/rfc822")) {
			MailMessage mm = fetchOneMessage(m, account, protocol, bodyOnly, true);
			mailMessage.addForwardMessage(mm);
			fetchForwardMessages(m, mm, account, protocol, bodyOnly);
		}
	}

	private MailMessage fetchOneMessage(MimePart mimePart, 
			IAccount account, IStoreConnection protocol, boolean bodyOnly, boolean findForward)
			throws IOException, StoreException {
		attach = new HashMap<String, String>();
		MimePart chosenPart = mimePart;
		if (chosenPart.getMimeType() == null
				|| chosenPart.getFullMimeType().equals("message/rfc822")) {
			chosenPart = bodySelector.findBodyTextPart(mimePart, findForward);
		}

		IMAPHeaders h = fetchHeaders(mimePart, protocol);

		MailBody body = getMailBody(chosenPart, protocol);
		if (chosenPart == null) {
			extractAttachments(mimePart, protocol, bodyOnly);
		} else {
			extractAttachments(chosenPart, protocol, bodyOnly, false);
		}
		MailMessage mm = new MailMessage();
		mm.setBody(body);
		mm.setAttachements(attach);
		mm.setSender(h.getFrom());
		mm.setDate(h.getDate());
		mm.setSubject(h.getSubject());
		mm.setHeaders(h.getRawHeaders());
		mm.setCc(h.getCc());
		mm.setTo(h.getTo());
		mm.setBcc(h.getBcc());
		mm.setSmtpId(h.getRawHeader("Message-ID"));
		mm.setUid(tree.getUid());
		mm.setInvitation(invitation);

		for (MailHeadersFilter filter: Activator.getDefault().getMailHeadersFilters()) {
			filter.filter(h, mm, account);
		}
		
		return mm;
	}

	private IMAPHeaders fetchHeaders(MimePart mimePart,
			IStoreConnection protocol) throws IOException, StoreException {
		String messageAddress = mimePart.getAddress();
		String part = null;
		if (messageAddress.isEmpty()) {
			part = "HEADER";
		} else {
			part = messageAddress + ".HEADER";
		}
		InputStream is = protocol.uidFetchPart(tree.getUid(), part);
		InputStreamReader reader = new InputStreamReader(is, getHeaderCharsetDecoder(mimePart));
		Map<String, String> rawHeaders = HeadersParser.parseRawHeaders(reader);
		IMAPHeaders h = new IMAPHeaders();
		h.setRawHeaders(rawHeaders);
		h.setUid(tree.getUid());
		return h;
	}

	private MailBody getMailBody(MimePart chosenPart, IStoreConnection protocol)
			throws IOException, StoreException {

		MailBody mb = new MailBody();
		if (chosenPart == null) {
			mb.addConverted("text/plain", "");
		} else {
			InputStream bodyText = protocol.uidFetchPart(tree.getUid(),
					chosenPart.getAddress());

			BodyParam charsetParam = chosenPart.getBodyParam("charset");
			String charsetName = computeSupportedCharset(charsetParam);

			byte[] rawData = extractPartData(chosenPart, bodyText);
			String partText = new String(rawData, charsetName);

			mb.addConverted(chosenPart.getFullMimeType(), partText);
			if (logger.isDebugEnabled()) {
				logger.debug("Added part " + chosenPart.getFullMimeType()
						+ "\n" + partText + "\n------");
			}
		}
		return mb;
	}

	private String computeSupportedCharset(BodyParam charsetParam) {
		if (charsetParam != null) {
			try {
				String charsetName = charsetParam.getValue();
				if (Charset.isSupported(charsetName)) {
					return charsetName;
				}
			} catch (IllegalCharsetNameException e) {
			} catch (IllegalArgumentException e) {
			}
		}
		return "utf-8";
	}

	private byte[] extractPartData(MimePart chosenPart, InputStream bodyText)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.transfer(bodyText, out, true);
		byte[] rawData = out.toByteArray();

		if ("QUOTED-PRINTABLE".equals(chosenPart.getContentTransfertEncoding())) {
			out = new ByteArrayOutputStream();
			InputStream in = new QuotedPrintableDecoderInputStream(
					new ByteArrayInputStream(rawData));
			FileUtils.transfer(in, out, true);
			rawData = out.toByteArray();
		} else if ("BASE64".equals(chosenPart.getContentTransfertEncoding())) {
			rawData = Base64.decodeToArray(new String(rawData));
		}
		return rawData;
	}

	private void extractAttachments(MimePart mimePart,
			IStoreConnection protocol, boolean bodyOnly, boolean isInvit)
			throws IOException, StoreException {
		if (mimePart != null) {
			MimePart parent = mimePart.getParent();
			if (parent != null) {
				boolean inv = false;
				for (MimePart mp : parent.getChildren()) {
					inv = mp.isInvitation();
					extractAttachmentData(mp, bodyOnly, protocol, isInvit
							|| inv);
				}
				if (parent.getMimeType() == null
						&& parent.getMimeSubtype() == null) {
					extractAttachments(parent, protocol, bodyOnly, inv);
				}
			}
		}

	}

	private void extractAttachments(MimePart mimePart,
			IStoreConnection protocol, boolean bodyOnly) throws IOException,
			StoreException {
		if (mimePart != null) {
			for (MimePart mp : mimePart.getChildren()) {
				extractAttachmentData(mp, bodyOnly, protocol, mp.isInvitation());
			}
		}

	}

	private void extractAttachmentData(MimePart mp, boolean bodyOnly,
			IStoreConnection protocol, boolean isInvitation)
			throws IOException, StoreException {
		long uid = tree.getUid();

		String id = "at_" + uid + "_" + (nbAttachments++);
		File out = null;
		if (!bodyOnly) {
			out = atMgr.open(id);
			if (!out.exists()) {
				InputStream part = protocol.uidFetchPart(uid, mp.getAddress());
				byte[] data = extractPartData(mp, part);
				FileUtils.transfer(new ByteArrayInputStream(data),
						new FileOutputStream(out), true);
			}
		}
		try {
			BodyParam name = mp.getBodyParam("name");
			if (!bodyOnly) {
				atMgr.storeMetadata(id, mp, out.length());
			}
			if (name != null && name.getValue() != null) {
				if (isInvitation && name.getValue().contains(".ics")
						&& !bodyOnly) {
					invitation = new FileInputStream(atMgr.open(id));
				}
				attach.put(id, name.getValue());
			} else if (mp.getContentId() != null
					&& !mp.getContentId().equalsIgnoreCase("nil")) {
				attach.put(id, mp.getContentId());
			} else if (isInvitation) {
				invitation = protocol.uidFetchPart(tree.getUid(), mp
						.getAddress());
			}
		} catch (Exception e) {
			logger.error("Error storing metadata for " + id, e);
		}
	}

	public void setPickupPlain(boolean pickupPlain) {
		this.pickupPlain = pickupPlain;
		this.bodySelector = new BodySelector(pickupPlain);
	}

	/**
	 * Tries to return a suitable {@link Charset} to decode the headers
	 * 
	 * @param part
	 * @return
	 */
	private Charset getHeaderCharsetDecoder(MimePart part) {
		String encoding = part.getContentTransfertEncoding();
		if (encoding == null) {
			return Charset.forName("utf-8");
		} else if (encoding.equalsIgnoreCase("8bit")) {
			return Charset.forName("iso-8859-1");
		} else {
			try {
				return Charset.forName(encoding);
			} catch (UnsupportedCharsetException uee) {
				if (logger.isDebugEnabled()) {
					logger.debug("illegal charset: " + encoding
							+ ", defaulting to utf-8");
				}
				return Charset.forName("utf-8");
			}
		}
	}
}
