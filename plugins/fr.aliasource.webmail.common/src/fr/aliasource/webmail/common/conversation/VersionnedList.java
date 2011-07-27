package fr.aliasource.webmail.common.conversation;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

public class VersionnedList<T> extends LinkedList<T> {

	private static final long serialVersionUID = -3495133497095800811L;

	private AtomicLong version;

	public VersionnedList() {
		super();
		version = new AtomicLong(0);
	}

	public long getVersion() {
		return version.get();
	}

	public void setVersion(long newValue) {
		version.set(newValue);
	}

	public long incrementAndGet() {
		return version.incrementAndGet();
	}

}
