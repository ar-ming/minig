package fr.aliasource.webmail.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;

public class MailSplitter {

	private static final Log logger = LogFactory.getLog(MailSplitter.class);
	
	/**
	 * joe.black@minig.org should return
	 * "joe.black@minig.org minig.org minig joe.black joe black"
	 * 
	 * @return a string suitable for indexing all parts of an email address
	 */
	public String getIndexedMailPart(Address ad) {
		String mail = ad.getMail();
		StringBuilder out = new StringBuilder(mail.length() * 6);

		out.append(mail).append(' '); // joe.black@minig.org

		String[] split = mail.split("@");
		out.append(split[1]).append(' '); // minig.org

		int idx = split[1].lastIndexOf(".");
		if (idx > 0) {
			out.append(split[1].substring(0, idx)).append(' '); // minig
		} else if (logger.isDebugEnabled()) {
			logger.debug("broken domain: " + split[1] + " in " + mail);
		}

		out.append(split[0]); // joe.black
		if (split[0].contains(".") || split[0].contains("-")) {
			out.append(' ').append(split[0].replace(".", " ").replace("-", " "));
		}

		return out.toString();
	}
}
