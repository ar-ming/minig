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

package fr.aliasource.webmail.common;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.SieveClient;

import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.webmail.common.cache.AccountCache;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.MinigConnectionFactory;
import fr.aliasource.webmail.common.imap.StoreException;
import fr.aliasource.webmail.common.message.AttachmentManager;
import fr.aliasource.webmail.common.message.LoadMessagesCommand;
import fr.aliasource.webmail.common.message.StoreMessageCommand;
import fr.aliasource.webmail.pool.Pool;

public class IMAPAccount implements IAccount {

	private Pool<IStoreConnection> imapPool;
	private String smtp;
	private AccountCache cache;
	private String userId;
	private AttachmentManager attachementManager;
	private String mailboxDelimiter = "/";
	private List<ICloseListener> cls;
	private SearchDirector sd;
	private String password;
	private String domain;
	private String sieveHost;
	private boolean closing;

	private static final Log logger = LogFactory.getLog(IMAPAccount.class);

	public IMAPAccount(String imapHost, String smtpHost, String userId,
			String password, String domain, AccountConfiguration accountConf,
			String sieveHost) throws IOException, StoreException,
			InterruptedException {
		this.closing = false;
		this.sieveHost = sieveHost;
		this.userId = userId;
		this.password = password;
		this.domain = domain;
		if ((this.domain == null || this.domain.trim().length() == 0)
				&& userId.contains("@")) {
			this.domain = userId.split("@")[1];
		}
		this.cls = new LinkedList<ICloseListener>();

		MinigConnectionFactory cf = new MinigConnectionFactory(imapHost, 143,
				userId, password);
		IStoreConnection test = cf.createNewObject();
		if (test != null) {
			test.destroy();
		} else {
			throw new StoreException("Cannot connect to IMAP server at "
					+ imapHost);
		}
		this.imapPool = new Pool<IStoreConnection>(userId + "-pool", cf, 2);
		this.smtp = smtpHost;
		this.sd = new SearchDirector(new MailIndexingParameters(this));
		logger.info("Will now start caching & crawling thread");
		cache = new AccountCache(this);
		cache.init(accountConf);
		attachementManager = new AttachmentManager(this);
		sd.startCrawlers();
	}

	public AccountCache getCache() {
		return cache;
	}

	public IStoreConnection getStoreProtocol() {
		return imapPool.get();
	}

	public String getTransportHost() {
		return smtp;
	}

	public IListConversations getListConversations() {
		return cache.getConversationCache();
	}

	public String getUserId() {
		return userId;
	}

	public String getUserPassword() {
		return password;
	}

	public String getDomain() {
		return domain;
	}

	public void close() {
		if (closing) {
			return;
		}
		closing = true;
		try {
			for (ICloseListener icl : cls) {
				icl.accountClosed(this);
			}
			cache.shutdown();
			sd.stopCrawlers();
			imapPool.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IFindReference getFindReference() {
		return getCache().getConversationCache();
	}

	public ILoadMessages getLoadMessages() {
		return new LoadMessagesCommand(this);
	}

	public IStoreMessage getStoreMessage() {
		return new StoreMessageCommand(this);
	}

	@Override
	public AttachmentManager getAttachementManager() {
		return attachementManager;
	}

	@Override
	public ISetFlags getFlagsCommand() {
		return getCache().getConversationCache();
	}

	@Override
	public IMoveConversation getMoveConversation() {
		return new StoreMessageCommand(this);
	}

	@Override
	public IMoveMessage getMoveMessage() {
		return new StoreMessageCommand(this);
	}

	@Override
	public String getMailboxDelimiter() {
		return mailboxDelimiter;
	}

	@Override
	public void setMailboxDelimiter(String delimiter) {
		this.mailboxDelimiter = delimiter;
	}

	@Override
	public void addCloseListener(ICloseListener icl) {
		cls.add(icl);
	}

	@Override
	public SearchDirector getSearchDirector() {
		return sd;
	}

	@Override
	public IFolder getFolder(String conversationId) {
		int idx = conversationId.lastIndexOf('/');
		if (idx == -1) {
			logger.warn("Conversation id without '/': "+conversationId);
			return null;
		}
		return new IMAPFolder(conversationId.substring(0, idx));
	}

	@Override
	public Credentials getCredentials() {
		return new Credentials(userId, password);
	}

	@Override
	public FilterStore getFilterStore() {
		SieveClient sc = new SieveClient(sieveHost, 2000, getCredentials()
				.getUid(), getCredentials().getPassword());
		return new FilterStore(sc);
	}

	@Override
	public void setClientChannel(IClientChannel pushHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean equals(Object obj) {
		return getUserId().equals(((IAccount) obj).getUserId());
	}

	@Override
	public int hashCode() {
		return getUserId().hashCode();
	}

	@Override
	public String getEmailAddress() {
		if (userId.contains("@")) {
			return userId;
		} else {
			return userId + "@" + domain;
		}
	}
	
}
