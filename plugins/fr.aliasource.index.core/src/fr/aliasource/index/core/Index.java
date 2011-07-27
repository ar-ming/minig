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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract Index classes, providing async updates of the underlying index.
 * 
 * @author tom
 * 
 */
public abstract class Index implements ISearchable {

	// private Queue<Map<String, String>> writeQueue;
	// private Queue<String> idDeletionQueue;
	// private ReadWriteLock rwLock;
	// private AsyncIndexWriter aiw;
	private String indexType;
	protected Log logger;

	// private Thread flushThread;

	public Index(String type) {
		logger = LogFactory.getLog(getClass());
		this.indexType = type;
		// writeQueue = new LinkedBlockingQueue<Map<String, String>>();
		// idDeletionQueue = new LinkedBlockingQueue<String>();
		// rwLock = new ReentrantReadWriteLock(false);
		// aiw = new AsyncIndexWriter(rwLock, writeQueue, idDeletionQueue,
		// this);
	}

	public void queueWrite(Map<String, String> data) {
		doWrite(data);
	}

	/**
	 * Writes an entry in the index
	 * called
	 * 
	 * @param data
	 */
	protected abstract void doWrite(Map<String, String> data);

	private List<Hit> find(String query) {
		// try {
		// rwLock.readLock().lock();
		return doQuery(query);
		// } finally {
		// rwLock.readLock().unlock();
		// }
	}

	@Override
	public List<Hit> findByType(String type, String query) {
		if (!indexType.equals(type)) {
			return new LinkedList<Hit>();
		} else {
			return find(query);
		}
	}

	public String getType() {
		return indexType;
	}

	public abstract List<Hit> doQuery(String query);

	public abstract void commit();

	public abstract void optimize();

	public abstract void deleteById(String id);

	public abstract void deleteByQuery(String query);

	public void queueDeletion(String id) {
//		idDeletionQueue.add(id);
//		if (idDeletionQueue.size() > 1000) {
//			flush();
//		}
		deleteById(id);
	}
}
