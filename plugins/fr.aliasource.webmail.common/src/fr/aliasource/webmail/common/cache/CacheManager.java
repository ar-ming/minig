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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.uid.UIDCache;
import fr.aliasource.webmail.common.uid.UIDChanges;

/**
 * Implements cache update policy.
 * 
 * The following policy is used for cache updates : refresh the list of
 * subscribed folders, then refresh each folder.
 * 
 * Folder refresh loads a list of cached uids, then this list is compared to the
 * uid list on the server. Added & removed uids are calculated for an
 * incremental update of cached conversations.
 * 
 * 
 * @author tom
 * 
 */
public class CacheManager {

	private AccountCache cache;

	private IAccount account;
	private Log logger;
	private Map<IFolder, UIDCache> uidc;
	private Semaphore firstIndexingRoundLock;
	private Semaphore refreshLock;
	private int skipCount;

	public CacheManager(AccountCache cache, IAccount account) {
		this.logger = LogFactory.getLog(getClass());
		this.account = account;
		this.cache = cache;
		this.skipCount = 0;
		this.uidc = new HashMap<IFolder, UIDCache>();
		firstIndexingRoundLock = new Semaphore(1);
		refreshLock = new Semaphore(1);
		grabFirstLock();
	}

	private void grabRefreshLock() {
		try {
			refreshLock.acquire();
		} catch (InterruptedException e) {
		}
	}

	private void releaseRefreshLock() {
		refreshLock.release();
	}

	private void grabFirstLock() {
		logger.info("taking for first indexing round lock...");
		try {
			firstIndexingRoundLock.acquire();
		} catch (InterruptedException e) {
		}
		logger.info("first indexing round lock acquired.");
	}

	public UIDCache cache(IFolder f) {
		UIDCache ret = uidc.get(f);
		if (ret == null) {
			if (f.getId() == 0) {
				cache.getSubscribedFolderCache().get(f);
			}
			ret = new UIDCache(account, f);
			uidc.put(f, ret);
		}
		return ret;
	}

	public void refreshAll() {
		long time = System.currentTimeMillis();
		grabRefreshLock();
		SubscribedFolderCache fc = cache.getSubscribedFolderCache();
		SummaryCache sc = cache.getSummaryCache();
		try {
			List<IFolder> folders = fc.update();
			for (IFolder folder : folders) {
				if (folder.getName().equalsIgnoreCase("INBOX")
						|| ((skipCount % 5) == 0)) {
					refreshUnlocked(folder);
				}
			}
			skipCount++;
			sc.update();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		time = System.currentTimeMillis() - time;
		logger.info("[" + account.getUserId() + "] refreshAll in " + time
				+ "ms.");
		releaseRefreshLock();
		firstIndexingRoundLock.release();
	}

	public boolean isFirstIndexingComplete() {
		try {
			boolean get = firstIndexingRoundLock.tryAcquire(10,
					TimeUnit.SECONDS);
			firstIndexingRoundLock.release();
			return get;
		} catch (InterruptedException e) {
			throw new RuntimeException(
					"interrupted while waiting for first indexing round.");
		}
	}

	public void refresh(IFolder folder) throws InterruptedException {
		grabRefreshLock();
		refreshUnlocked(folder);
		refreshLock.release();
	}

	private void refreshUnlocked(IFolder folder) throws InterruptedException {
		long time = System.currentTimeMillis();
		UIDCache uc = cache(folder);
		long ct = System.currentTimeMillis();
		Set<Long> cached = uc.getCachedData();
		ct = System.currentTimeMillis() - ct;
		long upd = System.currentTimeMillis();
		Set<Long> current = uc.update();
		upd = System.currentTimeMillis() - upd;

		if (uc.flagResyncNeeded()) {
			time = System.currentTimeMillis() - time;
			logger.info("[" + account.getUserId() + "]: [flagSync] "
					+ folder.getName() + " changes found. (" + time
					+ "ms (loadCache: " + ct + "ms, updCache: " + upd + "ms))");
			
			cache.getConversationCache().fastUpdate(folder, current, new ArrayList<Long>(0));
		} else {
			UIDChanges sync = uc.computeChanges(cached, current);
			time = System.currentTimeMillis() - time;
			logger.info("[" + account.getUserId() + "]: " + folder.getName()
					+ " changes found. (" + time + "ms (loadCache: " + ct
					+ "ms, updCache: " + upd + "ms))");

			cache.getConversationCache().fastUpdate(folder, sync.getAdded(),
					sync.getRemoved());
		}

	}

	public void shutdown() {
		logger.info("Cache mgr shutdown, waiting for first lock...");
		grabFirstLock();
		logger.info("Cache mgr shutdown, waiting for refresh lock...");
		grabRefreshLock();
	}

	public void forgetFolders(HashSet<IFolder> toRemove) {
		for (IFolder f : toRemove) {
			uidc.remove(f);
		}
	}

}
