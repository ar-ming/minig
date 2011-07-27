package fr.aliasource.webmail.disposition.response;

import java.util.Date;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.MessageBuilderFactory;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.address.parser.AddressBuilder;
import org.apache.james.mime4j.field.address.parser.ParseException;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.util.MimeUtil;

import com.google.common.collect.ImmutableMap;

import fr.aliasource.webmail.common.conversation.MailMessage;

public class ResponseBuilder {

	private final MailMessage orig;
	private final String userAddress;
	
	public ResponseBuilder(MailMessage orig, String userAddress) throws MimeException {
		this.orig = orig;
		this.userAddress = userAddress;
	}

	public Message build() throws MimeException {
		Message mm = buildBaseMessage();
		Multipart multipart = new MultipartImpl("report");
		multipart.addBodyPart(buildTextPart());
		multipart.addBodyPart(buildDispositionNotification());
		mm.setBody(multipart);
		return mm;
	}

	private Message buildBaseMessage() throws MimeException, ParseException {
		MessageBuilder messageBuilder = MessageBuilderFactory.newInstance().newMessageBuilder();
		Message mm = messageBuilder.newMessage();
		Header header = new Header();
		header.addField(Fields.contentType("multipart/report", 
				ImmutableMap.of("report-type", "disposition-notification",
						"boundary", MimeUtil.createUniqueBoundary())));
		header.addField(DefaultFieldParser.parse("References: " + orig.getSmtpId()));
		mm.setHeader(header);
		mm.setFrom(AddressBuilder.parseMailbox(userAddress));
		mm.setTo(AddressBuilder.parseMailbox(orig.getSender().getMail()));
		mm.setSubject("Return Receipt (displayed) - " + orig.getSubject());
		mm.setDate(new Date());
		return mm;
	}

	private BodyPart buildTextPart() {
		BodyPart bodyPart = new BodyPart();
		BodyFactory bodyFactory = new BodyFactory();
		String text = "This is a Return Receipt for the mail that you sent to " + userAddress + "\r\n" +
						"\r\n" +
						"Note: This Return Receipt only acknowledges that the message was displayed on the recipient's computer. " +
						"There is no guarantee that the recipient has read or understood the message contents.\r\n"; 
		TextBody textBody = bodyFactory.textBody(text);
		bodyPart.setBody(textBody, "text/plain", ImmutableMap.of("charset", "UTF-8"));
		bodyPart.setContentTransferEncoding(MimeUtil.ENC_8BIT);
		return bodyPart;
	}
	
	private BodyPart buildDispositionNotification() throws MimeException  {
		BodyPart bodyPart = new BodyPart();
		BodyFactory bodyFactory = new BodyFactory();
		String content = dispositionNotificationTextContent();
		TextBody textBody = bodyFactory.textBody(content);
		bodyPart.setBody(textBody, "message/disposition-notification");
		bodyPart.setContentDisposition(ContentDispositionField.DISPOSITION_TYPE_INLINE);
		bodyPart.setContentTransferEncoding(MimeUtil.ENC_7BIT);
		return bodyPart;
	}

	private String dispositionNotificationTextContent() throws MimeException {
		Header header = new Header();
		header.addField(DefaultFieldParser.parse("Reporting-UA: MiniG Webmail"));
		header.addField(DefaultFieldParser.parse("Final-Recipient:" + userAddress));
		header.addField(DefaultFieldParser.parse("Original-Message-ID:" + orig.getSmtpId()));
		header.addField(DefaultFieldParser.parse("Disposition: manual-action/MDN-sent-manually; displayed"));
		return header.toString();
	}


	
}
