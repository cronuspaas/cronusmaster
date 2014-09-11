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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jobs.PlayVarUtils;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.BatchOption;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.utils.FileIoUtils;
import resources.utils.JsonResponse;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider;
import com.stackscaling.agentmaster.resources.UserDataProvider;
import com.stackscaling.agentmaster.resources.agent.AgentResourceProvider;
import com.stackscaling.agentmaster.resources.log.BaseLog;
import com.stackscaling.agentmaster.resources.log.CmdLog;
import com.stackscaling.agentmaster.resources.log.FlowLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;
import com.stackscaling.agentmaster.resources.log.ILog;
import com.stackscaling.agentmaster.resources.log.JobLog;
import com.stackscaling.agentmaster.resources.log.LogAggregation;
import com.stackscaling.agentmaster.resources.log.LogAggregation.LogAggregationItem;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
import com.stackscaling.agentmaster.resources.utils.DateUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * 
 * @author ypei
 *
 */
public class Logs extends Controller {
	
	/**
	 * show logs
	 * @return
	 * @throws IOException
	 */
	private static List<Map<String, String>> cmdLogsInternal() throws IOException {
		IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.CMDLOG);
		List<String> logs = logger.listLogs();
		ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
		
		int idx = 0;
		for (String logName : logs) {
			Map<String, String> logMeta = BaseLog.getLogMetaFromName(logName);
			HashMap<String, String> log = new HashMap<String, String>();
			log.putAll(logMeta);
			log.put("name", logName);
			log.put("type", DataType.CMDLOG.name());
			if (idx++ <= PlayVarUtils.listLogSize) {
				ILog logImpl = logger.readLog(logName);
				log.put("status", logImpl.getStatus());
				log.put("statusdetail", logImpl.getStatusDetail());
				String userData = DataUtil.getOptionValue(logImpl.getUserData(), "var_values", "{}").trim();
				log.put("userData", userData);
				log.put("fetched", "true");
			}
			else {
				log.put("status", "-");
				log.put("statusdetail", "-");
				log.put("userData", "...");
//				log.put("userDataConcise", "...");
				log.put("fetched", "false");
			}
			logFiles.add(log);
		}
		return logFiles;
	}

	/**
	 * show logs
	 * @param date
	 */
	public static void cmdLogs(String alert) {

		String page = "cmdlogs";
		String topnav = "commands";

		try {

			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
			List<Map<String, String>> logFiles = cmdLogsInternal();
			render(page, topnav, logFiles, lastRefreshed, alert);

		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	/**
	 * show logs
	 * @param date
	 */
	public static void cmdLogsJson() {

		try {

			List<Map<String, String>> logFiles = cmdLogsInternal();
			renderJSON(JsonResponse.successResponse(null).addResult("logs", logFiles));

		} catch (Exception e) {
			renderJSON(JsonResponse.failedResponse(StringUtil.getStackTrace(e, 1000)));
		}

	}

	/**
	 * job logs
	 * @param date
	 */
	public static void jobLogs(String alert) {

		String page = "joblogs";
		String topnav = "jobs";

		try {
			
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.JOBLOG);
			List<String> logs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			int idx = 0;
			for (String logName : logs) {
				Map<String, String> logMeta = BaseLog.getLogMetaFromName(logName);
				HashMap<String, String> log = new HashMap<String, String>();
				log.putAll(logMeta);
				log.put("name", logName);
				log.put("type", DataType.JOBLOG.name());
				if (idx++ <= PlayVarUtils.listLogSize) {
					ILog logImpl = logger.readLog(logName);
					log.put("status", logImpl.getStatus());
					log.put("statusdetail", logImpl.getStatusDetail());
					if (logImpl.isHasRawLogs()) {
						log.put("fetch", "true");
					}
				}
				else {
					log.put("status", "-");
					log.put("statusdetail", "-");
					log.put("progress", "-");
					log.put("fetch", "false");
				}
				logFiles.add(log);
			}
			// List<>

			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
			Collections.sort(logFiles, new Comparator<Map<String, String>>(){

				@Override
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return 0-(o1.get("timeStamp").compareTo(o2.get("timeStamp")));
					
				}});

			render(page, topnav, logFiles, lastRefreshed, alert);
			
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	/**
	 * job logs
	 * @param date
	 */
	public static void wfLogs(String alert) {

		String page = "wflogs";
		String topnav = "workflows";

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

			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
			Collections.sort(logFiles, new Comparator<Map<String, String>>(){

				@Override
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return 0-(o1.get("timeStamp").compareTo(o2.get("timeStamp")));
					
				}});

			render(page, topnav, logFiles, lastRefreshed, alert);
			
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
			renderJSON(DataUtil.jsonResult("Error occured in index of logs"));
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
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(dtype);
			logger.deleteLog(name);
			
			// redirect with message
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
			String alert = String.format("%s deleted", name);
			redirect(redirectTarget, alert);
			
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

			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
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

			String fileContent = VarUtils.vf.readFileToString(filePath);
			renderText(fileContent);
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in getFileContent of logs"
					+ DateUtils.getNowDateTimeDotStr());
		}

	}

	/**
	 * index page
	 */
	public static void index() {
		
		redirect("Logs.exploreFiles", (String) null);

	}


	/**
	 * index page
	 */
	public static void search() {
		
		redirect("http://localhost:9200", true);

	}


}
