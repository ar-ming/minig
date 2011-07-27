package org.minig.obmsync.service;

import java.util.List;

import fr.aliasource.webmail.book.MinigContact;

public interface IBookService {
	
	void insert(MinigContact c) throws Exception;
	void insert(List<MinigContact> cl) throws Exception;
	
	int count() throws Exception;
	List<MinigContact> findAll() throws Exception;
	List<MinigContact> find(String query, int limit) throws Exception;
	
}
