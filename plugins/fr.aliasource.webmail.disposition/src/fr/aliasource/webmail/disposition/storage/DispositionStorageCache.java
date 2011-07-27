package fr.aliasource.webmail.disposition.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.minig.cache.JDBCCacheCallback;
import org.minig.cache.JDBCCacheTemplate;
import org.minig.cache.RowMapper;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.AccountCache;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.disposition.response.ResponseBuilder;

public class DispositionStorageCache implements DispositionStorage {

	private static final String TABLE_NAME = "notification";
	private static final Log logger = LogFactory.getLog(DispositionStorageCache.class);
	private JDBCCacheTemplate cacheTemplate;
	private final IAccount account;
	
	public DispositionStorageCache(IAccount account) {
		this.account = account;
		AccountCache cache = account.getCache();
		this.cacheTemplate = new JDBCCacheTemplate(cache.getDataStore(), cache.getCacheId());
	}
	
	
	@Override
	public void notificationSent(final MailMessage message) {
		try {
			sendDispositionNotification(message);
			setStatus(message.getUid(), DispositionStatus.Sent);
		} catch (IOException e) {
			logger.error("error sending disposition notification", e);
		} catch (SMTPException e) {
			logger.error("error sending disposition notification", e);
		} catch (MimeException e) {
			logger.error("error sending disposition notification", e);
		}
	}


	private void sendDispositionNotification(final MailMessage message) throws IOException, SMTPException, MimeException {
	
		String from = account.getEmailAddress();
		ResponseBuilder responseBuilder = new ResponseBuilder(message, from);
		Message response = responseBuilder.build();

		SMTPProtocol smtp = new SMTPProtocol(account.getTransportHost());
		try {
			smtp.openPort();
			smtp.ehlo(InetAddress.getLocalHost());
			smtp.mail(new Address(from));
			for (org.apache.james.mime4j.dom.address.Address addr: response.getTo()) {
				smtp.rcpt(new Address(addr.getEncodedString()));
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.writeTo(out);
			smtp.data(new ByteArrayInputStream(out.toByteArray()));
		} finally {
			smtp.quit();
		}
	}

	@Override
	public void notificationDenied(MessageId messageId) {
		setStatus(messageId.getImapId(), DispositionStatus.Denied);
	}

	private void setStatus(final long messageId, DispositionStatus status) {
		DispositionStatus currentStatus = selectCurrentValue(messageId);
		if (currentStatus != null) {
			updateStatus(messageId, status);
		} else {
			insertStatus(messageId, status);
		}
	}

	@Override
	public DispositionStatus getNotificationStatus(MessageId messageId) {
		return selectCurrentValue(messageId.getImapId());
	}
	
	private void updateStatus(final long messageId, final DispositionStatus status) {
		cacheTemplate.execute(new JDBCCacheCallback() {
			
			@Override
			public void execute(Connection con, int cacheId) throws SQLException {
				String query = "UPDATE " + TABLE_NAME + 
								" SET status = ? WHERE mid = ? AND minig_cache = ?";
				PreparedStatement st = con.prepareStatement(query);
				st.setInt(1, status.getDbValue());
				st.setLong(2, messageId);
				st.setInt(3, cacheId);
				st.execute();
			}
		});
	}

	private void insertStatus(final long messageId, final DispositionStatus status) {
		cacheTemplate.execute(new JDBCCacheCallback() {
			
			@Override
			public void execute(Connection con, int cacheId) throws SQLException {
				String query = "INSERT INTO " + TABLE_NAME + 
								" (status, mid, minig_cache) VALUES (?, ?, ?)";
				PreparedStatement st = con.prepareStatement(query);
				st.setInt(1, status.getDbValue());
				st.setLong(2, messageId);
				st.setInt(3, cacheId);
				st.execute();
			}
		});
	}


	private DispositionStatus selectCurrentValue(long messageId) {
		return cacheTemplate.queryForObject(
				"SELECT status from " + TABLE_NAME + " WHERE mid = ?", 
				new Long[] {messageId}, 
				new RowMapper<DispositionStatus>() {

					@Override
					public DispositionStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
						int statusAsInt = rs.getInt(1);
						return DispositionStatus.fromDbValue(statusAsInt);
					}
				});
	}

}
