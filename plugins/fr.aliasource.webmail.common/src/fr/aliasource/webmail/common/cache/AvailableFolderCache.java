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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minig.cache.CacheActivator;
import org.minig.cache.JDBCCacheCallback;
import org.minig.cache.RowMapper;

import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.folders.ListAvailableFoldersCommand;

/**
 * This class is responsible for caching the available folder list
 * 
 * @author matthieu
 * 
 */
public class AvailableFolderCache extends DatabaseCache<List<IFolder>> {

	private Map<IFolder, Integer> idCache;

	public static String TABLE_NAME = "minig_available_folders";

	public AvailableFolderCache(IAccount account) {
		super(account, new ListAvailableFoldersCommand(account), TABLE_NAME);
		idCache = new HashMap<IFolder, Integer>();
		if (!exists()) {
			try {
				update();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	protected List<IFolder> loadFromCache() {
		List<IFolder> lf = getJDBCCacheTemplate().query(
				"SELECT id, name, display_name, subscribed, shared FROM " + TABLE_NAME
						+ " ", new RowMapper<IFolder>() {
					@Override
					public IMAPFolder mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						IMAPFolder imf = new IMAPFolder(rs.getString(3), rs
								.getString(2), rs.getBoolean(4), rs.getBoolean(5));
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
	protected void writeCacheImpl(final List<IFolder> data)  {
		
		getJDBCCacheTemplate().execute(new JDBCCacheCallback() {
			@Override
			public void execute(Connection con, int cacheId)
					throws SQLException {
		
				PreparedStatement delete = null;
				PreparedStatement insert = null;
				try {
					delete = con.prepareStatement("DELETE FROM  " + TABLE_NAME
							+ " where minig_cache = ?");
					delete.setInt(1, cacheId);
					insert = con
							.prepareStatement("INSERT INTO  "
									+ TABLE_NAME
									+ " (name, display_name, subscribed, shared, minig_cache) VALUES (?, ?, ?, ?, ?)");
					delete.executeUpdate();
					for (IFolder f : data) {
						insert.setString(1, f.getName());
						insert.setString(2, f.getDisplayName());
						insert.setBoolean(3, f.isSubscribed());
						insert.setBoolean(4, f.isShared());
						insert.setInt(5, cacheId);
						insert.executeUpdate();
						int key = CacheActivator.get().generatedKey(con);
						f.setId(key);
					}
				} finally {
					JDBCUtils.cleanup(null, delete, null);
					JDBCUtils.cleanup(null, insert, null);
				}
			}});
	}

}
