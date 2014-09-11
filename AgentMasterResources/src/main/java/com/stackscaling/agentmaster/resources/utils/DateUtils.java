/*

Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package com.stackscaling.agentmaster.resources.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * deal with different date format
 */
public class DateUtils {

	static Logger LOG = LoggerFactory.getLogger(DateUtils.class);

	static final Date EPOCH = new Date(0);

	static final String DT_STR_FMT = "yyyy-MM-dd HH:mm:ss.SSSZ";
	static SimpleDateFormat DT_STR_FMTR = new SimpleDateFormat(DT_STR_FMT);

	public static String getDateTimeStr(Date d) {
		if (d == null) return "";
		String str = DT_STR_FMTR.format(d);
		return str;
	}

	public static Date fromDateTimeStr(String dateTimeStr) {

		try {

			return DT_STR_FMTR.parse(dateTimeStr);

		} catch (ParseException e) {
			LOG.error(e.getMessage());
			return EPOCH;
		}
	}

	static final String DTL_STR_FMT = "yyyy-MM-dd HH:mm:ss";
	static SimpleDateFormat DTL_STR_FMTR = new SimpleDateFormat(DTL_STR_FMT);

	public static String getDateTimeLocalStr(Date d) {
		if (d == null) return "";
		String str = DT_STR_FMTR.format(d);
		return str;
	}

	public static Date fromDateTimeLocalStr(String dateTimeStr) {

		try {
			return DT_STR_FMTR.parse(dateTimeStr);
		} catch (ParseException e) {
			LOG.error(e.getMessage());
			return EPOCH;
		}
	}

	static final String DTS_STR_FMT = "yyyy.MM.dd.HH.mm.ss.SSSZ";
	static SimpleDateFormat DTS_STR_FMTR = new SimpleDateFormat(DTS_STR_FMT);

	public static String getDateTimeDotStr(Date d) {
		if (d == null) return "";
		if (d.getTime() == 0L) return "Never";
		String str = DTS_STR_FMTR.format(d);
		return str;
	}

	public static Date fromDateTimeDotStr(String dateTimeStr) {

		try {

			return DTS_STR_FMTR.parse(dateTimeStr);

		} catch (ParseException e) {
			LOG.error(e.getMessage());
			return EPOCH;
		}
	}

	static String DTS_CONCISE_STR_FMTR = "yyyyMMddHHmmss";
	static final SimpleDateFormat DTS_CONCISE = new SimpleDateFormat(DTS_CONCISE_STR_FMTR);

	public static String getDateTimeStrConcise(Date d) {
		if (d == null)
			return "";
		String str = DTS_CONCISE.format(d);
		return str;
	}

	/**
	 * 20130512 Converts the sdsm string generated above to Date format
	 * 
	 * @param str
	 * @return
	 */
	public static Date getDateFromConciseStr(String str) {

		Date d = null;
		if (StringUtil.isNullOrEmpty(str))
			return null;

		try {
			d = DTS_CONCISE.parse(str);
		} catch (Exception ex) {
			LOG.error("%s, %s, %s", ex.getMessage(),
					"Exception while converting string to date : " + str);
		}

		return d;
	}

	public static String getNowDateTimeStr() {
		return getDateTimeStr(new Date());
	}

	public static String getNowDateTimeDotStr() {
		return getDateTimeDotStr(new Date());
	}

	public static String getNowDateTimeStrConcise() {
		return getDateTimeStrConcise(new Date());
	}

}
