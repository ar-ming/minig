package org.minig.obm23.locator;

import org.obm.locator.client.LocatorClient;

import fr.aliasource.webmail.common.IServerLocator;

public class ServerLocator implements IServerLocator {

	@Override
	public String getHostName(String login, String domain, String locatorUrlPart) {
		LocatorClient lc = new LocatorClient();
		String lat = login;
		if (domain != null && !lat.contains("@")) {
			lat += "@" + domain;
		}
		String ip = lc.locateHost(locatorUrlPart, lat);
		return ip;
	}

	@Override
	public boolean supportsUriScheme(String scheme) {
		return "locator".equals(scheme);
	}

}
