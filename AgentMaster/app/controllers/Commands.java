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
import java.util.Collections;
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

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.ICommand;
import resources.ICommandData;
import resources.INodeGroup;
import resources.INodeGroupData;
import resources.INodeGroupData.NodeGroupType;
import resources.IUserDataDao.DataType;
import resources.JobLog;
import resources.JobLog.UserCommand;
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
			for (ICommand cmd : cmds.values()) {
				Map<String, String> values = new HashMap<String, String>();
				values.put("name", cmd.getName());
				UrlTemplate req = cmd.getHttpTaskRequest().getUrlTemplate();
				values.put("url", req.getUrl());
				values.put("httpMethod", req.getMethod().name());
				StringBuffer headers = new StringBuffer();
				for (Entry<String, String> header : req.getHeaders().entrySet()) {
					headers.append(String.format("%s=%s", header.getKey(), header.getValue())).append("\n");
				}
				values.put("headers", headers.toString());
				values.put("body", req.getBody());
				values.put("variables", StringUtil.join(req.getVariableNames(), ","));
				commands.add(values);
			}
			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, commands, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	public static void commonCommands(String nodeGroup) {

		String page = "commonCommands";
		String topnav = "commands";

		render(page, topnav);
	}

	public static void getAgentCommandMetadata(String agentCommandType) {

		try {
			AgentDataProvider adp = AgentDataProvider.getInstance();
			AgentCommandMetadata agentCommandMetadata = adp.agentCommandMetadatas
					.get(agentCommandType);

			renderJSON(agentCommandMetadata);
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in getAgentCommandMetadata");
		}

	}

	public static void wizard() {

		String page = "wizard";
		String topnav = "commands";

		try {
			
			Map<String, ICommand> cmds = UserDataProvider.getCommandConfigs().getAllCommands();
			List<Map<String, String>> cmdsMeta = new ArrayList<Map<String,String>>();
			for (ICommand cmd : cmds.values()) {
				HashMap<String, String> meta = new HashMap<String, String>();
				meta.put("agentCommandType", cmd.getName());
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

	



	public static void generateUpdateSendAgentCommandToNodeGroup(
			String nodeGroupType, String agentCommandType) {

		try {

			AgentCommandProvider
					.generateUpdateSendAgentCommandToNodeGroupPredefined(
							nodeGroupType, agentCommandType);

			renderJSON(new JsonResult(
					"Successful generateUpdateSendAgentCommandToNodeGroup "
							+ DateUtils.getNowDateTimeStr()));
		} catch (Throwable t) {

			error(	"Error occured in generateUpdateSendAgentCommandToNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm()
					);
		}

	}
	
	
	public static void runCmdOnNodeGroup(String dataType, String nodeGroupType, String agentCommandType) 
	{
		DataType dType = DataType.valueOf(dataType.toUpperCase());
		ICommandData userConfigs = UserDataProvider.getCommandConfigs();
		INodeGroupData ngConfigs = UserDataProvider.getNodeGroupOfType(dType);
		try {
			ICommand cmd = userConfigs.getCommandByName(agentCommandType);
			HttpTaskRequest reqTemplate = cmd.getHttpTaskRequest();
			reqTemplate.setExecutionOption(new ExecuteOption());
			reqTemplate.setMonitorOption(new MonitorOption(5000));
			
			INodeGroup ng = ngConfigs.getNodeGroupByName(nodeGroupType);
			String[] hosts = ng.getNodeList().toArray(new String[0]);
			reqTemplate.setHosts(hosts);
			
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
			JobLog jobLog = new JobLog();
			UserCommand userCommand = new UserCommand();
			userCommand.cmd = cmd;
			userCommand.nodeGroup = ng;
			jobLog.setUserCommand(userCommand);
			
			StandaloneTaskListener listener = new StandaloneTaskListener();
			listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventHandler(jobLog));
			new StandaloneTaskExecutor(null, listener, reqTask).execute();
			
		} catch (Throwable t) {
			error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}
	
	

	/**
	 * 20131017: only adhoc node group
	 * 
	 * @param nodeGroupType
	 * @param agentCommandType
	 */
	public static void generateUpdateSendAgentCommandToAdhocNodeGroup(
			String nodeListFromText, String agentCommandType) {

		try {
			List<String> targetNodes = new ArrayList<String>();

			if (nodeListFromText != null) {

				boolean removeDuplicate = true;
				targetNodes.addAll(AgentUtils.getNodeListFromString(
						nodeListFromText, removeDuplicate));
			}

			String nodeGroupType = NodeGroupProvider
					.generateAdhocNodeGroupHelper(targetNodes);

			AgentCommandProvider
					.generateUpdateSendAgentCommandWithoutReplaceVarAdhocMap(
							nodeGroupType, agentCommandType);

			renderJSON(new JsonResult(nodeGroupType));
		} catch (Throwable t) {

			renderJSON(new JsonResult(
					"Error occured in generateUpdateSendAgentCommandToAdhocNodeGroup :"
							+ t.getLocalizedMessage()));
		}

	}

	/**
	 * 20131022: only adhoc node group with WithReplaceVarMapNodeSpecificAdhoc
	 * 
	 * @param nodeGroupType
	 * @param agentCommandType
	 */
	public static void genUpdateSendCommandWithReplaceVarMapNodeSpecificAdhocJson() {

		if (request == null || request.body == null) {
			VarUtils.printSysErrWithTimeAndOptionalReason(
					"genUpdateSendCommandWithReplaceVarMapNodeSpecificAdhocJson",
					"NULL request or request body.");

			renderJSON(new JsonResult(
					"Error occured in genUpdateSendCommandWithReplaceVarMapNodeSpecificAdhocJson"));

		}

		String supermanClientIpAddress = request.remoteAddress;

		models.utils.LogUtils.printLogNormal
				 ("supermanClientIpAddress in genUpdateSendCommandWithReplaceVarMapNodeSpecificAdhocJson() is "
						+ supermanClientIpAddress
						+ " "
						+ DateUtils.getNowDateTimeStrSdsm());

		String postData = MyHttpUtils.readHttpRequestPostData(request.body);

		if (postData != null) {

			try {
				RequestCommandWithNodeSpecficReplaceMap requestCommand = new Gson()
						.fromJson(postData,
								RequestCommandWithNodeSpecficReplaceMap.class);
				int reducedNodeCount = AgentUtils
						.removeDuplicateNodeList(requestCommand
								.getTargetNodes());

				models.utils.LogUtils.printLogNormal("reducedNodeCount for duplicated nodes "
						+ reducedNodeCount);

				AgentDataProvider adp = AgentDataProvider.getInstance();

				// this nodeGroupType has the timestamp.
				String nodeGroupType = NodeGroupProvider
						.generateAdhocNodeGroupHelper(requestCommand
								.getTargetNodes());

				// 20131026: START update to check if to add and use new adhoc
				// command?
				Boolean useNewAgentCommand = (requestCommand
						.getUseNewAgentCommand() == null) ? false
						: requestCommand.getUseNewAgentCommand();

				String agentCommandType = null;

				if (useNewAgentCommand) {
					String commandLine = requestCommand
							.getNewAgentCommandLine();
					String requestContentTemplate = requestCommand
							.getNewAgentCommandContentTemplate();
					// now to add update into memory hashmap
					agentCommandType = AgentConfigProviderHelper
							.addOrUpdateAgentCommandInMemoryFromString(
									commandLine, requestContentTemplate);
				} else {
					agentCommandType = requestCommand.getAgentCommandType();
				}

				// 20131026: END update to check if to add and use new adhoc
				// command?

				// 20131110: START update to check if need to aggregate
				// responses; if yes: check if needs to create a new reg exp.
				// Use which regular expression
				Boolean willAggregateResponse = (requestCommand
						.getWillAggregateResponse() == null) ? false
						: requestCommand.getWillAggregateResponse();

				Boolean useNewAggregation = (requestCommand
						.getUseNewAggregation() == null) ? false
						: requestCommand.getUseNewAggregation();
				String aggregationType = null;

				if (willAggregateResponse) {
					aggregationType = requestCommand.getAggregationType();
				}
				// only when need to aggregate, and also use new expression.

				if (willAggregateResponse && useNewAggregation) {

					String aggregationExpression = requestCommand
							.getNewAggregationExpression();
					/**
					 * Assumption: the aggregationExpression is encoded by URL
					 * encoder http://meyerweb.com/eric/tools/dencoder/;
					 * 
					 * Therefore; need to decode
					 * http://stackoverflow.com/questions
					 * /6138127/how-to-do-url-decoding-in-java
					 * 
					 * String result = URLDecoder.decode(url, "UTF-8");
					 * 
					 * e.g. get agent version: origin:
					 * .*"Version"[:,]\s"(.*?)".* after encoder:
					 * .*%22Version%22%5B%3A%2C%5D%5Cs%22(.*%3F)%22.*
					 * 
					 * PATTERN_AGENT_VERSION_FROM_AGENT_VI now to process and
					 * decode.
					 */

					String aggregationExpressionAfterDecode = URLDecoder
							.decode(aggregationExpression, "UTF-8");
					// now to add update into memory hashmap
					AgentConfigProviderHelper
							.addOrUpdateAggregationMetadataInMemoryFromString(
									aggregationType,
									aggregationExpressionAfterDecode);
				}

				// validate: now in memory aggregationMetadatas should have this
				// entry: aggregationType
				if (willAggregateResponse
						&& adp.aggregationMetadatas.get(aggregationType) == null) {
					String errorMsg = "ERROR. aggregationType "
							+ aggregationType
							+ " does not exist in aggregationMetadatas";

					renderJSON(new JsonResult(errorMsg));

				}

				// 20131110: END update to check if need to aggregate responses;
				// if yes: check if needs to create a new reg exp.
				// Use which regular expression

				AgentCommandProvider
						.generateUpdateSendAgentCommandWithReplaceVarMapNodeSpecificAdhoc(
								nodeGroupType, agentCommandType, requestCommand
										.getReplacementVarMapNodeSpecific());

				// 20131110: START whether or not aggregate response?
				if (!willAggregateResponse) {
					renderJSON(adp.adhocAgentData.get(nodeGroupType)
							.getNodeGroupDataMapValidForSingleCommand(
									agentCommandType));
				} else {
					// not from log; timeStamp is only used to make log file
					// names.
					String timeStamp = null;
					String rawDataSourceType = RawDataSourceType.ADHOC_AGENT_DATA
							.toString();
					String responseText = AgentCommadProviderHelperAggregation
							.genAggregationResultTextGivenAggregationType(
									nodeGroupType, agentCommandType, timeStamp,
									rawDataSourceType, aggregationType);

					renderJSON(responseText);
				}
				// 20131110: END whether or not aggregate response?

			} catch (Throwable t) {

				t.printStackTrace();
				renderJSON(new JsonResult(
						"Error occured in genUpdateSendCommandWithReplaceVarMapNodeSpecificAdhocJson. Error msg:"+ t.getLocalizedMessage()));
			}

		} else {
			VarUtils.printSysErrWithTimeAndOptionalReason(
					"genUpdateSendCommandWithReplaceVarMapNodeSpecificAdhocJson",
					"NULL: postData.");

			renderJSON(new JsonResult(
					"Error occured in upgradeAgents: NULL: postData."));
		}

	}

	/**
	 * 20131023: only adhoc node group with WithReplaceVarMap All nodes with the
	 * same replacement
	 * 
	 * 20131110: add reg expression.
	 * 
	 * @param nodeGroupType
	 * @param agentCommandType
	 */
	public static void genUpdateSendCommandWithReplaceVarMapAdhocJson() {

		if (request == null || request.body == null) {
			VarUtils.printSysErrWithTimeAndOptionalReason(
					"genUpdateSendCommandWithReplaceVarMapAdhocJson",
					"NULL request or request body.");

			renderJSON(new JsonResult(
					"Error occured in genUpdateSendCommandWithReplaceVarMapAdhocJson"));

		}

		String supermanClientIpAddress = request.remoteAddress;

		models.utils.LogUtils.printLogNormal
				 ("supermanClientIpAddress in genUpdateSendCommandWithReplaceVarMapAdhocJson() is "
						+ supermanClientIpAddress
						+ " "
						+ DateUtils.getNowDateTimeStrSdsm());

		String postData = MyHttpUtils.readHttpRequestPostData(request.body);

		if (postData != null) {

			try {
				RequestCommandWithReplaceMap requestCommand = new Gson()
						.fromJson(postData, RequestCommandWithReplaceMap.class);
				int reducedNodeCount = AgentUtils
						.removeDuplicateNodeList(requestCommand
								.getTargetNodes());

				models.utils.LogUtils.printLogNormal("reducedNodeCount for duplicated nodes "
						+ reducedNodeCount);

				AgentDataProvider adp = AgentDataProvider.getInstance();

				// this nodeGroupType has the timestamp.
				String nodeGroupType = NodeGroupProvider
						.generateAdhocNodeGroupHelper(requestCommand
								.getTargetNodes());

				// 20131026: START update to check if to add and use new adhoc
				// command?
				Boolean useNewAgentCommand = (requestCommand
						.getUseNewAgentCommand() == null) ? false
						: requestCommand.getUseNewAgentCommand();

				String agentCommandType = null;

				if (useNewAgentCommand) {
					String commandLine = requestCommand
							.getNewAgentCommandLine();
					String requestContentTemplate = requestCommand
							.getNewAgentCommandContentTemplate();
					// now to add update into memory hashmap
					agentCommandType = AgentConfigProviderHelper
							.addOrUpdateAgentCommandInMemoryFromString(
									commandLine, requestContentTemplate);
				} else {
					agentCommandType = requestCommand.getAgentCommandType();
				}

				// 20131026: END update to check if to add and use new adhoc
				// command?

				// 20131110: START update to check if need to aggregate
				// responses; if yes: check if needs to create a new reg exp.
				// Use which regular expression
				Boolean willAggregateResponse = (requestCommand
						.getWillAggregateResponse() == null) ? false
						: requestCommand.getWillAggregateResponse();

				Boolean useNewAggregation = (requestCommand
						.getUseNewAggregation() == null) ? false
						: requestCommand.getUseNewAggregation();
				String aggregationType = null;

				if (willAggregateResponse) {
					aggregationType = requestCommand.getAggregationType();
				}
				// only when need to aggregate, and also use new expression.

				if (willAggregateResponse && useNewAggregation) {

					String aggregationExpression = requestCommand
							.getNewAggregationExpression();
					/**
					 * Assumption: the aggregationExpression is encoded by URL
					 * encoder http://meyerweb.com/eric/tools/dencoder/;
					 * 
					 * Therefore; need to decode
					 * http://stackoverflow.com/questions
					 * /6138127/how-to-do-url-decoding-in-java
					 * 
					 * String result = URLDecoder.decode(url, "UTF-8");
					 * 
					 * e.g. get agent version: origin:
					 * .*"Version"[:,]\s"(.*?)".* after encoder:
					 * .*%22Version%22%5B%3A%2C%5D%5Cs%22(.*%3F)%22.*
					 * 
					 * PATTERN_AGENT_VERSION_FROM_AGENT_VI now to process and
					 * decode.
					 */

					String aggregationExpressionAfterDecode = URLDecoder
							.decode(aggregationExpression, "UTF-8");
					// now to add update into memory hashmap
					AgentConfigProviderHelper
							.addOrUpdateAggregationMetadataInMemoryFromString(
									aggregationType,
									aggregationExpressionAfterDecode);
				}

				// validate: now in memory aggregationMetadatas should have this
				// entry: aggregationType
				if (willAggregateResponse
						&& adp.aggregationMetadatas.get(aggregationType) == null) {
					String errorMsg = "ERROR. aggregationType "
							+ aggregationType
							+ " does not exist in aggregationMetadatas";

					renderJSON(new JsonResult(errorMsg));

				}

				// 20131110: END update to check if need to aggregate responses;
				// if yes: check if needs to create a new reg exp.
				// Use which regular expression

				AgentCommandProvider
						.generateUpdateSendAgentCommandWithReplaceVarAdhocMap(
								nodeGroupType, agentCommandType,
								requestCommand.getReplacementVarMap());

				// 20131110: START whether or not aggregate response?
				if (!willAggregateResponse) {
					renderJSON(adp.adhocAgentData.get(nodeGroupType)
							.getNodeGroupDataMapValidForSingleCommand(
									agentCommandType));
				} else {
					// not from log; timeStamp is only used to make log file
					// names.
					String timeStamp = null;
					String rawDataSourceType = RawDataSourceType.ADHOC_AGENT_DATA
							.toString();
					String responseText = AgentCommadProviderHelperAggregation
							.genAggregationResultTextGivenAggregationType(
									nodeGroupType, agentCommandType, timeStamp,
									rawDataSourceType, aggregationType);

					renderJSON(responseText);
				}

				// 20131110: END whether or not aggregate response?

			} catch (Throwable t) {

				t.printStackTrace();
				renderJSON(new JsonResult(
						"Error occured in genUpdateSendCommandWithReplaceVarMapAdhocJson() with reason: "
								+ t.getLocalizedMessage()));
			}

		} else {
			VarUtils.printSysErrWithTimeAndOptionalReason(
					"genUpdateSendCommandWithReplaceVarMapAdhocJson",
					"NULL: postData.");

			renderJSON(new JsonResult(
					"Error occured in upgradeAgents: NULL: postData."));
		}

	}



	/**
	 * NONE ADHOC DATA ONLY
	 * 
	 * @param nodeGroupType
	 * @param agentCommandType
	 */
	public static void updateRequestContentGeneric(String nodeGroupType,
			String agentCommandType) {

		try {

			AgentCommandProviderHelperInternalFlow.updateRequestContentGeneric(
					nodeGroupType, agentCommandType,
					AgentDataProvider.allAgentData,
					AgentDataProvider.nodeGroupSourceMetadatas);

			renderJSON(new JsonResult("Successful updateRequestContentGeneric "
					+ DateUtils.getNowDateTimeStr()));
		} catch (Throwable t) {

			renderJSON(new JsonResult(
					"Error occured in updateRequestContentGeneric"));
		}

	}
	

	
	public static void commandToSingleTargetServer(String nodeListFromText,
			String agentCommandType, String varName,
			 String targetServerNew) {


		String nodeGroupType = null;
		try {

			if (nodeListFromText == null || nodeListFromText.isEmpty()
					|| agentCommandType == null
					|| agentCommandType.isEmpty()
					
					|| varName == null
					|| varName.isEmpty()
					
					|| targetServerNew == null
					|| targetServerNew.isEmpty()
			) {
				models.utils.LogUtils
						.printLogError("nodeListFromText or agentCommandType or varName or targetServerNew is NULL or empty; now exit in func execScriptViaAgentWorkFlow() !!"
								+ DateUtils.getNowDateTimeStrSdsm());

				error(("Error occured in commandToSingleTargetServer is null or empty "));
			}
			
			
			// trim white spaces
			agentCommandType = agentCommandType.trim();
			varName = varName.trim();
			targetServerNew = targetServerNew.trim();

			List<String> targetNodes = new ArrayList<String>();
			if (nodeListFromText != null) {

				boolean removeDuplicate = true;
				targetNodes.addAll(AgentUtils.getNodeListFromString(
						nodeListFromText, removeDuplicate));
			} else {
				models.utils.LogUtils
						.printLogError("User input an empty nodeListFromText"
								+ DateUtils.getNowDateTimeStrSdsm());
			}
			nodeGroupType = CommandProviderSingleServerHelper
					.commandToSingleTargetServer(targetNodes,
							agentCommandType, varName, targetServerNew);

			renderJSON(new JsonResult(nodeGroupType));
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON(new JsonResult(
					"Error occured in commandToSingleTargetServer"));
		}

	}// end func

}
