package fr.aliasource.webmail.disposition;

import java.util.List;

import org.minig.imap.Address;
import org.minig.imap.IMAPHeaders;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.MailHeadersFilter;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.disposition.storage.DispositionStatus;
import fr.aliasource.webmail.disposition.storage.DispositionStorage;
import fr.aliasource.webmail.disposition.storage.DispositionStorageCache;

public class DispositionHeadersFilter implements MailHeadersFilter {

	@Override
	public void filter(IMAPHeaders from, MailMessage to, IAccount account) {
		List<Address> dispositionNotificationTo = from.getDispositionNotification();
		if (dispositionNotificationTo != null && !dispositionNotificationTo.isEmpty()) {
			DispositionStorage storage = new DispositionStorageCache(account);
			DispositionStatus notificationStatus = storage.getNotificationStatus(new MessageId(from.getUid()));
			if (notificationStatus == null || notificationStatus.isPending()) {
				to.setDispositionNotificationTo(dispositionNotificationTo);
			}
		}
	}

}
