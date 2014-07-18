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
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.utils.FileIoUtils;

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
	 * fetch raw script logs
	 * @param logId
	 */
	public static void fetchRawLogs(String logId) {
		
		try {
			
			String alert = null;
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.CMDLOG);
			ILog log = logger.readLog(logId);
			if (log.isRawLogsFetched()) {
				alert = String.format("Raw logs for job %s are already available in full text search", log.uuid());
			}
			else {
				HttpTaskRequest taskReq = new HttpTaskRequest();
				UrlTemplate urlTemplate = new UrlTemplate("https://<host>:12020/status/guidoutput/<guid>", HttpMethod.GET);
				taskReq.setSyncTaskOptions(TaskResourcesProvider.HTTP_CLIENT, urlTemplate, new ExecuteOption(), AgentResourceProvider.AGENT_PROCESSOR);
				taskReq.setHosts(log.getNodeGroup().getHosts());
				taskReq.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue("guid", log.uuid()));
				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);

				StandaloneTaskListener listener = new StandaloneTaskListener();
				listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventUpdater(log));
				
				new StandaloneTaskExecutor(new BatchOption(), listener, task).execute();
				
				log.setRawLogsFetched(true);
				logger.saveLog(log);
				alert = String.format("Fetch raw script logs for job %s, it will be available in full text search", log.uuid());
			}
			
			redirect("Logs.cmdLogs", alert);
			
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

}

	/**
	 * show logs
	 * @param date
	 */
	public static void cmdLogs(String alert) {

		String page = "cmdlogs";
		String topnav = "commands";

		try {
			
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
				if (idx++ <= 50) {
					ILog logImpl = logger.readLog(logName);
					log.put("status", logImpl.getStatus());
					log.put("statusdetail", logImpl.getStatusDetail());
					log.put("progress", logImpl.getDisplayProgress());
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

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

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
				if (idx++ <= 50) {
					ILog logImpl = logger.readLog(logName);
					log.put("status", logImpl.getStatus());
					log.put("statusdetail", logImpl.getStatusDetail());
					log.put("progress", logImpl.getDisplayProgress());
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

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
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

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
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

			String fileContent = VarUtils.vf.readFileToString(filePath);
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
							String aggRegEx,
							String nav) 
	{
		String page = "cmdLogs";
		String topnav = StringUtil.isNullOrEmpty(nav) ? "logs" : nav;
		
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
				agentCommandType = log.getCommandKey();
				nodeGroupType = log.getNodeGroup().getName();
				dataType = log.getNodeGroup().getType();
				regExs = UserDataProvider.getCommandConfigs().getCommandByName(log.getCommandKey()).getAggRegexs();
				logAggregation = log.aggregate(aggField, aggRegEx);
			}
			else if (alog instanceof JobLog)  {
				JobLog log = (JobLog) alog;
				agentCommandType = log.getCommandKey();
				nodeGroupType = log.getNodeGroup().getName();
				dataType = log.getNodeGroup().getType();
				regExs = UserDataProvider.getCommandConfigs().getCommandByName(log.getCommandKey()).getAggRegexs();
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
		
		redirect("Logs.exploreFiles", (String) null);

	}



}
