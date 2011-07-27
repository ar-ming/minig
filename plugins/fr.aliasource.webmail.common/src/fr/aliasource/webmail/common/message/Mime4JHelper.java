package fr.aliasource.webmail.common.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.Field;
import org.apache.james.mime4j.field.address.parser.AddressBuilder;
import org.apache.james.mime4j.field.address.parser.ParseException;
import org.apache.james.mime4j.util.MimeUtil;

public final class Mime4JHelper {

	public static Field field(final String name, String val) {
		int used = name.length() + 2;
		final String value = MimeUtil.fold(EncoderUtil.encodeIfNecessary(val,
				EncoderUtil.Usage.TEXT_TOKEN, used), 0);

		final String toParse = name + ": " + value;
		return new Field() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getBody() {
				return value;
			}

			@Override
			public void writeTo(OutputStream out) throws IOException {
				byte[] b = toParse.getBytes();
				out.write(b, 0, b.length);
			}
		};
	}

	public static Mailbox toM4JAddress(org.minig.imap.Address addr) {
		if (addr == null || addr.getMail() == null) {
			return null;
		}
		try {
			return AddressBuilder
					.parseMailbox((addr.getDisplayName() != null ? addr
							.getDisplayName() : "")
							+ " <" + addr.getMail() + ">");
		} catch (Throwable t) {
			try {
				return AddressBuilder.parseMailbox(addr.getMail());
			} catch (ParseException e) {
				return null;
			}
		}
	}

	public static Mailbox[] toM4JAddress(List<org.minig.imap.Address> addresses) {
		Set<Mailbox> mbs = new HashSet<Mailbox>();
		for (org.minig.imap.Address addr: addresses) {
			Mailbox mb = toM4JAddress(addr);
			if(mb !=null){
				mbs.add(mb);
			}
		}
		return mbs.toArray(new Mailbox[0]); 
	}
	
}
