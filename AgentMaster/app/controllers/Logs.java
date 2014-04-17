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
package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;
import resources.log.BaseLog;
import resources.log.CmdLog;
import resources.log.FlowLog;
import resources.log.IJobLogger;
import resources.log.ILog;
import resources.log.JobLog;
import resources.log.LogAggregation;
import resources.log.LogAggregation.LogAggregationItem;
import resources.utils.DateUtils;
import resources.utils.FileIoUtils;
import resources.utils.DataUtil.JsonResult;

/**
 * 
 * @author ypei
 *
 */
public class Logs extends Controller {

	/**
	 * show logs
	 * @param date
	 */
	public static void cmdLogs() {

		String page = "cmdlogs";
		String topnav = "logs";

		try {
			
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.CMDLOG);
			List<String> logs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			for (String logName : logs) {
				Map<String, String> logMeta = BaseLog.getLogMetaFromName(logName);
				HashMap<String, String> log = new HashMap<String, String>();
				log.putAll(logMeta);
				log.put("name", logName);
				log.put("type", DataType.CMDLOG.name());
				logFiles.add(log);
			}
			// List<>

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
			Collections.sort(logFiles, new Comparator<Map<String, String>>(){

				@Override
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return 0-(o1.get("timeStamp").compareTo(o2.get("timeStamp")));
					
				}});

			render(page, topnav, logFiles, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	/**
	 * job logs
	 * @param date
	 */
	public static void jobLogs() {

		String page = "joblogs";
		String topnav = "logs";

		try {
			
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.JOBLOG);
			List<String> logs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			for (String logName : logs) {
				Map<String, String> logMeta = BaseLog.getLogMetaFromName(logName);
				HashMap<String, String> log = new HashMap<String, String>();
				log.putAll(logMeta);
				log.put("name", logName);
				log.put("type", DataType.JOBLOG.name());
				logFiles.add(log);
			}
			// List<>

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
			Collections.sort(logFiles, new Comparator<Map<String, String>>(){

				@Override
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return 0-(o1.get("timeStamp").compareTo(o2.get("timeStamp")));
					
				}});

			render(page, topnav, logFiles, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	/**
	 * job logs
	 * @param date
	 */
	public static void wfLogs() {

		String page = "wflogs";
		String topnav = "logs";

		try {
			
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.FLOWLOG);
			List<String> logs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			for (String logName : logs) {
				Map<String, String> logMeta = FlowLog.getLogMetaFromName(logName);
				HashMap<String, String> log = new HashMap<String, String>();
				log.putAll(logMeta);
				log.put("name", logName);
				log.put("type", DataType.FLOWLOG.name());
				logFiles.add(log);
			}
			// List<>

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
			Collections.sort(logFiles, new Comparator<Map<String, String>>(){

				@Override
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return 0-(o1.get("timeStamp").compareTo(o2.get("timeStamp")));
					
				}});

			render(page, topnav, logFiles, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	/**
	 * download log file
	 * @param type
	 * @param name
	 */
	public static void download(String type, String name) {

		try {

			DataType dtype = DataType.valueOf(type);
			String fileContent = UserDataProvider.getUserDataDao().readData(dtype, name);
			
			renderJSON(fileContent);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON(new JsonResult("Error occured in index of logs"));
		}

	}

	/**
	 * delete log file
	 * @param type
	 * @param name
	 */
	public static void delete(String type, String name) {

		try {
			
			DataType dtype = DataType.valueOf(type.toUpperCase());
			UserDataProvider.getUserDataDao().deleteData(dtype, name);
			String redirectTarget = null;
			switch(dtype) {
			case CMDLOG:
				redirectTarget = "Logs.cmdLogs";
				break;
			case JOBLOG:
				redirectTarget = "Logs.jobLogs";
				break;
			case FLOWLOG:
				redirectTarget = "Logs.wfLogs";
				break;
			}
			redirect(redirectTarget);
			
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON(t);
		}

	}
	
	/**
	 * Generic display any files.
	 * 
	 * @param path
	 */
	public static void exploreFiles(String path) {

		if (path == null) {
			path = new String("");
		}
		String page = "exploreFiles";
		String topnav = "logs";

		try {

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
			List<String> fileNames = new ArrayList<String>();
			List<String> dirNames = new ArrayList<String>();

			FileIoUtils.getFileAndDirNamesInFolder(path, fileNames, dirNames);

			render(page, topnav, fileNames, dirNames, path, lastRefreshed);

		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in exploreFiles of logs");
		}

	}// end func

	/**
	 * file content
	 * @param filePath
	 */
	public static void getFileContent(String filePath) {

		try {

			String fileContent = FileIoUtils.readFileToString(filePath);
			renderText(fileContent);
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in getFileContent of logs"
					+ DateUtils.getNowDateTimeStrSdsm());
		}

	}

	/**
	 * aggregate result
	 */
	public static void aggregate(
							String logType,
							String logId,
							String aggField,
							String aggRegEx) 
	{
		String page = "cmdLogs";
		String topnav = "logs";
		String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
		
		try {
			DataType type = DataType.valueOf(logType.toUpperCase());
			ILog alog = UserDataProvider.getJobLoggerOfType(type).readLog(logId);
			String agentCommandType = null;
			String nodeGroupType = null;
			String dataType = null;
			List<String> regExs = null;
			LogAggregation logAggregation = null;
			
			if (alog instanceof CmdLog) {
				CmdLog log = (CmdLog) alog;
				agentCommandType = log.getUserCommand().cmd.getName();
				nodeGroupType = log.getNodeGroup().getName();
				dataType = log.getNodeGroup().getType();
				regExs = log.getUserCommand().cmd.getAggRegexs();
				logAggregation = log.aggregate(aggField, aggRegEx);
			}
			else if (alog instanceof JobLog)  {
				JobLog log = (JobLog) alog;
				agentCommandType = log.getUserCommand().cmd.getName();
				nodeGroupType = log.getNodeGroup().getName();
				dataType = log.getNodeGroup().getType();
				regExs = log.getUserCommand().cmd.getAggRegexs();
				logAggregation = log.aggregate(aggField, aggRegEx);
			}
			
			ArrayList<Map<String, String>> aggList = new ArrayList<Map<String,String>>();
			for (Entry<String, LogAggregationItem> aggEntry : logAggregation.getAggregations().entrySet()) {
				Map<String, String> agg = new HashMap<String, String>();
				agg.put("value", aggEntry.getKey());
				agg.put("matchField", aggField);
				agg.put("matchRegEx", aggRegEx);
				agg.put("nodeCount", Integer.toString(aggEntry.getValue().count));
				agg.put("nodes", StringUtil.join(aggEntry.getValue().hosts, "\n"));
				aggList.add(agg);
			}
			HashMap<String, String> logMeta = new HashMap<String, String>();
			logMeta.put("logType", logType);
			logMeta.put("logId", logId);
			logMeta.put("aggField", aggField);
			render(page, topnav, aggList, lastRefreshed, agentCommandType, nodeGroupType, dataType, regExs, logMeta);
		} 
		catch (Exception e) {
			e.printStackTrace();
			error(e);
		}
	}

	/**
	 * index page
	 */
	public static void index() {
		
		redirect("Logs.cmdLogs", (String) null);

	}



}
