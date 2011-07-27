package org.obm.locator.client;

import java.io.FileInputStream;
import java.util.Properties;

import org.eclipse.core.runtime.Plugin;
import org.obm.locator.client.impl.XTrustProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.obm.locator.client";

	// The shared instance
	private static Activator plugin;
	
	private String locatorUrl;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		XTrustProvider.install();
		Properties p = new Properties();
		FileInputStream fis = new FileInputStream("/etc/obm/obm_conf.ini");
		try {
			p.load(fis);
			locatorUrl = "https://"+p.getProperty("host")+":8084/";
		} finally {
			fis.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public String getLocatorUrl() {
		return locatorUrl;
	}

}
