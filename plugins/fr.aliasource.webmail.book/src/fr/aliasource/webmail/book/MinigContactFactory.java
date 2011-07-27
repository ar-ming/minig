package fr.aliasource.webmail.book;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.book.Contact;

import fr.aliasource.webmail.book.impl.MinigContactImpl;

public class MinigContactFactory {

	private static final Log logger = LogFactory
			.getLog(MinigContactFactory.class);

	public <T> MinigContact createFrom(T source) {
		if (source.getClass().equals(Contact.class)) {
			return new MinigContactImpl((Contact) source);
		}
		logger
				.error("don't know how to create from class "
						+ source.getClass());
		return null;
	}

}
