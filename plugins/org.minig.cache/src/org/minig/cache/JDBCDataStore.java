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

package org.minig.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.aliapool.PoolActivator;
import fr.aliasource.utils.JDBCUtils;

public class JDBCDataStore implements IDatastore {

	private static final Log logger = LogFactory.getLog(JDBCDataStore.class);

	public JDBCDataStore() {
	}

	public Connection getConnection() {
		return CacheActivator.get().getConnection();
	}
	
	@Override
	public UserTransaction getUserTransaction() {
		return PoolActivator.getDefault().getTransactionManager();
	}

	@Override
	public void resetCache(int cacheId) {
		Connection con = null;
		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = getConnection();
			purgeUserCache(con, cacheId);
			ut.commit();
		} catch (Exception e) {
			logger.error("Error executing query creating cacheId", e);
			JDBCUtils.rollback(ut);
		} finally {
			JDBCUtils.closeConnection(con);
		}
	}
	
	@Override
	public int getCacheId(String userId) {
		ResultSet rs = null;
		PreparedStatement st = null;
		PreparedStatement st2 = null;
		Connection con = null;
		int cacheId = 0;

		UserTransaction ut = getUserTransaction();
		try {
			ut.begin();
			con = getConnection();
			st = con
					.prepareStatement("SELECT id FROM minig_cache WHERE user_id = ?");
			st.setString(1, userId);
			rs = st.executeQuery();
			if (rs.next()) {
				cacheId = rs.getInt(1);
			}

			if (cacheId == 0) {
				st2 = con.prepareStatement(
						"INSERT INTO minig_cache (user_id) VALUES (?)");

				st2.setString(1, userId);
				st2.executeUpdate();
				cacheId = CacheActivator.get().generatedKey(con);
			}
			ut.commit();
		} catch (Exception e) {
			logger.error("Error executing query creating cacheId", e);
			JDBCUtils.rollback(ut);
		} finally {
			JDBCUtils.cleanup(null, st2, null);
			JDBCUtils.cleanup(con, st, rs);
		}
		return cacheId;
	}

	private void purgeUserCache(Connection con, int cacheId)
			throws SQLException {
		logger.info("resetting minig_cache for cacheId="+cacheId);
		Statement st = null;
		try {
			st = con.createStatement();
			st.execute("DELETE FROM minig_uids WHERE minig_cache=" + cacheId);
			st.execute("DELETE FROM minig_available_folders WHERE minig_cache=" + cacheId);
			st.execute("DELETE FROM minig_subscribed_folders WHERE minig_cache=" + cacheId);
		} finally {
			JDBCUtils.cleanup(null, st, null);
		}
	}


}
