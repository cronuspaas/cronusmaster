package resources.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import org.lightj.util.StringUtil;

/**
 * data util
 * @author biyu
 *
 */
public class DataUtil {
	
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
	
	public static String getOptionValue(Map<String, String> options, String key, String defVal) {
		return (options.containsKey(key) && !StringUtil.isNullOrEmpty(options.get(key))) ? options.get(key) : defVal;
	}
	
	public static Map<String, String> createResultItem(String key, String value) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("id", key);
		item.put("value", value);
		return item;
	}	
	
	public static JsonResult jsonResult(String result) {
		return new JsonResult(result);
	}

	public static class JsonResult {

		public String result;
		public String time;
		
		public JsonResult(String result) {
			super();
			this.result = result;
			this.time = DateUtils.getNowDateTimeStrSdsm().toString();
		}
	}
}
