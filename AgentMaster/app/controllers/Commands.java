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

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.MapListPrimitiveJsonParser;
import org.lightj.util.SpringContextUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.utils.JsonResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider.LogTaskEventHandler;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.command.BaseCommandData;
import com.stackscaling.agentmaster.resources.command.ICommand;
import com.stackscaling.agentmaster.resources.command.ICommandData;
import com.stackscaling.agentmaster.resources.command.ICommandEnhancer;
import com.stackscaling.agentmaster.resources.log.CmdLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;
import com.stackscaling.agentmaster.resources.log.ILog;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroup;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroupData;
import com.stackscaling.agentmaster.resources.oneclickcommand.IOneClickCommand;
import com.stackscaling.agentmaster.resources.oneclickcommand.IOneClickCommandData;
import com.stackscaling.agentmaster.resources.oneclickcommand.OneClickCommandImpl;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * 
 * @author ypei
 *
 */
public class Commands extends Controller {
	
	static Comparator<Map<String, String>> cmdComparator = new Comparator<Map<String, String>>(){

		@Override
		public int compare(Map<String, String> o1,
				Map<String, String> o2) {
			return o1.get("name").compareTo(o2.get("name"));
			
		}
	};
		
		
	static Comparator<Map<String, String>> oneclickComparator = new Comparator<Map<String, String>>(){

		@Override
		public int compare(Map<String, String> o1,
				Map<String, String> o2) {
			return o1.get("name").compareTo(o2.get("name"));
			
		}
	};
	
	/**
	 * commands index
	 * @throws Exception
	 */
	private static List<Map<String, String>> indexInternal() throws Exception 
	{
		Map<String, ICommand> cmds = UserDataProviderFactory.getCommandConfigs().getAllCommands();
		List<Map<String, String>> commands = new ArrayList<Map<String,String>>();
		for (Entry<String, ICommand> entry : cmds.entrySet()) {
			Map<String, String> values = new HashMap<String, String>();
			ICommand cmd = entry.getValue();
			values.put("name", entry.getKey());
			UrlTemplate req = cmd.createCopy().getUrlTemplate();
			values.put("url", req.getUrl());
			values.put("httpMethod", req.getMethod().name());
			StringBuffer headers = new StringBuffer();
			if (req.getHeaders() != null) {
				for (Entry<String, String> header : req.getHeaders().entrySet()) {
					headers.append(String.format("%s=%s", header.getKey(), header.getValue())).append(",");
				}
			}
			values.put("headers", headers.toString());
			if (req.getBody()!=null && !req.getBody().isEmpty()) {
				values.put("body", JsonUtil.encode(req.getBody()));
			}
			values.put("variables", StringUtil.join(req.getVariableNames(), ", "));
			values.put("userData", JsonUtil.encode(cmd.getUserData()));
			values.put("category", cmd.getCategory());
			StringBuffer parameters = new StringBuffer();
			if (req.getParameters() != null) {
				for (Entry<String, String> param : req.getParameters().entrySet()) {
					parameters.append(String.format("%s=%s", param.getKey(), param.getValue())).append(",");
				}
			}
			values.put("parameters", parameters.toString());
			commands.add(values);
		}
		
		Collections.sort(commands, cmdComparator);
		return commands;
	}

	/**
	 * commands json
	 */
	public static void indexJson() {

		try {
			renderJSON(JsonResponse.successResponse(null).addResult("commands", indexInternal()));
		} catch (Exception e) {
			renderJSON(JsonResponse.failedResponse(StringUtil.getStackTrace(e, 1000)));
		}

	}
	
