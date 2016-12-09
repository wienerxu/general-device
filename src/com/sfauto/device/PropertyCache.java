package com.sfauto.device;

import java.util.HashMap;
import java.util.Map;

public class PropertyCache {
	public static PropertyCache cache = null;

	public static PropertyCache getInstance() {
		if (cache == null) {
			cache = new PropertyCache();
		}
		return cache;
	}

	Map<Integer, TimedInteger> intCache = null;
	Map<Integer, TimedFloat> floatCache = null;
	Map<Integer, TimedString> strCache = null;

	private PropertyCache() {
		intCache = new HashMap<Integer, TimedInteger>();
		floatCache = new HashMap<Integer, TimedFloat>();
		strCache = new HashMap<Integer, TimedString>();
	}

	public void putInt(int id, int value) {
		long time = System.currentTimeMillis();
		if (intCache.containsKey(id)) {
			TimedInteger ti = intCache.get(id);
			ti.value = value;
			ti.time = time;
		} else {
			intCache.put(id, new TimedInteger(time, value));
		}
	}

	public void putFloat(int id, float value) {
		long time = System.currentTimeMillis();
		if (floatCache.containsKey(id)) {
			TimedFloat tf = floatCache.get(id);
			tf.value = value;
			tf.time = time;
		} else {
			floatCache.put(id, new TimedFloat(time, value));
		}
	}

	public void putString(int id, String value) {
		long time = System.currentTimeMillis();
		if (strCache.containsKey(id)) {
			TimedString ts = strCache.get(id);
			ts.value = value;
			ts.time = time;
		} else {
			strCache.put(id, new TimedString(time, value));
		}
	}

	public int getInt(int id) {
		if (intCache.containsKey(id)) {
			return intCache.get(id).value;
		} else {
			return 0;
		}
	}

	public long getIntTime(int id) {
		if (intCache.containsKey(id)) {
			return intCache.get(id).time;
		} else {
			return 0;
		}
	}
	
	public float getFloat(int id) {
		if (floatCache.containsKey(id)) {
			return floatCache.get(id).value;
		} else {
			return 0f;
		}
	}

	public long getFloatTime(int id) {
		if (floatCache.containsKey(id)) {
			return floatCache.get(id).time;
		} else {
			return 0;
		}
	}
	
	public String getString(int id) {
		if (strCache.containsKey(id)) {
			return strCache.get(id).value;
		} else {
			return "";
		}
	}

	public long getStringTime(int id) {
		if (strCache.containsKey(id)) {
			return strCache.get(id).time;
		} else {
			return 0;
		}
	}
	
	class TimedInteger {
		long time;
		int value;

		public TimedInteger(long time, int value) {
			this.time = time;
			this.value = value;
		}
	}

	class TimedFloat {
		long time;
		float value;

		public TimedFloat(long time, float value) {
			this.time = time;
			this.value = value;
		}
	}

	class TimedString {
		long time;
		String value;

		public TimedString(long time, String value) {
			this.time = time;
			this.value = value;
		}
	}
}
