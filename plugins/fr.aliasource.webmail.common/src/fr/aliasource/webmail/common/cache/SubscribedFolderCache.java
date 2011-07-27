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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.minig.cache.CacheActivator;
import org.minig.cache.JDBCCacheCallback;
import org.minig.cache.RowMapper;

import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.folders.ListSubscribedFoldersCommand;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * This class is responsible for caching the subscribed folder list
 * 
 * @author tom
 * 
 */
public class SubscribedFolderCache extends DatabaseCache<List<IFolder>> {

	private ListSubscribedFoldersCommand lfc;
	public static String TABLE_NAME = "minig_subscribed_folders";
	private Map<IFolder, Integer> idCache;

	public SubscribedFolderCache(IAccount account) {
		super(account, new ListSubscribedFoldersCommand(account), TABLE_NAME);
		this.lfc = (ListSubscribedFoldersCommand) command;
		idCache = new HashMap<IFolder, Integer>();
		if (!exists()) {
			try {
				update();
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * @param accountConf
	 * @return IMAP mailbox delimiter
	 * @throws IOException
	 * @throws StoreException
	 * @throws InterruptedException
	 */
	public String initDefaultFolders(AccountConfiguration accountConf)
			throws IOException, StoreException, InterruptedException {
		return lfc.initDefaultFolders(accountConf);
	}

	@Override
	protected List<IFolder> loadFromCache() {
		List<IFolder> lf = getJDBCCacheTemplate().query(
				"SELECT id, name, display_name, shared FROM " + TABLE_NAME,
				new RowMapper<IFolder>() {
					@Override
					public IMAPFolder mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						IMAPFolder imf = new IMAPFolder(rs.getString(3), rs
								.getString(2), true, rs.getBoolean(4));
						int id = rs.getInt(1);
						imf.setId(id);
						idCache.put(imf, id);
						return imf;

					}
				});

		Collections.sort(lf);
		return lf;
	}

	@Override
	protected void writeCacheImpl(final List<IFolder> data) {

		final List<IFolder> oldFolder = getCachedData();

		getJDBCCacheTemplate().execute(new JDBCCacheCallback() {
			@Override
			public void execute(Connection con, int cacheId)
					throws SQLException {

				// First delete
				HashSet<IFolder> toRemove = new HashSet<IFolder>();
				if (oldFolder != null) {
					toRemove.addAll(oldFolder);
				}
				toRemove.removeAll(data);

				PreparedStatement exists = null;
				PreparedStatement insert = null;
				PreparedStatement delete = null;
				try {
					exists = con.prepareStatement("SELECT id FROM "
							+ TABLE_NAME + " WHERE name=? AND minig_cache = ?");
					delete = con.prepareStatement("DELETE FROM " + TABLE_NAME
							+ " WHERE id=? AND minig_cache = ?");
					insert = con
							.prepareStatement("INSERT INTO "
									+ TABLE_NAME
									+ " (name, display_name, minig_cache, shared) VALUES (?, ?, ?, ?)");

					for (IFolder f : toRemove) {
						logger.info("removing subscribed folder with "
								+ f.getId() + " with id=" + f.getId());
						delete.setInt(1, f.getId());
						delete.setInt(2, cacheId);
						delete.executeUpdate();
						idCache.remove(f);
					}
					if (!toRemove.isEmpty()) {
						account.getCache().getCacheManager().forgetFolders(
								toRemove);
					}

					for (IFolder f : data) {
						ResultSet rs = null;
						try {
							exists.setString(1, f.getName());
							exists.setInt(2, cacheId);
							rs = exists.executeQuery();
							if (!rs.next()) {
								insert.setString(1, f.getName());
								insert.setString(2, f.getDisplayName());
								insert.setInt(3, cacheId);
								insert.setBoolean(4, f.isShared());
								insert.executeUpdate();
								int key = CacheActivator.get()
										.generatedKey(con);
								f.setId(key);
							} else {
								f.setId(rs.getInt(1));
							}
						} finally {
							JDBCUtils.cleanup(null, null, rs);
						}
						idCache.put(f, f.getId());
					}

				} finally {
					JDBCUtils.cleanup(null, exists, null);
					JDBCUtils.cleanup(null, insert, null);
				}
			}
		});

	}

	public IFolder get(IFolder f) {
		if (logger.isDebugEnabled()) {
			logger.debug("get(" + f + ") " + (f != null ? f.getName() : "")
					+ " idCache: " + idCache);
		}
		Integer id = idCache.get(f);
		if (id == null) {
			List<Integer> folderId = getJDBCCacheTemplate().query(
					"SELECT id FROM " + TABLE_NAME + " WHERE name=? ",
					new String[] { f.getName() }, new RowMapper<Integer>() {
						@Override
						public Integer mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return rs.getInt(1);
						}
					});
			if (folderId.size() == 1) {
				id = folderId.get(0);
				idCache.put(f, id);
			} else {
				logger.error("[" + account.getUserId()
						+ "] folder_id not found for folder " + f.getName()
						+ " (folders found: " + folderId.size() + ")");
			}
		}

		f.setId(id);
		return f;
	}

	public IFolder getFolderByName(String folderName) {
		for (IFolder folder: idCache.keySet()) {
			if (folder.getName().equals(folderName)) {
				return folder;
			}
		}
		return null;
	}
	
}
