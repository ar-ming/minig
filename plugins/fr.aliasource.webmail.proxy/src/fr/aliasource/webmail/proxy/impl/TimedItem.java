/*
 * Created on Jul 22, 2004
 *
 */
package fr.aliasource.webmail.proxy.impl;

import fr.aliasource.webmail.proxy.api.IStoppable;

/**
 * A time/objet pair
 * 
 * @author tom
 *
 */
public class TimedItem<V extends IStoppable> {
	private long time;
	private V value;

	public TimedItem(long time, V value) {
		this.time = time;
		this.value = value;
	}

	/**
	 * @return
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return
	 */
	public V getValue() {
		return value;
	}

	/**
	 * @param l
	 */
	public void setTime(long l) {
		time = l;
	}

	/**
	 * @param object
	 */
	public void setValue(V object) {
		value = object;
	}

}