	/**
	 * list of commands
	 */
	public static void index(String alert) {

		String page = "index";
		String topnav = "commands";

		try {
			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
			List<Map<String, String>> commands = indexInternal(); 
			render(page, topnav, commands, lastRefreshed, alert);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}
	
	/**
	 * oneclick commands
	 * @return
	 */
	private static List<Map<String, String>> oneclickInternal() throws Exception {
		Map<String, IOneClickCommand> cmds = UserDataProviderFactory.getOneClickCommandConfigs().getAllCommands();
		List<Map<String, String>> commands = new ArrayList<Map<String,String>>();
		for (Entry<String, IOneClickCommand> entry : cmds.entrySet()) {
			Map<String, String> values = new HashMap<String, String>();
			IOneClickCommand cmd = entry.getValue();
			values.put("name", entry.getKey());
			values.put("command", cmd.getCommandKey());
			values.put("nodeGroup", cmd.getNodeGroupKey());
			values.put("description", cmd.getDescription());
			String userData = DataUtil.getOptionValue(cmd.getUserData(), "var_values", "{}").trim();
			values.put("userData", userData);
			commands.add(values);
		}
		
		Collections.sort(commands, oneclickComparator);
		return commands;
		
	}

	/**
	 * show one click commands
	 */
	public static void oneclick(String alert) {

		String page = "oneclick";
		String topnav = "commands";

		try {
			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
			List<Map<String, String>> commands = oneclickInternal(); 
			render(page, topnav, commands, lastRefreshed, alert);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}
	}
	
	/**
	 * one click commands
	 */
	public static void oneclickJson() {

		try {
			renderJSON(JsonResponse.successResponse(null).addResult("commands", oneclickInternal()));
		} catch (Exception e) {
			renderJSON(JsonResponse.failedResponse(StringUtil.getStackTrace(e, 1000)));
		}

	}

	
	/**
	 * command wizard
	 */
	public static void wizard(String dataId, String dataType) {

		String page = "wizard";
		String topnav = "commands";

		try {
			Map<String, INodeGroup> ngsMap = UserDataProviderFactory.getNodeGroupOfType(DataType.NODEGROUP).getAllNodeGroups();
			ArrayList<Map<String, String>> ngs = new ArrayList<Map<String, String>>();
			for (String v : ngsMap.keySet()) {
				Map<String, String> kvp = new HashMap<String, String>(1);
				kvp.put("nodeGroupType", v);
				ngs.add(kvp);
			}
			String nodeGroupSourceMetadataListJsonArray = JsonUtil.encode(ngs);
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			
			ICommand cmd = null;
			switch (dType) {
			case COMMAND:
				cmd = UserDataProviderFactory.getCommandConfigs().getCommandByName(dataId);
				break;

			case CMDLOG:
				ILog log = UserDataProviderFactory.getJobLoggerOfType(dType).readLog(dataId);
				String agentCommandType = log.getCommandKey();
				cmd = UserDataProviderFactory.getCommandConfigs().getCommandByName(agentCommandType);
			}

			String cmdName = cmd.getName();
			render(page, topnav, nodeGroupSourceMetadataListJsonArray, cmdName, dataType, dataId);
		} catch (Throwable t) {

			t.printStackTrace();
			renderJSON(DataUtil.jsonResult("Error occured in wizard"));
		}

	}

	/**
	 * options for a command
	 * @param dataId
	 */
	public static void getOptions(String dataId, String dataType) {
		
		try {
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			
			ArrayList<Map<String, String>> result = new ArrayList<Map<String,String>>();
			ICommand cmd = null;
			switch (dType) {
			case COMMAND:
				cmd = UserDataProviderFactory.getCommandConfigs().getCommandByName(dataId);
				if (cmd.getUserData() != null) {
					result.add(createResultItem("var_values", JsonUtil.encodePretty(cmd.getUserData())));
				}
				break;

			case CMDLOG:
				ILog log = UserDataProviderFactory.getJobLoggerOfType(dType).readLog(dataId);
				String agentCommandType = log.getCommandKey();
				Map<String, String> options = log.getUserData();
				ICommandData userConfigs = UserDataProviderFactory.getCommandConfigs();

				// build task
				cmd = userConfigs.getCommandByName(agentCommandType);
				String userData = DataUtil.getOptionValue(options, "var_values", "{}").trim();
				result.add(createResultItem("var_values", userData));
			}

			HttpTaskRequest req = cmd.createCopy();

			// enhance the request by its category
			ICommandEnhancer cmdEnhancer = UserDataProviderFactory.getCommandEnhancer(cmd.getCategory());
			cmdEnhancer.enhanceRequest(req);

			// get options for execution
			ExecuteOption eo = req.getExecutionOption()==null ? new ExecuteOption() : req.getExecutionOption();
			result.add(createResultItem("exe_initde", Long.toString(eo.getInitDelaySec())));
			result.add(createResultItem("exe_to", Long.toString(eo.getTimeOutSec())));
			result.add(createResultItem("exe_retry", Long.toString(eo.getMaxRetry())));
			result.add(createResultItem("exe_rede", Long.toString(eo.getRetryDelaySec())));

			result.add(createResultItem("mon_disabled", Boolean.toString(HttpTaskRequest.TaskType.async.name().equals(req.getTaskType()))));
			MonitorOption mo = req.getMonitorOption()==null ? new MonitorOption() : req.getMonitorOption();
			result.add(createResultItem("mon_int", Long.toString(mo.getIntervalSec())));
			result.add(createResultItem("mon_initde", Long.toString(mo.getInitDelaySec())));
			result.add(createResultItem("mon_to", Long.toString(mo.getTimeOutSec())));
			result.add(createResultItem("mon_retry", Long.toString(mo.getMaxRetry())));
			result.add(createResultItem("mon_rede", Long.toString(mo.getRetryDelaySec())));

			BatchOption bo = req.getBatchOption()==null ? new BatchOption(0, Strategy.UNLIMITED) : req.getBatchOption();
			result.add(createResultItem("thrStrategy", bo.getStrategy().name()));
			result.add(createResultItem("thr_rate", Integer.toString(bo.getConcurrentRate())));
			
			renderJSON(result);
			
		} catch (Throwable t) {

			t.printStackTrace();
			renderJSON(DataUtil.jsonResult("Error occured in wizard"));
		}
		
	}
	
	private static Map<String, String> createResultItem(String key, String value) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("id", key);
		item.put("value", value);
		return item;
	}
	
	
	/**
	 * run oneclick command
	 * @param logType
	 * @param logId
	 */
	private static String oneclickRunInternal(String dataId, String ngName) throws Exception 
	{
		IOneClickCommand oneclickCmd = UserDataProviderFactory.getOneClickCommandConfigs().getCommandByName(dataId);
		
		ICommandData userConfigs = UserDataProviderFactory.getCommandConfigs();
		INodeGroupData ngConfigs = UserDataProviderFactory.getNodeGroupOfType(DataType.NODEGROUP);

		// build task
		ICommand cmd = userConfigs.getCommandByName(oneclickCmd.getCommandKey());
		String[] hosts = null;
		INodeGroup ng = null;
		if (!StringUtil.isNullOrEmpty(ngName)) {
			ng = ngConfigs.getNodeGroupByName(ngName);
			hosts = ng.getHosts();
		}
		else if (!StringUtil.isNullOrEmpty(oneclickCmd.getNodeGroupKey())) {
			ng = ngConfigs.getNodeGroupByName(oneclickCmd.getNodeGroupKey());
			hosts = ng.getHosts();
		}
		else {
			ng = INodeGroupData.NG_EMPTY;
		}

		Map<String, String> options = oneclickCmd.getUserData();
		String varValues = DataUtil.getOptionValue(options, "var_values", "{}").trim();
		Map<String, Object> userData = (Map<String, Object>) MapListPrimitiveJsonParser.parseJson(varValues);
		HttpTaskRequest httpTask = BaseCommandData.createTaskByRequest(hosts, cmd, options, userData);

		CmdLog cmdLog = prepareLog(cmd, ng, options, httpTask);
		
		// fire task
		ExecutableTask reqTask = HttpTaskBuilder.buildTask(httpTask);
		StandaloneTaskListener listener = new StandaloneTaskListener();
		int numOfHost = hosts!=null ? hosts.length : 1;
		LogTaskEventHandler handler = new TaskResourcesProvider.LogTaskEventHandler(cmdLog, numOfHost);
		handler.saveLog(true);
		listener.setDelegateHandler(handler);
		new StandaloneTaskExecutor(httpTask.getBatchOption(), listener, reqTask).execute();

		return cmdLog.uuid();
	}
	
