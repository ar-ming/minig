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

package fr.aliasource.webmail.pool.impl;

import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.pool.IPoolable;
import fr.aliasource.webmail.pool.IPoolableObjectFactory;
import fr.aliasource.webmail.pool.Pool;

public class KeepAliveTask<T extends IPoolable> extends TimerTask {

	private LinkedBlockingQueue<T> availableObjects;

	private IPoolableObjectFactory<T> factory;

	private Pool<T> pool;

	private static final Log logger = LogFactory.getLog(KeepAliveTask.class);

	public KeepAliveTask(LinkedBlockingQueue<T> availableObjects,
			IPoolableObjectFactory<T> factory, Pool<T> pool) {
		this.availableObjects = availableObjects;
		this.factory = factory;
		this.pool = pool;
	}

	@Override
	public void run() {
		LinkedList<T> dead = new LinkedList<T>();

		for (T t : availableObjects) {
			boolean isAlive = t.keepAlive();
			if (!isAlive) {
				logger.warn("Dead poolable item (" + t + "). will recycle.");
				dead.add(t);
			}
		}

		if (!dead.isEmpty()) {
			logger.warn("pool usage report: " + pool.getUsageReport());
		}

		for (T t : dead) {
			availableObjects.remove(t);
			recycle();
		}
		
		if (availableObjects.remainingCapacity() - pool.getUsageCount() > 0) {
			logger.error("Pool refilling failed this time: "+pool.getUsageReport());
		}
	}

	private void recycle() {
		T recycled = factory.createNewObject();
		if (recycled != null) {
			boolean accepted = availableObjects.offer(recycled);
			if (accepted) {
				logger.info("Dead poolable item (" + recycled + ") recycled.");
			} else {
				try {
					pool.nativeDestroy(recycled);
				} catch (Throwable t) {
				}
			}
		} else {
			logger.warn("Cannot recycle dead poolable "
					+ "(next keepAliveTask run will retry)");
		}
	}

}
