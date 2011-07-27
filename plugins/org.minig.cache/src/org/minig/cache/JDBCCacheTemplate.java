package org.minig.cache;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.JDBCUtils;

/**
 * Helper class to JDBC query over a cacheId
 * 
 * @author matthieu 
 * 
 */
public class JDBCCacheTemplate {

	private IDatastore ds;
	private int cacheId;
	private static Log logger = LogFactory.getLog(JDBCCacheTemplate.class);

	public JDBCCacheTemplate(IDatastore dataStore, int cacheId) {
		this.ds = dataStore;
		this.cacheId = cacheId;
	}

	public int queryForInt(String query, String whereCondititions) {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		int count = 0;

		String q = query + " WHERE minig_cache = ? ";
		try {
			con = ds.getConnection();
			if (whereCondititions != null) {
				q += whereCondititions;
			}
			ps = con.prepareStatement(q);
			ps.setInt(1, cacheId);
			rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException se) {
			logger.error("Error executing query " + q, se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		return count;
	}

	public <T> List<T> query(String query, RowMapper<T> rowMapper) {
		Connection con = ds.getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		List<T> ret = new ArrayList<T>();
		try {
			st = con.prepareStatement(query + " WHERE minig_cache = ?");
			st.setInt(1, cacheId);
			rs = st.executeQuery();
			int i = 0;
			while (rs.next()) {
				ret.add(rowMapper.mapRow(rs, i));
				i++;
			}
		} catch (Throwable se) {
			logger.error("Error executing query " + query, se);
		} finally {
			JDBCUtils.cleanup(con, st, rs);
		}
		return ret;
	}

	public void execute(JDBCCacheCallback cacheCallback) {
		UserTransaction ut = ds.getUserTransaction();
		Connection con = null;
		try {
			ut.begin();
			con = ds.getConnection();
			cacheCallback.execute(con, cacheId);
			ut.commit();
		} catch (Throwable e) {
			JDBCUtils.rollback(ut);
			if(e !=null && e instanceof BatchUpdateException){
				logger.error("Unexpected rollback:",((BatchUpdateException)e).getNextException());
			} else {
				logger.error("Unexpected rollback: " + e.getMessage(), e);
			}
		} finally {
			JDBCUtils.cleanup(con, null, null);
		}

	}

	public <T> T queryForObject(String query, Object[] objects,
			RowMapper<T> rowMapper) {
		List<T> list = query(query, objects, rowMapper);
		if (list.size() > 1) {
			throw new CacheException();
		} else {
			if (list.size() == 1) {
				return list.get(0);
			} else {
				return null;
			}
		}
	}
	
	public <T> List<T> query(String query, Object[] objects,
			RowMapper<T> rowMapper) {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		List<T> ret = new ArrayList<T>();
		try {
			con = ds.getConnection();
			st = con.prepareStatement(query + " AND minig_cache = ?");
			for (int i = 0; i < objects.length; i++) {
				st.setObject(i + 1, objects[i]);
			}
			st.setInt(objects.length + 1, cacheId);
			rs = st.executeQuery();
			int i = 0;
			while (rs.next()) {
				ret.add(rowMapper.mapRow(rs, i));
				i++;
			}
		} catch (Throwable se) {
			logger.error("Error executing query " + query, se);
		} finally {
			JDBCUtils.cleanup(con, st, rs);
		}
		return ret;
	}

}
