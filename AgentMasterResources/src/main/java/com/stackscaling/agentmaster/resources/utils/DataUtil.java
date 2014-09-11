package com.stackscaling.agentmaster.resources.utils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * data util
 * @author biyu
 *
 */
public class DataUtil {

	/**
	 * remove from map any empty or zero values
	 * @param source
	 * @return
	 */
	public static Map<String, String> removeNullAndZero(Map<String, String> source) {
		// remove all zero value from options before saving
		HashMap<String, String> result = new HashMap<String, String>(source);
		for (Iterator<Entry<String, String>> iter = result.entrySet().iterator(); iter.hasNext();) {
			Entry<String, String> entry = iter.next();
			if (StringUtil.isNullOrEmptyAfterTrim(entry.getValue()) || StringUtil.equalIgnoreCase("0", entry.getValue())) {
				iter.remove();
			}
		}
		return result;
	}

	/**
	 * retrieve from map with key, with default value if key not exist
	 * @param options
	 * @param key
	 * @param defVal
	 * @return
	 */
	public static String getOptionValue(Map<String, String> options, String key, String defVal) {
		return (options.containsKey(key) && !StringUtil.isNullOrEmpty(options.get(key))) ? options.get(key) : defVal;
	}

	/**
	 * decode recursively a json string into map of object (nested arrays or maps)
	 * @param jsonMap
	 * @return
	 * @throws IOException
	 */
	public static void decode(String key, String jsonVal, Map<String, Object> result) throws IOException
	{
		result.put(key, decodeRecursive(jsonVal));
	}

	/**
	 * decode recursively a json string to corresponding object (nested arrays or maps)
	 * @param jsonVal
	 * @return
	 * @throws IOException
	 */
	public static Object decodeRecursive(String jsonVal) throws IOException
	{
		Object value = null;
		if (jsonVal.startsWith("[") && jsonVal.endsWith("]")) {
			// string array
			String[] sValues = JsonUtil.decode(jsonVal, String[].class);
			Object tmpValue = decodeRecursive(sValues[0]);
			Class klass = tmpValue.getClass();
			value = Array.newInstance(klass, sValues.length);
			for (int i = 0; i < sValues.length; i++) {
				tmpValue = decodeRecursive(sValues[i]);
				Array.set(value, i, tmpValue);
			}
		}
		else if (jsonVal.startsWith("{") && jsonVal.endsWith("}")) {
			// string map
			HashMap<String, String> sValues = JsonUtil.decode(jsonVal, new TypeReference<HashMap<String, String>>(){});
			HashMap<String, Object> tmpValues = new HashMap<String, Object>();
			value = tmpValues;
			for (Entry<String, String> entry : sValues.entrySet()) {
				tmpValues.put(entry.getKey(), decodeRecursive(entry.getValue()));
			}
		}
		else {
			value = jsonVal;
		}
		return value;
	}

	/**
	 * turn nvp map into array of id, value maps
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, String> createResultItem(String key, String value) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("id", key);
		item.put("value", value);
		return item;
	}



	/**
	 * turn a result string into json result
	 * @param result
	 * @return
	 */
	public static JsonResult jsonResult(String result) {
		return new JsonResult(result);
	}

	static class JsonResult {

		public String result;
		public String time;

		public JsonResult(String result) {
			super();
			this.result = result;
			this.time = DateUtils.getNowDateTimeDotStr();
		}
	}
}
