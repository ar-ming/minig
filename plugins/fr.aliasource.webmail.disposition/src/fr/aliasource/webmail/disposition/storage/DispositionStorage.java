package fr.aliasource.webmail.disposition.storage;

import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;

public interface DispositionStorage {

	void notificationSent(MailMessage message);
	
	void notificationDenied(MessageId messageId);
	
	 DispositionStatus getNotificationStatus(MessageId messageId);
	 
}
