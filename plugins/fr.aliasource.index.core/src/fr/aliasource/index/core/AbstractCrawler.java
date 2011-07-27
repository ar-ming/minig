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

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base implementation for {@link ICrawler}. Users must implement getType and
 * fetchData.
 * 
 * @author tom
 * 
 */
public abstract class AbstractCrawler implements ICrawler {

	protected Log logger;
	private LinkedBlockingQueue<String> toCrawl;
	protected ICrawlerListener listener;

	protected AbstractCrawler() {
		this.logger = LogFactory.getLog(getClass());
		this.toCrawl = new LinkedBlockingQueue<String>();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ICrawler
				&& ((ICrawler) obj).getType().equals(getType());
	}

	@Override
	public int hashCode() {
		return getType().hashCode();
	}

	@Override
	public void startFetch() {
		if (logger.isDebugEnabled()) {
			logger.debug("incremental fetch");
		}

		int i = 0;
		String id = null;
		do {
			try {
				id = toCrawl.poll(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
			if (id != null) {
				Map<String, String> data = fetchData(id);
				listener.dataFetched(getType(), data);
				i++;
			}
		} while (id != null);
		listener.crawlComplete(getType(), i > 0);
	}

	@Override
	public void queueFetch(String id) {
		if (logger.isDebugEnabled()) {
			logger.debug("queue fetch on id " + id);
		}

		toCrawl.add(id);
	}

	@Override
	public void registerListener(ICrawlerListener listener) {
		this.listener = listener;
	}

	protected abstract Map<String, String> fetchData(String id);

}
