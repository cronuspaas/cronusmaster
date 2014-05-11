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
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.log.CmdLog;
import resources.nodegroup.AdhocNodeGroupDataImpl;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.utils.DataUtil;
import resources.utils.DateUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 
 * @author ypei
 *
 */
public class Commands extends Controller {

	// command wizard
	public static void index() {

		String page = "index";
		String topnav = "commands";

		try {
			Map<String, ICommand> cmds = UserDataProvider.getCommandConfigs().getAllCommands();
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
				values.put("body", req.getBody());
				values.put("variables", StringUtil.join(req.getVariableNames(), ", "));
				values.put("userData", JsonUtil.encode(cmd.getUserData()));
				StringBuffer parameters = new StringBuffer();
				if (req.getParameters() != null) {
					for (Entry<String, String> param : req.getParameters().entrySet()) {
						parameters.append(String.format("%s=%s", param.getKey(), param.getValue())).append(",");
					}
				}
				values.put("parameters", parameters.toString());
				commands.add(values);
			}
			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, commands, lastRefreshed);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}

	/**
	 * command wizard
	 */
	public static void wizard(String cmdName) {

		String page = "wizard";
		String topnav = "commands";

		try {
			Map<String, INodeGroup> ngsMap = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getAllNodeGroups();
			ArrayList<Map<String, String>> ngs = new ArrayList<Map<String, String>>();
			for (String v : ngsMap.keySet()) {
				Map<String, String> kvp = new HashMap<String, String>(1);
				kvp.put("nodeGroupType", v);
				ngs.add(kvp);
			}
			String nodeGroupSourceMetadataListJsonArray = JsonUtil.encode(ngs);
			
			render(page, topnav, nodeGroupSourceMetadataListJsonArray, cmdName);
		} catch (Throwable t) {

			t.printStackTrace();
			renderJSON(DataUtil.jsonResult("Error occured in wizard"));
		}

	}

	/**
	 * options for a command
	 * @param cmdName
	 */
	public static void getOptions(String cmdName) {
		
		try {
			ICommand cmd = UserDataProvider.getCommandConfigs().getCommandByName(cmdName);
			ArrayList<Map<String, String>> result = new ArrayList<Map<String,String>>();
			
			HttpTaskRequest req = cmd.createCopy();
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
			if (cmd.getUserData() != null) {
				result.add(createResultItem("var_values", JsonUtil.encodePretty(cmd.getUserData())));
			}
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
	 * execute a command on a node group
	 * @param dataType
	 * @param nodeGroupType
	 * @param agentCommandType
	 * @param options
	 */
	public static void runCmdOnNodeGroup(String dataType, String nodeGroupType, String agentCommandType, Map<String, String> options) 
	{
		DataType dType = DataType.valueOf(dataType.toUpperCase());
		ICommandData userConfigs = UserDataProvider.getCommandConfigs();
		INodeGroupData ngConfigs = UserDataProvider.getNodeGroupOfType(dType);
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

			Map<String, String> userData = JsonUtil.decode(
					DataUtil.getOptionValue(options, "var_values", "{}"), 
					new TypeReference<HashMap<String, String>>(){});

			HttpTaskRequest reqTemplate = createTaskByRequest(hosts, cmd, options, userData);
			
			// prepare log
			CmdLog jobLog = new CmdLog();
			Map<String, String> optionCleanup = DataUtil.removeNullAndZero(options);
			jobLog.setUserData(optionCleanup);
			jobLog.setCommandKey(cmd.getName());
			jobLog.setNodeGroup(ng);
			
			// fire task
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
			StandaloneTaskListener listener = new StandaloneTaskListener();
			listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventHandler(DataType.CMDLOG, jobLog));
			new StandaloneTaskExecutor(reqTemplate.getBatchOption(), listener, reqTask).execute();
			
		} catch (Throwable t) {
			t.printStackTrace();
			error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}
	
	/**
	 * build http request from user request
	 * @param ng
	 * @param cmd
	 * @param options
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static HttpTaskRequest createTaskByRequest(
			String[] hosts, 
			ICommand cmd, 
			Map<String, String> options, 
			Map<String, String> userData) throws IOException 
	{
		HttpTaskRequest reqTemplate = cmd.createCopy();

		long exeInitDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "exe_initde", "0"));
		long exeTimoutSec = Long.parseLong(DataUtil.getOptionValue(options, "exe_to", "0"));
		int exeRetry = Integer.parseInt(DataUtil.getOptionValue(options, "exe_retry", "0"));
		long retryDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "exe_rede", "0"));
		ExecuteOption exeOption = new ExecuteOption(exeInitDelaySec, exeTimoutSec, exeRetry, retryDelaySec);
		reqTemplate.setExecutionOption(exeOption);
		
		if (StringUtil.equalIgnoreCase(HttpTaskRequest.TaskType.asyncpoll.name(), reqTemplate.getTaskType())) {
			long monIntervalSec = Integer.parseInt(DataUtil.getOptionValue(options, "mon_int", "1"));
			long monInitDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "mon_initde", "0"));
			long monTimoutSec = Long.parseLong(DataUtil.getOptionValue(options, "mon_to", "0"));
			int monRetry = Integer.parseInt(DataUtil.getOptionValue(options, "mon_retry", "0"));
			long monRetryDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "mon_rede", "0"));
			MonitorOption monOption = new MonitorOption(monInitDelaySec, monIntervalSec, monTimoutSec, monRetry, monRetryDelaySec);
			reqTemplate.setMonitorOption(monOption);
		}
		else {
			for (String option : new String[] {"mon_int", "mon_initde", "mon_to", "mon_retry", "mon_rede"}) {
				options.remove(option);
			}
		}
				
		Strategy strategy = Strategy.valueOf(DataUtil.getOptionValue(options, "thrStrategy", "UNLIMITED"));
		int maxRate = Integer.parseInt(DataUtil.getOptionValue(options, "thr_rate", "1000"));
		BatchOption batchOption = new BatchOption(maxRate, strategy);
		reqTemplate.setBatchOption(batchOption);
		
		HashMap<String, String> values = new HashMap<String, String>();
		for (Entry<String, String> entry : userData.entrySet()) {
			values.put(entry.getKey(), entry.getValue());
		}
		
		if (hosts != null) {
			reqTemplate.setHosts(hosts);
		}
		reqTemplate.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(values));
		return reqTemplate;
	}

}
