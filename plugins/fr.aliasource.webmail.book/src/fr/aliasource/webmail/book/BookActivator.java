/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.book;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import fr.aliasource.webmail.proxy.ProxyConfiguration;

/**
 * The activator class controls the plug-in life cycle
 */
public class BookActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "fr.aliasource.webmail.book";

	// The shared instance
	private static BookActivator plugin;

	private BookManager bookManager;
	
	/**
	 * The constructor
	 */
	public BookActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bookManager = new BookManager(new ProxyConfiguration());
		bookManager.init();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bookManager.shutdown();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BookActivator getDefault() {
		return plugin;
	}

	public BookManager getBookManager() {
		return bookManager;
	}

}
