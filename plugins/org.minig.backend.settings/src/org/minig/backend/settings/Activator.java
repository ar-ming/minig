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

package org.minig.backend.settings;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.minig.backend.settings.impl.AccountSettingsFactory;
import org.osgi.framework.BundleContext;

import fr.aliasource.utils.RunnableExtensionLoader;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.minig.backend.settings";

	// The shared instance
	private static Activator plugin;

	private List<ISettingsProviderFactory> factories;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		factories = new LinkedList<ISettingsProviderFactory>();
		factories.add(new AccountSettingsFactory());

		RunnableExtensionLoader<ISettingsProviderFactory> rel = new RunnableExtensionLoader<ISettingsProviderFactory>();
		factories.addAll(rel.loadExtensions(PLUGIN_ID, "provider", "provider_factory",
				"implementation"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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

	public List<ISettingsProviderFactory> getFactories() {
		return factories;
	}

}
