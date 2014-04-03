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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.data.AgentCommandMetadata;
import models.data.JsonResult;
import models.data.RawDataSourceType;
import models.data.providers.AgentCommadProviderHelperAggregation;
import models.data.providers.AgentCommandProvider;
import models.data.providers.AgentCommandProviderHelperInternalFlow;
import models.data.providers.AgentConfigProviderHelper;
import models.data.providers.AgentDataProvider;
import models.data.providers.CommandProviderSingleServerHelper;
import models.data.providers.NodeGroupProvider;
import models.rest.beans.requests.RequestCommandWithNodeSpecficReplaceMap;
import models.rest.beans.requests.RequestCommandWithReplaceMap;
import models.utils.AgentUtils;
import models.utils.DateUtils;
import models.utils.MyHttpUtils;
import models.utils.VarUtils;

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
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.log.JobLog;
import resources.log.JobLog.UserCommand;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;

import com.google.gson.Gson;

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
				values.put("variables", StringUtil.join(req.getVariableNames(), ","));
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
	public static void wizard() {

		String page = "wizard";
		String topnav = "commands";

		try {
			
			Map<String, ICommand> cmds = UserDataProvider.getCommandConfigs().getAllCommands();
			List<Map<String, String>> cmdsMeta = new ArrayList<Map<String,String>>();
			for (String cmdName : cmds.keySet()) {
				HashMap<String, String> meta = new HashMap<String, String>();
				meta.put("agentCommandType", cmdName);
				cmdsMeta.add(meta);
			}
			
			Map<String, INodeGroup> ngsMap = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getAllNodeGroups();
			ArrayList<Map<String, String>> ngs = new ArrayList<Map<String, String>>();
			for (String v : ngsMap.keySet()) {
				Map<String, String> kvp = new HashMap<String, String>(1);
				kvp.put("nodeGroupType", v);
				ngs.add(kvp);
			}
			String nodeGroupSourceMetadataListJsonArray = JsonUtil.encode(ngs);
			
			String agentCommandMetadataListJsonArray = JsonUtil.encode(cmdsMeta);

			render(page, topnav, nodeGroupSourceMetadataListJsonArray, agentCommandMetadataListJsonArray);
		} catch (Throwable t) {

			t.printStackTrace();
			renderJSON(new JsonResult("Error occured in wizard"));
		}

	}

	private static String getOptionValue(Map<String, String> options, String key, String defVal) {
		return (options.containsKey(key) && !StringUtil.isNullOrEmpty(options.get(key))) ? options.get(key) : defVal;
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
			ICommand cmd = userConfigs.getCommandByName(agentCommandType);
			HttpTaskRequest reqTemplate = cmd.createCopy();

			long exeInitDelayMs = Long.parseLong(getOptionValue(options, "exe_initde", "0")) * 1000L;
			long exeTimoutMs = Long.parseLong(getOptionValue(options, "exe_initde", "0")) * 1000L;
			int exeRetry = Integer.parseInt(getOptionValue(options, "exe_rede", "0"));
			long retryDelayMs = Long.parseLong(getOptionValue(options, "exe_rede", "0")) * 1000L;
			ExecuteOption exeOption = new ExecuteOption(exeInitDelayMs, exeTimoutMs, exeRetry, retryDelayMs);
			
			long monIntervalMs = Integer.parseInt(getOptionValue(options, "mon_int", "1")) * 1000L;
			long monInitDelayMs = Long.parseLong(getOptionValue(options, "mon_initde", "0")) * 1000L;
			long monTimoutMs = Long.parseLong(getOptionValue(options, "mon_initde", "0")) * 1000L;
			int monRetry = Integer.parseInt(getOptionValue(options, "mon_rede", "0"));
			long monRetryDelayMs = Long.parseLong(getOptionValue(options, "mon_rede", "0")) * 1000L;
			MonitorOption monOption = new MonitorOption(monInitDelayMs, monIntervalMs, monTimoutMs, monRetry, monRetryDelayMs);
					
			Strategy strategy = Strategy.valueOf(getOptionValue(options, "thrStrategy", "UNLIMITED"));
			int maxRate = Integer.parseInt(getOptionValue(options, "thr_rate", "1000"));
			BatchOption batchOption = new BatchOption(maxRate, strategy);
			
			HashMap<Object, Object> varValues = JsonUtil.decode(getOptionValue(options, "var_values", "{}"), HashMap.class);
			HashMap<String, String> values = new HashMap<String, String>();
			for (Entry<Object, Object> entry : varValues.entrySet()) {
				values.put(entry.getKey().toString(), entry.getValue().toString());
			}
			
			reqTemplate.setExecutionOption(exeOption);
			reqTemplate.setMonitorOption(monOption);
			
			INodeGroup ng = ngConfigs.getNodeGroupByName(nodeGroupType);
			String[] hosts = ng.getNodeList().toArray(new String[0]);
			reqTemplate.setHosts(hosts);
			reqTemplate.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(values));
			
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
			JobLog jobLog = new JobLog();
			UserCommand userCommand = new UserCommand();
			userCommand.cmd = cmd;
			userCommand.nodeGroup = ng;
			jobLog.setUserCommand(userCommand);
			
			StandaloneTaskListener listener = new StandaloneTaskListener();
			listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventHandler(DataType.CMDLOG, jobLog));
			new StandaloneTaskExecutor(batchOption, listener, reqTask).execute();
			
		} catch (Throwable t) {
			t.printStackTrace();
			error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}
	
}