	/**
	 * run oneclick command
	 * @param logType
	 * @param logId
	 */
	public static void oneclickRun(String dataId) {

		try {

			String uuid = oneclickRunInternal(dataId, null);
			String alert = String.format("%s launched successfully.", uuid);
			redirect("Logs.cmdLogs", alert);

		} catch (Exception e) {
			e.printStackTrace();
			error(String.format("Error occur in job wizard, %s", e.getLocalizedMessage()));
		}

	}

	/**
	 * run oneclick command
	 * @param dataId
	 * @param ngName: override nodegroup
	 */
	public static void oneclickRunJson(String dataId, String ngName) {

		try {

			String uuid = oneclickRunInternal(dataId, ngName);
			renderJSON(JsonResponse.successResponse(null).addResult("logUuid", uuid));
			
		} catch (Exception e) {
			renderJSON(JsonResponse.failedResponse(StringUtil.getStackTrace(e, 1000)));
		}

	}

	/**
	 * execute a command on a node group
	 * @param dataType
	 * @param nodeGroupType
	 * @param agentCommandType
	 * @param options
	 */
	public static void runCmdOnNodeGroup(String dataType, String nodeGroupType, String agentCommandType, Map<String, String> options) 
	{
		DataType dType = DataType.valueOf(dataType.toUpperCase());
		ICommandData userConfigs = UserDataProviderFactory.getCommandConfigs();
		INodeGroupData ngConfigs = UserDataProviderFactory.getNodeGroupOfType(dType);
		try {
			
			// build task
			ICommand cmd = userConfigs.getCommandByName(agentCommandType);
			String[] hosts = null;
			INodeGroup ng = null;
			if (!StringUtil.isNullOrEmpty(nodeGroupType)) {
				ng = ngConfigs.getNodeGroupByName(nodeGroupType);
				hosts = ng.getHosts();
			}
			else {
				ng = INodeGroupData.NG_EMPTY;
			}

			String varValues = DataUtil.getOptionValue(options, "var_values", "{}").trim();
			Map<String, Object> userData = (Map<String, Object>) MapListPrimitiveJsonParser.parseJson(varValues);
			HttpTaskRequest httpTask = BaseCommandData.createTaskByRequest(hosts, cmd, options, userData);

			// prepare log
			CmdLog cmdLog = prepareLog(cmd, ng, options, httpTask);
			
			// fire task
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(httpTask);
			StandaloneTaskListener listener = new StandaloneTaskListener();
			int numOfHost = hosts!=null ? hosts.length : 1;
			LogTaskEventHandler handler = new TaskResourcesProvider.LogTaskEventHandler(cmdLog, numOfHost);
			handler.saveLog(true);
			listener.setDelegateHandler(handler);
			new StandaloneTaskExecutor(httpTask.getBatchOption(), listener, reqTask).execute();
			
		} catch (Throwable t) {
			t.printStackTrace();
			error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage());
		}

	}
	
	/**
	 * prepare cmd log
	 * @param cmd
	 * @param ng
	 * @param options
	 * @param httpTask
	 * @return
	 * @throws IOException
	 */
	static private CmdLog prepareLog(
			ICommand cmd, 
			INodeGroup ng, 
			Map<String, String> options, 
			HttpTaskRequest httpTask) throws IOException 
	{
		// prepare log
		CmdLog cmdLog = new CmdLog();
		Map<String, String> optionCleanup = DataUtil.removeNullAndZero(options);
		cmdLog.setUserData(optionCleanup);
		cmdLog.setCommandKey(cmd.getName());
		cmdLog.setNodeGroup(ng);
		boolean hasRawLogs = (cmd.getCategory()!=null && cmd.getCategory().toLowerCase().startsWith("agent"));
		cmdLog.setHasRawLogs(hasRawLogs);
		cmdLog.setHttpTask(httpTask);
		cmdLog.setStatus(TaskResultEnum.Running.name());
		IJobLogger logger = UserDataProviderFactory.getJobLoggerOfType(DataType.CMDLOG);
		logger.saveLog(cmdLog);
		httpTask.getTemplateValuesForAllHosts().addToCurrentTemplate("correlationId", cmdLog.uuid());
		return cmdLog;
	}

}
