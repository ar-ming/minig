package org.minig.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import fr.aliasource.obm.aliapool.PoolActivator;
import fr.aliasource.obm.aliapool.pool.DataSource;
import fr.aliasource.obm.aliapool.tm.TransactionManager;
import fr.aliasource.utils.IniFile;
import fr.aliasource.utils.JDBCUtils;

public class CacheActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.minig.cache";
	private static final Log logger = LogFactory.getLog(CacheActivator.class);

	private static CacheActivator plugin;

	private DataSource ds;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IniFile ini = new IniFile("/etc/minig/account_conf.ini") {
			@Override
			public String getCategory() {
				return "account";
			}
		};
		Map<String, String> data = ini.getData();

		String driver = data.get("account.caches.jdbc.driver");
		String jdbcUrl = data.get("account.caches.jdbc.url");
		String login = data.get("account.caches.jdbc.login");
		String password = data.get("account.caches.jdbc.password");
		logger.info("starting minig database connection to: " + jdbcUrl);

		int poolSize = 4 * Runtime.getRuntime().availableProcessors();
		
		ds = PoolActivator.getDefault().createDataSource(driver, jdbcUrl,
				login, password, poolSize, "SELECT 1");

		logger.info("MiniG backend sql cache activated (url: " + jdbcUrl + ")");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ds.stop();
		ds = null;
		plugin = null;
		super.stop(context);
	}

	TransactionManager getUserTransaction() {
		return PoolActivator.getDefault().getTransactionManager();
	}

	Connection getConnection() {
		Connection con = null;
		try {
			con = ds.getConnection();
		} catch (Throwable e) {
			logger.error("Error getting derby connection", e);
		}
		return con;
	}

	public int generatedKey(Connection con) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int ret = 0;
		try {
			ps = con.prepareStatement("SELECT lastval()");
			rs = ps.executeQuery();
			rs.next();
			ret = rs.getInt(1);
		} finally {
			JDBCUtils.cleanup(null, ps, rs);
		}
		return ret;
	}

//	private String computeUrl(String cacheDirectory) {
//		return "jdbc:derby:"
//				+ cacheDirectory
//				+ "/minig.db"
//				+ (new File(cacheDirectory + "/minig.db").exists() ? ""
//						: ";create=true");
//	}

	static public CacheActivator get() {
		return plugin;
	}

}
