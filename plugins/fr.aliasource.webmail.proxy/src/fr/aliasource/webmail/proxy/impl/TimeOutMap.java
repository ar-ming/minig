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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.proxy.api.IStoppable;

/**
 * Map implementation removing unused values automatically after a given
 * timeout. Values in this map are all {@link IStoppable} implementations. The
 * stop method of the element is called when an item is timed out.
 * 
 * @author tom
 * 
 */
public class TimeOutMap<K, V extends IStoppable> implements Map<K, V> {

	private static Log logger = LogFactory.getLog(TimeOutMap.class);
	private Map<K, TimedItem<V>> map;
	private long timeout;
	private ExecutorService executor;

	private class Remover extends TimerTask {

		public Remover() {
		}

		public void run() {
			// remove old entries
			long curTime = System.currentTimeMillis();
			synchronized (map) {
				Iterator<K> it = map.keySet().iterator();
				while (it.hasNext()) {
					final Object key = it.next();
					final TimedItem<V> ti = map.get(key);
					long delay = curTime - ti.getTime();
					if (delay > timeout) {
						logger.info("Removing expired entry " + ti.getValue());
						executor.execute(new Runnable() {
							@Override
							public void run() {
								V val = ti.getValue();
								val.stop();
								synchronized (map) {
									map.remove(key);
								}
								logger.info("Executor finished removal of "
										+ val);
							}
						});
					}
				}
				logger.info("Remover thread checked " + map.size()
						+ " active sessions.");
			}
		}
	}

	/**
	 * @param timeout
	 *            unused elements timeout in milliseconds
	 */
	public TimeOutMap(long timeout) {
		map = Collections.synchronizedMap(new HashMap<K, TimedItem<V>>());
		this.timeout = timeout;
		executor = Executors.newFixedThreadPool(4);
		Timer t = new Timer();
		t.scheduleAtFixedRate(new Remover(), 1000, timeout);
	}

	/**
	 * calls stop on all elements in this map, and clear it.
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		synchronized (map) {
			for (K key : map.keySet()) {
				IStoppable stoppable = get(key);
				stoppable.stop();
			}
			map.clear();
		}
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public V put(K key, V value) {
		synchronized (map) {
			map.put(key, new TimedItem<V>(System.currentTimeMillis(), value));
		}
		return value;
	}

	public int size() {
		synchronized (map) {
			return map.size();
		}
	}

	public V get(Object key) {
		TimedItem<V> t = null;
		synchronized (map) {
			t = map.get(key);
		}
		if (t != null) {
			return t.getValue();
		} else {
			return null;
		}
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new RuntimeException("not implemented");
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		long cur = System.currentTimeMillis();
		for (K key : m.keySet()) {
			map.put(key, new TimedItem<V>(cur, m.get(key)));
		}
	}

	public V remove(Object key) {
		synchronized (map) {
			TimedItem<V> k = map.remove(key);
			if (k != null) {
				return k.getValue();
			} else {
				return null;
			}
		}
	}

	public Collection<V> values() {
		throw new RuntimeException("not implemented");
	}

}
