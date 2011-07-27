package fr.aliasource.webmail.common;

import org.minig.imap.IMAPHeaders;

import fr.aliasource.webmail.common.conversation.MailMessage;

public interface MailHeadersFilter {

	void filter(IMAPHeaders from, MailMessage to, IAccount account);
	
}
