package fr.aliasource.webmail.book.impl;

import org.obm.sync.book.BookItemsParser;
import org.obm.sync.book.BookItemsWriter;
import org.obm.sync.book.Contact;
import org.w3c.dom.Element;

import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.book.MinigContactFactory;

public class ContactSerialiser {

	private BookItemsWriter writer;
	private BookItemsParser parser;
	private MinigContactFactory factory;

	public ContactSerialiser() {
		writer = new BookItemsWriter();
		parser = new BookItemsParser();
		factory = new MinigContactFactory();
	}

	public void serialize(Element contact, MinigContact mc) {
		writer.appendContact(contact, mc.adapt(Contact.class));
	}

	public MinigContact parse(Element contact) {
		Contact obm = parser.parseContact(contact);
		return factory.createFrom(obm);
	}

}
