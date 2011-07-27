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

package fr.aliasource.webmail.common.cache;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.cache.IDatastore;
import org.minig.cache.JDBCDataStore;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * Provides access to the cache used for the given account
 * 
 * @author tom
 * 
 */
public class AccountCache {

	private ConversationCache conversationCache;
	private SubscribedFolderCache subCache;
	private AvailableFolderCache availableFolderCache;
	private SignatureCache signatureCache;
	private String path;
	private CacheManager cacheManager;
	private AutoRefreshTask autoRefresh;
	private boolean started;
	private SummaryCache summaryCache;
	private Log logger;
	private IAccount account;
	private boolean resetUserCache;
	private IDatastore ds;
	private int cacheId;
	private Thread refreshThread;

	/**
	 * Creates a cache for the given account. The cache is xml-based and is
	 * periodicaly updated once start method has been called.
	 * 
	 * @param account
	 * @param accountConf
	 */
	public AccountCache(IAccount account) {
		logger = LogFactory.getLog(getClass());

		this.account = account;
	}

	public void init(AccountConfiguration accountConf) throws IOException,
			StoreException, InterruptedException {

		path = accountConf.getSetting(account.getUserId(),
				"account.caches.folder")
				+ File.separator
				+ "webmail_cache"
				+ File.separator
				+ account.getUserId().replaceFirst("@", "_at_");

		resetUserCache = false;
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
			resetUserCache = true;
		}

		ds = new JDBCDataStore();

		cacheId = ds.getCacheId(account.getUserId());
		if (resetUserCache) {
			ds.resetCache(cacheId);
		}
		logger.info("Cache id:" + cacheId);

		logger.info("ds created: " + ds);

		cacheManager = new CacheManager(this, account);

		// cacheId
		subCache = new SubscribedFolderCache(account);
		String delimiter = subCache.initDefaultFolders(accountConf);
		subCache.update();

		autoRefresh = new AutoRefreshTask(cacheManager);

		conversationCache = new ConversationCache(account);
		availableFolderCache = new AvailableFolderCache(account);
		signatureCache = new SignatureCache(account);
		summaryCache = new SummaryCache(account);

		account.setMailboxDelimiter(delimiter);
		if (resetUserCache) {
			logger.info(account.getUserId()
					+ " index reset required, clearing index...");
			account.getSearchDirector().clearIndex(account.getUserId());
			resetUserCache = false;
			logger.info(account.getUserId() + " index cleared.");
		}
		IFolder inbox = subCache.get(new IMAPFolder("INBOX"));
		cacheManager.refresh(inbox);
		refreshThread = new Thread(autoRefresh, "autorefresh-"
				+ account.getUserId());
	}

	public ConversationCache getConversationCache() {
		return conversationCache;
	}

	public SummaryCache getSummaryCache() {
		return summaryCache;
	}

	public String getCachePath() {
		return path;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * Start the autoRefresh task responsible for updating the cache every
	 * minute. The actual updating is done by the {@link CacheManager} class.
	 * 
	 * Do first cache update 10sec after call.
	 */
	public void start() {
		started = true;
		refreshThread.start();
	}

	/**
	 * stops periodic updating of caches.
	 */
	public void shutdown() {
		if (!started) {
			return;
		}
		while (!cacheManager.isFirstIndexingComplete()) {
			logger.info("waiting for first indexing before shutdown...");
		}
		autoRefresh.stop();
		try {
			refreshThread.join();
		} catch (InterruptedException e) {
		}
		logger.info("waiting for cache manager shutdown...");
		cacheManager.shutdown();
		started = false;
		logger.info("refresh completed, task cancelled.");

	}

	public SubscribedFolderCache getSubscribedFolderCache() {
		return subCache;
	}

	public AvailableFolderCache getAvailableFolderCache() {
		return availableFolderCache;
	}

	public SignatureCache getSignatureCache() {
		return signatureCache;
	}

	public IDatastore getDataStore() {
		return ds;
	}

	public int getCacheId() {
		return cacheId;
	}

}
