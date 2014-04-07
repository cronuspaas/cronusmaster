package resources.log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lightj.util.StringUtil;

import resources.log.JobLog.CommandResponse;

public class LogAggregation {
	
	static final int MAX_CONTENT_LEN = 200;
	
	private final Map<String, LogAggregationItem> aggregations = new HashMap<String, LogAggregation.LogAggregationItem>();
	
	private String matchField;
	private String matchRegEx;
	private Field matchingField;
	private Pattern pattern;
	
	public LogAggregation(String matchField, String matchRegEx) {
		this.matchField = matchField;
		this.matchRegEx = matchRegEx;
		if (!StringUtil.isNullOrEmptyAfterTrim(matchRegEx)) {
			this.pattern = Pattern.compile(matchRegEx);
		}
		try {
			this.matchingField = CommandResponse.class.getField(matchField);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public Map<String, LogAggregationItem> getAggregations() {
		return aggregations;
	}

	public void aggregateEntry(CommandResponse cmdRes) {
		try {
			Object value = matchingField.get(cmdRes);
			String strValue = value!=null ? value.toString() : "null";
			String matchValue = null;
			if (pattern != null) {
				Matcher m = pattern.matcher(strValue);
				matchValue = m.find() ? m.group(1) : m.group();
			} else {
				matchValue = strValue;
			}
			if (!aggregations.containsKey(matchValue)) {
				aggregations.put(matchValue, new LogAggregationItem(matchRegEx, matchField, matchValue));
			}
			aggregations.get(matchValue).addHost(cmdRes.host);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

	public static class LogAggregationItem {
		/** user input */
		public String matchRegEx;
		public String matchField;
		/** result */
		public int count;
		public List<String> hosts = new ArrayList<String>();
		public String matchValue;
		public LogAggregationItem(String matchRegEx, String matchField, String matchValue) {
			this.matchField = matchField;
			this.matchRegEx = matchRegEx;
			this.matchValue = matchValue;
		}
		public void addHost(String host) {
			hosts.add(host);
			count++;
		}
		
	}
	

}
