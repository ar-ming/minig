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

package fr.aliasource.index.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.index.core.impl.IndexManager;
import fr.aliasource.index.core.impl.RunnableCrawler;
import fr.aliasource.utils.RunnableExtensionLoader;

/**
 * Manages crawling & searching
 * 
 * @author tom
 * 
 */
public class SearchDirector implements ICrawlerListener, ISearchable {

	private Map<String, ICrawler> crawlers;
	private Map<ICrawler, RunnableCrawler> runnables;
	private Map<String, Thread> runningCrawlers;
	private IndexManager idxMgr;
	private Log logger;
	private boolean stopped;

	private IIndexingParameters parameters;

	public SearchDirector(IIndexingParameters parameters) {
		logger = LogFactory.getLog(getClass());
		crawlers = Collections.synchronizedMap(new HashMap<String, ICrawler>());
		runnables = Collections
				.synchronizedMap(new HashMap<ICrawler, RunnableCrawler>());
		runningCrawlers = Collections
				.synchronizedMap(new HashMap<String, Thread>());
		idxMgr = new IndexManager(parameters);
		stopped = false;
		this.parameters = parameters;
		registerCrawlers();
		logger.info("SearchDirector created.");
	}

	private void registerCrawlers() {
		RunnableExtensionLoader<ICrawlerFactory> loader = new RunnableExtensionLoader<ICrawlerFactory>();

		List<ICrawlerFactory> c = loader.loadExtensions(
				SearchActivator.PLUGIN_ID, "crawlerfactory", "crawler_factory",
				"implementation");
		for (ICrawlerFactory cw : c) {
			registerCrawler(cw.create(parameters));
		}
	}

	private void registerCrawler(AbstractCrawler cw) {
		synchronized (crawlers) {
			crawlers.put(cw.getType(), cw);
		}
		synchronized (runnables) {
			runnables.put(cw, new RunnableCrawler(cw));
		}
		idxMgr.setupIndex(cw.getType());
		cw.registerListener(this);
	}

	/**
	 * Launch crawl threads, starting by a full fetch, then doing incremental
	 * updates when crawlData is called.
	 */
	public void startCrawlers() {
		runCrawlers();
	}

	/**
	 * Waits for crawl threads completion & prevents new additions to the index
	 */
	public void stopCrawlers() {
		stopped = true;
		if (logger.isInfoEnabled()) {
			logger.info("Waiting for running crawlers completion...");
		}
		List<Thread> toStop = new ArrayList<Thread>(runningCrawlers.size());
		synchronized (runningCrawlers) {
			toStop.addAll(runningCrawlers.values());
		}

		for (Thread t : toStop) {
			logger.info("trying to stop " + t);
			try {
				t.join();
				logger.info("thread " + t + " died cleanly.");
			} catch (InterruptedException e) {
				logger.warn("interrupted exception on crawler completion");
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Crawlers ended.");
		}
	}

	private void runCrawlers() {
		for (ICrawler cw : crawlers.values()) {
			runCrawler(cw);
		}
	}

	private void runCrawler(ICrawler cw) {
		if (stopped) {
			return;
		}

		Thread t = null;
		synchronized (runningCrawlers) {
			t = runningCrawlers.get(cw.getType());
		}
		if (t == null || !t.isAlive()) {
			startCrawlThread(cw);
		}
	}

	private void startCrawlThread(ICrawler cw) {
		RunnableCrawler rc = null;
		synchronized (runnables) {
			rc = runnables.get(cw);
		}

		Thread t = new Thread(rc, "crawler-" + cw.toString());
		synchronized (runningCrawlers) {
			runningCrawlers.put(cw.getType(), t);
		}
		t.start();
	}

	/**
	 * Queue a new data (re)fetch on a crawler & asynchronously index it.
	 * 
	 * @param type
	 * @param id
	 */
	public void crawlData(String type, String id) {
		if (stopped) {
			return;
		}

		ICrawler c = null;
		synchronized (crawlers) {
			c = crawlers.get(type);
		}
		if (c != null) {
			c.queueFetch(id);
			runCrawler(c);
		} else {
			logger.warn("No crawler registered for fetching " + type + " / "
					+ id);
		}
	}

	@Override
	public void dataFetched(String type, Map<String, String> fetchedData) {
		idxMgr.index(type, fetchedData);
	}

	@Override
	public void crawlComplete(String type, boolean commitNeeded) {
		if (commitNeeded) {
			idxMgr.getIndex(type).commit();
		}
		synchronized (runningCrawlers) {
			runningCrawlers.remove(type);
		}
		logger.info("type " + type + " removed from runningCrawlers");
	}

	@Override
	public List<Hit> findByType(String type, String query) {
		return idxMgr.getIndex(type).doQuery(query);
	}

	public void queueDeletion(String type, String id) {
		idxMgr.delete(type, id);
	}

	public void clearIndex(String type) {
		idxMgr.getIndex(type).deleteByQuery("type:" + type);
	}

}
