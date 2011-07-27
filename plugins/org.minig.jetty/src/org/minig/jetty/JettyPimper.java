package org.minig.jetty;

import java.util.Dictionary;

import org.eclipse.equinox.http.jetty.JettyCustomizer;
import org.mortbay.jetty.nio.SelectChannelConnector;

/**
 * Pimp my Jetty
 * 
 * @author tom
 * 
 */
public class JettyPimper extends JettyCustomizer {

	public JettyPimper() {
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object customizeHttpConnector(Object connector, Dictionary settings) {
		try {
			SelectChannelConnector c = (SelectChannelConnector) connector;
			c.setAcceptors(10);
			c.setMaxIdleTime(20 * 60 * 1000);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return super.customizeHttpConnector(connector, settings);
	}

}
