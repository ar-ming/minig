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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.cache.IDatastore;
import org.minig.cache.JDBCCacheTemplate;

import fr.aliasource.webmail.common.IAccount;

/**
 * Database based caching command implementation
 * 
 * @author tom
 * 
 * @param <W>
 *            the type of the cached data
 */
public abstract class DatabaseCache<W> extends AbstractCache<W> {

	protected Log logger;
	protected IDirectCommand<W> command;
	protected IDatastore ds;
	protected String tableName;
	protected boolean exists;

	private W memoryCache;
	protected boolean memoryCacheEnabled;

	protected DatabaseCache(IAccount account, IDirectCommand<W> command,
			String tableName) {
		super(account);
		this.logger = LogFactory.getLog(getClass());
		this.command = command;
		this.ds = account.getCache().getDataStore();
		this.tableName = tableName;
		exists = false;
		memoryCacheEnabled = false;
	}

	protected boolean existsImpl(String tableName) {
		long time = System.currentTimeMillis();
		boolean ret = getJDBCCacheTemplate().queryForInt(
				"SELECT 1 FROM " + tableName, null) > 0;
		time = System.currentTimeMillis() - time;
		if (logger.isDebugEnabled()) {
			logger.debug("[ " + account.getUserId() + "] existsImpl("
					+ tableName + ") done in " + time + "ms.");
		}
		return ret;
	}

	@Override
	protected boolean exists() {
		if (!exists) {
			exists = existsImpl(tableName);
		}
		return exists;
	}

	public W update() throws InterruptedException {
		W data = null;

		if (command != null) {
			long gdtTime = System.currentTimeMillis();
			try {
				data = command.getData();
			} catch (Exception e) {
				// this is crappy exception handling, but your logs will
				// be filled of exceptions
				logger.error("[" + account.getUserId() + "] getData() failed "
						+ e.getMessage(), e);
				return null;
			}
			gdtTime = System.currentTimeMillis() - gdtTime;

			long writeTime = System.currentTimeMillis();
			writeToCache(data);
			if (logger.isDebugEnabled()) {
				writeTime = System.currentTimeMillis() - writeTime;
				logger.debug("[" + account.getUserId()
						+ "] cache update time: dataLoad: " + gdtTime
						+ "ms, writeCache: " + writeTime + "ms");
			}
		}
		return data;
	}

	public W getCachedData() {
		if (exists()) {
			if (memoryCacheEnabled && memoryCache != null) {
				return memoryCache;
			} else {
				return loadFromCache();
			}
		} else {
			return null;
		}
	}

	public W getData() {
		if (!exists()) {
			try {
				update();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}

		if (memoryCacheEnabled && memoryCache != null) {
			return memoryCache;
		} else {
			return loadFromCache();
		}
	}

	@Override
	public void writeToCache(final W data) {
		writeCacheImpl(data);
		if (memoryCacheEnabled) {
			memoryCache = data;
		}

	}

	protected abstract void writeCacheImpl(final W data);

	protected JDBCCacheTemplate getJDBCCacheTemplate() {
		return new JDBCCacheTemplate(account.getCache().getDataStore(), account
				.getCache().getCacheId());
	}

}
