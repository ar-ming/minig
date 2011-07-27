package org.obm.locator.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocatorClient {

	private static final Log logger = LogFactory.getLog(LocatorClient.class);

	public String locateHost(String serviceSlashProperty, String loginAtDomain) {
		String url = Activator.getDefault().getLocatorUrl() + "location/host/"
				+ serviceSlashProperty + "/" + loginAtDomain;
		String ip = null;

		try {
			InputStream is = new URL(url).openStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is,
					Charset.forName("utf-8")));
			ip = r.readLine();
			r.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return ip;
	}

}
