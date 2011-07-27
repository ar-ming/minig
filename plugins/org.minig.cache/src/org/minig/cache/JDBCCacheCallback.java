package org.minig.cache;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author matthieu
 *
 */
public abstract class JDBCCacheCallback {
	abstract public void execute(Connection con, int cacheId) throws SQLException;

}
