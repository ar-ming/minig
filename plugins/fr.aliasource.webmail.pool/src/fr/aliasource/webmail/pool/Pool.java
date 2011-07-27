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

package fr.aliasource.webmail.pool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.pool.impl.KeepAliveTask;
import fr.aliasource.webmail.pool.impl.PoolableProxy;

public class Pool<T extends IPoolable> {

	private Set<T> objectsInUse;
	private LinkedBlockingQueue<T> availableObjects;
	private Log logger;
	private String poolId;
	private Timer keepAliveTimer;
	private String destroyMethod;

	public Pool(String poolId, IPoolableObjectFactory<T> factory, int poolSize) {
		this(poolId, factory, poolSize, "destroy");
	}

	public Pool(String poolId, IPoolableObjectFactory<T> factory, int poolSize,
			String destroyMethodName) {
		this(poolId, factory, poolSize, "destroy", 10000);
	}

	public Pool(String poolId, IPoolableObjectFactory<T> factory, int poolSize,
			String destroyMethodName, long keepAlivePeriod) {
		this.logger = LogFactory.getLog(getClass());
		this.poolId = poolId;
		this.objectsInUse = Collections.synchronizedSet(new HashSet<T>());
		this.destroyMethod = destroyMethodName;
		this.availableObjects = new LinkedBlockingQueue<T>(poolSize);

		for (int i = 0; i < poolSize; i++) {
			logger.info(poolId + ": Adding pooled object...");
			availableObjects.add(factory.createNewObject());
			logger.info(poolId + ": Pooled object added.");
		}

		keepAliveTimer = new Timer(poolId + "-keepalive-timer", true);
		KeepAliveTask<T> kaTsk = new KeepAliveTask<T>(availableObjects,
				factory, this);
		keepAliveTimer.scheduleAtFixedRate(kaTsk, 10000, keepAlivePeriod);
	}

	public T get() {
		return get(10, TimeUnit.SECONDS, true);
	}

	public T get(long timeout, TimeUnit tu, boolean retry) {
		T ret = null;
		do {
			try {
				ret = availableObjects.poll(timeout, tu);
			} catch (InterruptedException e) {
				logger.error(poolId + ": Interrupted exception");
			}
			if (ret == null) {
				String message = poolId + ": Took more than " + timeout + " "
						+ tu + " to get a pooled item.";
				if (!retry) {
					throw new RuntimeException(message);
				} else {
					logger.warn(message);
				}
			}
		} while (ret == null);

		T proxied = PoolableProxy.createProxy(this, ret, destroyMethod);
		objectsInUse.add(ret);
		return proxied;
	}

	public void reclaim(T poolable) {
		boolean present = objectsInUse.remove(poolable);
		if (!present) {
			logger.error("Cannot reclaim an object not from this pool "
					+ poolable.getClass());
			return;
		}

		// the keep alive task might have refilled the pool
		boolean accepted = availableObjects.offer(poolable);
		if (!accepted) {
			logger.warn("could not offer poolable to avaiableObjects");
			if (!availableObjects.contains(poolable)) {
				try {
					nativeDestroy(poolable);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public void destroy() {
		if (!objectsInUse.isEmpty()) {
			logger.warn(poolId + ": Closing pool with active IPoolable(s).");
		}
		keepAliveTimer.cancel();
		for (T poolable : availableObjects) {
			try {
				nativeDestroy(poolable);
			} catch (Exception e) {
				logger.warn("cannot call destroy method '" + destroyMethod
						+ "'", e);
			}
		}
		logger.info(poolId + ": pool destroyed.");
	}

	public void nativeDestroy(T poolable) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Method m = poolable.getClass().getMethod(destroyMethod);
		m.invoke(poolable);
	}

	public String getUsageReport() {
		StringBuilder sb = new StringBuilder();
		sb.append(poolId);
		sb.append(" inUse: ");
		sb.append(objectsInUse.size());
		sb.append(" available: ");
		sb.append(availableObjects.size());
		sb.append(" currentTotal: "
				+ (objectsInUse.size() + availableObjects.size()));
		return sb.toString();
	}

	public int getUsageCount() {
		// Production logs show that this can return -1... JDK bug ?!
		return Math.max(0, objectsInUse.size());
	}

}
