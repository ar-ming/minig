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

package fr.aliasource.webmail.proxy.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.RunnableExtensionLoader;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LocatorRegistry;
import fr.aliasource.webmail.proxy.ProxyActivator;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.Completion;
import fr.aliasource.webmail.proxy.api.ICompletionSourceFactory;

public class CompletionRegistry {

	private List<ICompletionSourceFactory> factories;
	private Comparator<? super Completion> completionComparator;
	private ProxyConfiguration conf;
	private Log logger;

	public CompletionRegistry(ProxyConfiguration conf, LocatorRegistry locator) {
		logger = LogFactory.getLog(getClass());
		this.conf = conf;
		completionComparator = new Comparator<Completion>() {
			public int compare(Completion o1, Completion o2) {
				return o1.getCompareValue().compareTo(o2.getCompareValue());
			}
		};
		factories = new LinkedList<ICompletionSourceFactory>();

		loadPlugins(conf);
	}

	private void loadPlugins(ProxyConfiguration conf) {
		RunnableExtensionLoader<ICompletionSourceFactory> loader = new RunnableExtensionLoader<ICompletionSourceFactory>();
		List<ICompletionSourceFactory> l = loader.loadExtensions(
				ProxyActivator.PLUGIN_ID, "completionsourcefactory",
				"completion_source_factory", "implementation");
		for (ICompletionSourceFactory csf : l) {
			register(csf);
		}
		logger.info(factories.size() + " completion factories registered.");
	}

	private void register(ICompletionSourceFactory csf) {
		csf.init(conf);
		factories.add(csf);
	}

	public List<Completion> complete(IAccount account, String type, String query, int limit) {
		SortedSet<Completion> comps = new TreeSet<Completion>(
				completionComparator);
		for (ICompletionSourceFactory csf : factories) {
			if (csf.supports(type)) {
				List<Completion> cResult = csf.getInstance(type).complete(account, 
						query, limit);
				comps.addAll(cResult);
			}
		}

		Iterator<Completion> it = comps.iterator();
		int i = 0;
		List<Completion> ret = new ArrayList<Completion>(limit);
		while (it.hasNext() && i < limit) {
			ret.add(it.next());
			i++;
		}
		if (logger.isInfoEnabled()) {
			logger.info("complete(" + type + ", " + query + ", " + limit
					+ ") => " + ret.size() + " results.");
		}
		return ret;
	}

}
