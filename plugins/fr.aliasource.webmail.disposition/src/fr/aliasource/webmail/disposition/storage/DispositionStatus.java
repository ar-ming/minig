package fr.aliasource.webmail.disposition.storage;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public enum DispositionStatus {
	Sent(0) {
		@Override
		public boolean isPending() {
			return false;
		}
	},
	Denied(1) {
		@Override
		public boolean isPending() {
			return false;
		}
	},
	Pending(2) {
		@Override
		public boolean isPending() {
			return true;
		}
	};
	
	private static Map<Integer, DispositionStatus> map;
	private final int dbValue;

	private DispositionStatus(int dbValue) {
		this.dbValue = dbValue;
	}
	
	public int getDbValue() {
		return dbValue;
	}
	
	public abstract boolean isPending();
	
	public static DispositionStatus fromDbValue(int value) {
		if (map == null) {
			map = createMap();
		}
		return map.get(value);
	}

	private static Map<Integer, DispositionStatus> createMap() {
		HashMap<Integer, DispositionStatus> tmp = Maps.newHashMapWithExpectedSize(values().length);
		for (DispositionStatus status: values()) {
			tmp.put(status.getDbValue(), status);
		}
		return tmp;
	}
}