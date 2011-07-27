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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.PKCS8Key;
import org.apache.james.jdkim.DKIMSigner;
import org.apache.james.jdkim.exceptions.FailException;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.MessageBuilderFactory;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.MailMessage;

/**
 * Prepares a MIME message
 * 
 * @author tom
 * 
 */
public class Mime4jFormatter {

	private Log logger;
	private IAccount account;
	private MessageBuilder builder;

	public Mime4jFormatter(IAccount account) {
		logger = LogFactory.getLog(getClass());
		this.account = account;
		try {
			this.builder = MessageBuilderFactory.newInstance()
					.newMessageBuilder();
		} catch (MimeException e) {
			logger.error("Cannot create message builder", e);
		}
	}

	public InputStream format(MailMessage m, SendParameters sp)
			throws IOException {
		return format(m, null, null, sp);
	}

	public InputStream format(MailMessage m, String messageId, String references, SendParameters sp)
			throws IOException {
		return format(m, messageId, references, sp, true, null);
	}

	/**
	 * Returns a stream to the mime formatted message
	 */
	public InputStream format(MailMessage m, String messageId, String references,
			SendParameters sp, boolean dkim, String dkimValue)
			throws IOException {

		// create mime4j message
		Message mm = builder.newMessage();

		// write them first as other setters will overwrite that
		setCustomHeaders(m, mm, messageId, references, sp, dkimValue);

		mm.setDate(m.getDate());
		mm.setSubject(m.getSubject());
		mm.setFrom(Mime4JHelper.toM4JAddress(m.getSender()));

		mm.setTo(Mime4JHelper.toM4JAddress(m.getTo()));
		mm.setCc(Mime4JHelper.toM4JAddress(m.getCc()));
		mm.setBcc(Mime4JHelper.toM4JAddress(m.getBcc()));

		Multipart multipart = new MultipartImpl("mixed");

		BodyFactory bodyFactory = new BodyFactory();

		String bodyValue = m.getBody().getValue("text/html");
		String typeSub = "html";
		if (bodyValue == null || "".equals(bodyValue)) {
			bodyValue = m.getBody().getValue("text/plain");
			typeSub = "plain";
			if (bodyValue == null) {
				bodyValue = " ";
			}
		}
		BodyPart part = createTextPart(bodyFactory, bodyValue.replace("\r\n",
				"\n").replace("\n", "\r\n"), typeSub);
		multipart.addBodyPart(part);

		if (hasAttachements(m)) {
			AttachmentManager am = account.getAttachementManager();

			for (String attachId : m.getAttachements().keySet()) {
				if (!am.isValidId(attachId)) {
					logger.warn("Invalid attachement id " + attachId
							+ ", skipping.");
					continue;
				}
				try {
					attach(multipart, bodyFactory, am, attachId);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}

		}

		mm.setMultipart(multipart);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mm.writeTo(out);
		mm.dispose();
		// logger.info("mail:\n"
		// + new String(out.toByteArray()).replace("\r\n", "CRLF\n"));
		logger.info("mime4j mail generation called by " + account.getUserId());

		byte[] data = out.toByteArray();
		InputStream read = new ByteArrayInputStream(data);

		PrivateKey pk = loadPK();
		String dkimHeader = null;

		if (dkim) {
			if (pk != null) {
				DKIMSigner signer = new DKIMSigner("v=1; s=selector; d="
						+ account.getDomain()
						+ "; h=from:to:subject; a=rsa-sha256; bh=; b=;", pk);
				try {
					dkimHeader = signer.sign(read);
				} catch (FailException e) {
					logger.error("DKIM signing failed");
				}
				if (dkimHeader != null) {
					read = format(m, messageId, references, sp, false, dkimHeader
							.substring("DKIM-Signature: ".length()));
				} else {
					read = new ByteArrayInputStream(data);
				}
			}
		}

		return read;
	}

	private PrivateKey loadPK() {
		PrivateKey privateKey = null;
		File f = new File("/etc/minig/dkim_pk.pem");
		if (!f.exists()) {
			return privateKey;
		}

		try {
			PKCS8Key pkcs8 = new PKCS8Key(new FileInputStream(f), null);
			privateKey = pkcs8.getPrivateKey();
		} catch (Exception e) {
			logger.error("Error loading private key for DKIM signing", e);
		}
		return privateKey;
	}

	private void attach(Multipart multipart, BodyFactory bodyFactory,
			AttachmentManager am, String attachId)
			throws FileNotFoundException, IOException {
		Map<String, String> meta = am.getMetadata(attachId);
		String fn = meta.get(AttachmentManager.META_FILENAME);
		String mime = meta.get(AttachmentManager.META_MIME);
		InputStream in = new FileInputStream(am.open(attachId));
		BodyPart attach = createBinaryPart(bodyFactory, in, mime, fn);
		multipart.addBodyPart(attach);
	}

	private void setCustomHeaders(MailMessage m, Message mm, String messageId, String references,
			SendParameters sp, String dkimValue) {
		Map<String, String> otherHeaders = new HashMap<String, String>();

		if (m.getSmtpId() != null) {
			otherHeaders.put("Message-ID", m.getSmtpId());
		}
		otherHeaders.put("X-Mailer", "MiniG Webmail");
		if (messageId != null) {
			otherHeaders.put("In-Reply-To", messageId);
		}
		if (references != null) {
			otherHeaders.put("References", references);
		}
		if (sp.isHighPriority()) {
			otherHeaders.put("X-Priority", "1");
		}
		if (sp.isDispositionNotification()) {
			otherHeaders.put("Disposition-Notification-To", Mime4JHelper.toM4JAddress(m.getSender()).getAddress());
		}
		
		if (dkimValue != null) {
			otherHeaders.put("DKIM-Signature", dkimValue);
		}

		Header h = new Header();
		for (String s : otherHeaders.keySet()) {
			String v = otherHeaders.get(s);
			h.addField(Mime4JHelper.field(s, v));
		}
		mm.setHeader(h);
	}

	/**
	 * Creates a text part from the specified string.
	 */
	private BodyPart createTextPart(BodyFactory bodyFactory, String text,
			String subtype) {
		// Use UTF-8 to encode the specified text
		TextBody body = bodyFactory.textBody(text, "UTF-8");

		// Create a text/plain body part
		BodyPart bodyPart = new BodyPart();
		bodyPart.setText(body, subtype);
		bodyPart.setContentTransferEncoding("quoted-printable");

		return bodyPart;
	}

	private BodyPart createBinaryPart(BodyFactory bodyFactory, InputStream in,
			String mimeType, String fileName) throws IOException {
		// Create a binary message body from the stream
		StorageProvider storageProvider = bodyFactory.getStorageProvider();
		Storage storage = storeStream(storageProvider, in);
		BinaryBody body = bodyFactory.binaryBody(storage);

		// Create a body part with the correct MIME-type and transfer encoding
		BodyPart bodyPart = new BodyPart();
		bodyPart.setBody(body, mimeType);
		if (!mimeType.endsWith("/rfc822")) {
			bodyPart.setContentTransferEncoding("base64");
		}

		// Specify a filename in the Content-Disposition header (implicitly sets
		// the disposition type to "attachment")
		QPHeaderEncoder encoder = new QPHeaderEncoder();
		bodyPart.setFilename(encoder.encode(fileName));

		return bodyPart;
	}

	/**
	 * Stores the specified stream in a Storage object.
	 */
	private static Storage storeStream(StorageProvider storageProvider,
			InputStream in) throws IOException {
		// An output stream that is capable of building a Storage object.
		StorageOutputStream out = storageProvider.createStorageOutputStream();

		FileUtils.transfer(in, out, true);

		// Implicitly closes the output stream and returns the data that has
		// been written to it.
		return out.toStorage();
	}

	private final boolean hasAttachements(MailMessage m) {
		boolean ret = false;
		for (String attId : m.getAttachements().keySet()) {
			if (account.getAttachementManager().isValidId(attId)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

}
