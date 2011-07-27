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

package fr.aliasource.webmail.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import fr.aliasource.index.core.AbstractCrawler;
import fr.aliasource.index.core.IIndexingParameters;
import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.MailIndexingParameters;
import fr.aliasource.webmail.common.conversation.IConversationListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class IndexingActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "fr.aliasource.webmail.indexing";

	// The shared instance
	private static IndexingActivator plugin;

	private Log logger;

	/**
	 * The constructor
	 */
	public IndexingActivator() {
		logger = LogFactory.getLog(getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		logger.info("Indexing plugin activated");
	}

	/*
	 * (non-Javadoc)
	 * 
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
	public static IndexingActivator getDefault() {
		return plugin;
	}

	public IConversationListener createListener(IAccount account) {
		SearchDirector sd = account.getSearchDirector();
		return new ConversationsIndexer(account, sd);
	}

	public AbstractCrawler create(IIndexingParameters parameters) {
		MailIndexingParameters mip = (MailIndexingParameters) parameters;
		return new MailCrawler(mip);
	}

}
