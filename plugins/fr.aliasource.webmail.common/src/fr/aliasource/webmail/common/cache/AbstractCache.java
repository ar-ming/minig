package fr.aliasource.webmail.common.cache;

import fr.aliasource.webmail.common.IAccount;

/**
 * 
 * @author matthieu
 *
 */
public abstract class AbstractCache<W> implements ICachingCommand<W> {

	protected IAccount account;
	
	protected AbstractCache(IAccount account){
		this.account = account;
	}

	protected abstract boolean exists();
	
	public abstract void writeToCache(W data) throws Exception;

	protected abstract W loadFromCache();
}
