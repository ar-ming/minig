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

package fr.aliasource.webmail.common.uid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.minig.cache.JDBCCacheCallback;
import org.minig.cache.RowMapper;

import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.DatabaseCache;
import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Stores a cache of all the uids in a given folder
 * 
 * @author tom
 * 
 */
public class UIDCache extends DatabaseCache<Set<Long>> {

	private int folderId;
	private String debugName;
	private AtomicLong flagResync;

	public static String TABLE_NAME = "minig_uids";

	public UIDCache(IAccount account, IFolder f) {
		super(account, new UIDFetchCommand(f, account), TABLE_NAME);
		flagResync = new AtomicLong(f.getName().equalsIgnoreCase("inbox") ? 0
				: new Random().nextInt(25));
		memoryCacheEnabled = true;
		this.folderId = f.getId();
		this.debugName = "[" + account.getUserId() + "][" + f.getName() + "]";
		if (folderId == 0) {
			throw new RuntimeException("folderId for " + f.getName() + " is 0.");
		}
		this.exists = false;
	}

	@Override
	protected boolean existsImpl(String tableName) {
		long time = System.currentTimeMillis();

		int size = getJDBCCacheTemplate().queryForInt(
				"SELECT 1 FROM " + tableName, " AND folder_id=" + folderId);
		boolean ret = size > 0;

		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(debugName + " existsImpl(" + tableName
					+ ") folderId: " + folderId + "=> " + size + ". Done in "
					+ time + "ms.");
		}
		return ret;
	}

	public UIDChanges computeChanges(Set<Long> oldUids, Set<Long> fetched) {
		Set<Long> toRemove = new HashSet<Long>();
		Set<Long> old = new HashSet<Long>();
		if (oldUids != null) {
			old.addAll(oldUids);
		}
		toRemove.addAll(old);
		toRemove.removeAll(fetched);

		Set<Long> toInsert = new HashSet<Long>();
		toInsert.addAll(fetched);
		toInsert.removeAll(old);
		return new UIDChanges(toInsert.toArray(new Long[toInsert.size()]),
				toRemove.toArray(new Long[toRemove.size()]), fetched);
	}

	@Override
	protected Set<Long> loadFromCache() {
		long time = System.currentTimeMillis();
		Set<Long> uids = new HashSet<Long>();

		uids.addAll(getJDBCCacheTemplate().query(
				"SELECT uid FROM " + TABLE_NAME + " WHERE folder_id="
						+ folderId, new Object[0], new RowMapper<Long>() {
					@Override
					public Long mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return rs.getLong(1);
					}
				}));

		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(debugName + " loadCache(" + TABLE_NAME + ") in "
					+ time + "ms.");
		}
		return uids;
	}

	@Override
	protected void writeCacheImpl(final Set<Long> data) {

		final Set<Long> oldUids = getCachedData();

		getJDBCCacheTemplate().execute(new JDBCCacheCallback() {
			@Override
			public void execute(Connection con, int cacheId)
					throws SQLException {

				Set<Long> toRemove = new HashSet<Long>();

				if (oldUids != null) {
					toRemove.addAll(oldUids);
					toRemove.removeAll(data);
				}

				Set<Long> toInsert = new HashSet<Long>();
				toInsert.addAll(data);
				if (oldUids != null) {
					toInsert.removeAll(oldUids);
				}

				if (toRemove.size() == 0 && toInsert.size() == 0) {
					return;
				}

				PreparedStatement del = null;
				PreparedStatement insert = null;

				if (logger.isDebugEnabled()) {
					logger.debug(debugName + " should run a batch with "
							+ toRemove.size() + " deletions & "
							+ toInsert.size() + " insertions.");
				}

				try {
					del = con
							.prepareStatement("DELETE FROM "
									+ TABLE_NAME
									+ " WHERE folder_id=? AND uid=? AND minig_cache = ?");
					for (Long l : toRemove) {
						del.setInt(1, folderId);
						del.setLong(2, l);
						del.setInt(3, cacheId);
						del.addBatch();
					}

					insert = con
							.prepareStatement("INSERT INTO "
									+ TABLE_NAME
									+ " (folder_id, uid, minig_cache) VALUES (?, ?, ?)");
					for (Long l : toInsert) {
						insert.setInt(1, folderId);
						insert.setLong(2, l);
						insert.setInt(3, cacheId);
						insert.addBatch();
					}
					del.executeBatch();
					insert.executeBatch();
				} finally {
					JDBCUtils.cleanup(null, del, null);
					JDBCUtils.cleanup(null, insert, null);
				}
			}
		});

	}

	public boolean flagResyncNeeded() {
		return ((flagResync.incrementAndGet() - 1) % 30) == 0;
	}

}
