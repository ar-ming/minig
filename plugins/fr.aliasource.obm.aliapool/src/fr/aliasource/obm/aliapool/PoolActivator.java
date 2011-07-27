package fr.aliasource.obm.aliapool;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import fr.aliasource.obm.aliapool.pool.DataSource;
import fr.aliasource.obm.aliapool.tm.TransactionManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class PoolActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "fr.aliasource.obm.aliapool";

	// The shared instance
	private static PoolActivator plugin;

	private TransactionManager tm;
	private Log logger;

	public PoolActivator() {
		logger = LogFactory.getLog(getClass());
		tm = TransactionManager.getInstance();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		logger.info("Pool bundle started.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		logger.info("Pool bundle stopped.");
	}

	/**
	 * Returns the shared instance
	 */
	public static PoolActivator getDefault() {
		return plugin;
	}

	public TransactionManager getTransactionManager() {
		return tm;
	}

	public DataSource createDataSource(String driverClass, String url,
			String login, String password, int max, String pingQuery)
			throws SQLException {
		return new DataSource(driverClass, url, login, password,
				null, max, pingQuery);
	}

	/**
	 * Creates a new datasource with the given jdbc properties, driver class & url.
	 */
	public DataSource createDataSource(String driverClass, String url,
			Properties jdbcProps, int max, String pingQuery)
			throws SQLException {
		return new DataSource(driverClass, url, null, null, jdbcProps,
				max, pingQuery);
	}
}
