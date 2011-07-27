package org.minig.imap.tls;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.filter.SSLFilter;

public class MinigTLSFilter extends SSLFilter {

	private static SSLContext CTX;

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(MinigTLSFilter.class);

	static {
		try {
			CTX = BogusSSLContextFactory.getInstance(false);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public MinigTLSFilter() {
		super(CTX);
	}

}
