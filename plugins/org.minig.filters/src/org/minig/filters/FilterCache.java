package org.minig.filters;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.minig.cache.JDBCCacheCallback;
import org.minig.cache.RowMapper;
import org.minig.obmsync.service.MinigForward;
import org.minig.obmsync.service.MinigVacation;
import org.minig.obmsync.service.SettingService;

import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.webmail.common.FilterStore;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.DatabaseCache;

/**
 * 
 * @author matthieu
 * 
 */
public class FilterCache extends DatabaseCache<List<FilterDefinition>> {

	public static String TABLE_NAME = "minig_filters";

	protected FilterCache(IAccount account) {
		super(account, null, TABLE_NAME);
	}

	@Override
	protected void writeCacheImpl(final List<FilterDefinition> data) {

		getJDBCCacheTemplate().execute(new JDBCCacheCallback() {
			@Override
			public void execute(Connection con, int cacheId)
					throws SQLException {

				String q = "INSERT INTO "
						+ TABLE_NAME
						+ " (criteria, star, delete_it, mark_read, forward, deliver, minig_cache) VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement ps = null;

				try {
					ps = con.prepareStatement(q);
					for (FilterDefinition fd : data) {

						ps.setString(1, fd.getCriteriaString());
						ps.setBoolean(2, fd.isStarIt());
						ps.setBoolean(3, fd.isDelete());
						ps.setBoolean(4, fd.isMarkAsRead());
						ps.setString(5, fd.getForwardTo());
						ps.setString(6, fd.getDeliverInto());
						ps.setInt(7, cacheId);
						ps.executeUpdate();
					}

					updateSieveScriptsOnServer(con, cacheId);

				} finally {
					JDBCUtils.cleanup(null, ps, null);
				}
			}

		});

	}

	private void updateSieveScriptsOnServer(Connection con, int cacheId) {
		// vacation & forward are needed for script regen
		try {
			SettingService ss = new SettingService(account);
			MinigForward mf = ss.getEmailForwarding();
			MinigVacation mv = ss.getVacationSettings();
			String scriptContent = new SieveScriptBuilder().createScript(con, cacheId, mv, mf);
			sendSieveScriptOnServer(scriptContent);
		} catch (Exception e) {
			logger.error("error updating sieve script on server", e);
		}
	}

	private void sendSieveScriptOnServer(String scriptContent) {
		logger.info("script content:\n" + scriptContent);
		FilterStore fs = account.getFilterStore();
		fs.login();
		try {
			fs.replaceActiveScript(new ByteArrayInputStream(scriptContent.getBytes()));
		} finally {
			fs.logout();
		}
	}
	
	@Override
	protected List<FilterDefinition> loadFromCache() {
		List<FilterDefinition> ret = getJDBCCacheTemplate().query(
				"SELECT criteria, star, delete_it, mark_read, forward, deliver, id FROM "
						+ TABLE_NAME + " ", new RowMapper<FilterDefinition>() {
					@Override
					public FilterDefinition mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return getDefinition(rs);
					}

				});
		return ret;
	}

	private FilterDefinition getDefinition(ResultSet rs) throws SQLException {
		FilterDefinition ret = new FilterDefinition();
		HashMap<String, String> cr = new HashMap<String, String>();
		ret.setCriteria(cr);
		String allCrits = rs.getString(1);
		String[] crits = allCrits.split("\n"); // criteria
		boolean start = rs.getBoolean(2);
		boolean delete = rs.getBoolean(3);
		boolean markAsRead = rs.getBoolean(4);
		String forward = rs.getString(5);
		String deliver = rs.getString(6);
		String id = rs.getString(7);

		for (String c : crits) {
			int idx = c.indexOf(": ");
			String crit = c.substring(0, idx);
			String val = c.substring(idx + 2);
			cr.put(crit, val);
		}
		ret.setStarIt(start);
		ret.setDelete(delete);
		ret.setMarkAsRead(markAsRead);
		ret.setForwardTo(forward);
		ret.setDeliverInto(deliver);
		ret.setId(id);

		return ret;
	}

	public void remove(final String filterId) {

		getJDBCCacheTemplate().execute(new JDBCCacheCallback() {
			@Override
			public void execute(Connection con, int cacheId)
					throws SQLException {
				FilterStore fs = account.getFilterStore();
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement("DELETE FROM " + TABLE_NAME
							+ " WHERE id=?");
					ps.setInt(1, Integer.parseInt(filterId));
					int deleted = ps.executeUpdate();
					if (deleted > 0) {
						
						
						// vacation & forward are needed for script regen
						SettingService ss = null;
						MinigForward mf = null;
						MinigVacation mv = null;
						try {
							ss = new SettingService(account);
							mf = ss.getEmailForwarding();
							mv = ss.getVacationSettings();
						} catch (Exception e) {
							logger.error("error loading vac/fwd for sieve regen", e);
						}

						
						String scriptContent = new SieveScriptBuilder()
								.createScript(con, cacheId, mv, mf);
						logger.info("script content:\n" + scriptContent);
						fs.login();
						fs.replaceActiveScript(new ByteArrayInputStream(
								scriptContent.getBytes()));
						fs.logout();
					}
				} finally {
					JDBCUtils.cleanup(null, ps, null);
				}

			}
		});

	}

}
