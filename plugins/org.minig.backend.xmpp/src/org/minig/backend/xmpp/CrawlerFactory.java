package org.minig.backend.xmpp;

import fr.aliasource.index.core.AbstractCrawler;
import fr.aliasource.index.core.ICrawlerFactory;
import fr.aliasource.index.core.IIndexingParameters;
import fr.aliasource.webmail.common.MailIndexingParameters;

public class CrawlerFactory implements ICrawlerFactory {

	public CrawlerFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractCrawler create(IIndexingParameters parameters) {
		// TODO Auto-generated method stub
		MailIndexingParameters mip = (MailIndexingParameters) parameters;
		return new ChatCrawler(mip);
	}

}
