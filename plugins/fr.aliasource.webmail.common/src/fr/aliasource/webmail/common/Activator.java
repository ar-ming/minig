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

package fr.aliasource.webmail.common;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.minig.cache.CacheActivator;
import org.osgi.framework.BundleContext;

import fr.aliasource.utils.RunnableExtensionLoader;
import fr.aliasource.webmail.common.conversation.IBodyFormatter;
import fr.aliasource.webmail.common.conversation.IConversationListenerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "fr.aliasource.webmail.common";

	// The shared instance
	private static Activator plugin;

	private List<IBodyFormatter> formatters;
	private List<IConversationListenerFactory> listenersFactories;
	private List<MailHeadersFilter> mailHeadersFilters;

	private Log logger = LogFactory.getLog(getClass());

	private LocatorRegistry registry;

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.registry = new LocatorRegistry();
		loadFormatters();
		loadListenerFactories();
		loadMailHeadersFilter();
		loadDataStore();
	}

	private void loadDataStore() {
		logger.info("Loadiing MiniG datastore...");
		CacheActivator.get();
	}

	private void loadMailHeadersFilter() {
		RunnableExtensionLoader<MailHeadersFilter> loader = new RunnableExtensionLoader<MailHeadersFilter>();
		mailHeadersFilters = loader.loadExtensions(PLUGIN_ID, "mailheadersfilter", "mail_headers_filter", "implementation");
	}
	
	private void loadListenerFactories() {
		RunnableExtensionLoader<IConversationListenerFactory> loader = new RunnableExtensionLoader<IConversationListenerFactory>();
		listenersFactories = loader.loadExtensions(PLUGIN_ID,
				"conversationlistenerfactory", "conversation_listener_factory",
				"implementation");
	}

	private void loadFormatters() {
		RunnableExtensionLoader<IBodyFormatter> rel = new RunnableExtensionLoader<IBodyFormatter>();
		List<IBodyFormatter> le = rel.loadExtensions(PLUGIN_ID,
				"bodyformatter", "body_formatter", "implementation");
		formatters = le;
	}

	public List<IBodyFormatter> getFormatters() {
		return formatters;
	}

	public LocatorRegistry getLocatorRegistry() {
		return registry;
	}

	public List<IConversationListenerFactory> getListenerFactories() {
		return listenersFactories;
	}

	public List<MailHeadersFilter> getMailHeadersFilters() {
		return mailHeadersFilters;
	}
	

	@Override
	public void stop(BundleContext context) throws Exception {
		formatters.clear();
		formatters = null;
		listenersFactories.clear();
		listenersFactories = null;
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

}
