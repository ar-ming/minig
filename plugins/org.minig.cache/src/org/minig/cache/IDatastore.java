package org.minig.cache;

import java.sql.Connection;

import javax.transaction.UserTransaction;

public interface IDatastore {

	Connection getConnection();
	
	UserTransaction getUserTransaction();

	int getCacheId(String userId);

	void resetCache(int cacheId);

}
