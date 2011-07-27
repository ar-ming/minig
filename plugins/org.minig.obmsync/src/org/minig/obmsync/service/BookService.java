package org.minig.obmsync.service;

import java.util.LinkedList;
import java.util.List;

import org.minig.obmsync.exception.ObmSyncConnectionException;
import org.minig.obmsync.provider.impl.ObmSyncBookProvider;
import org.minig.obmsync.provider.impl.ObmSyncProviderFactory;

import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LoginUtils;

public class BookService implements IBookService {

	private ObmSyncBookProvider provider;

	private String userId;
	private String userPassword;

	public BookService(IAccount ac) throws ObmSyncConnectionException {
		this(LoginUtils.lat(ac), ac.getUserPassword());
	}

	public BookService(String userId, String userPassword)
			throws ObmSyncConnectionException {
		this.userId = userId;
		this.userPassword = userPassword;
		provider = ObmSyncProviderFactory.getBookProvider(userId);
	}

	@Override
	public int count() throws Exception {
		int count = 0;
		try {
			this.login();
			count = provider.count();
		} finally {
			this.logout();
		}
		return count;
	}

	@Override
	public List<MinigContact> find(String query, int limit) throws Exception {
		List<MinigContact> ret = new LinkedList<MinigContact>();
		try {
			this.login();
			ret.addAll(provider.find(query, limit));
		} finally {
			this.logout();
		}
		return ret;
	}

	@Override
	public void insert(MinigContact c) throws Exception {
		try {
			this.login();
			provider.create(c);
		} finally {
			this.logout();
		}
	}

	@Override
	public void insert(List<MinigContact> cl) throws Exception {
		try {
			this.login();
			for (MinigContact c : cl) {
				provider.create(c);
			}
		} finally {
			this.logout();
		}
	}

	@Override
	public List<MinigContact> findAll() throws Exception {
		List<MinigContact> result = new LinkedList<MinigContact>();
		try {
			this.login();
			result = provider.getAll();
		} finally {
			this.logout();
		}
		return result;
	}

	private void logout() {
		provider.logout();
	}

	private void login() {
		provider.login(userId, userPassword);
	}

}
