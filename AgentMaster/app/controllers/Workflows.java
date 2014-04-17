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


import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.FlowEvent;
import org.lightj.session.FlowSession;
import org.lightj.session.FlowSessionFactory;
import org.lightj.session.IFlowEventListener;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepTransition;
import org.lightj.task.BatchOption;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;
import resources.command.ICommand;
import resources.log.BaseLog.UserWorkflow;
import resources.log.FlowLog;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.utils.DataUtil;
import resources.utils.DateUtils;
import resources.workflow.IWorkflowMeta;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 
 * @author binyu
 *
 */
public class Workflows extends Controller {

	// command wizard
	public static void index() {

		String page = "index";
		String topnav = "workflows";

		try {
			Map<String, IWorkflowMeta> workflows = UserDataProvider.getWorkflowConfigs().getAllFlows();
			List<Map<String, String>> results = new ArrayList<Map<String,String>>();
			for (Entry<String, IWorkflowMeta> entry : workflows.entrySet()) {
				Map<String, String> values = new HashMap<String, String>();
				IWorkflowMeta workflow = entry.getValue();
				values.put("name", entry.getKey());
				values.put("type", workflow.getFlowType());
				values.put("klazz", workflow.getFlowClass());
				values.put("description", workflow.getFlowDescription());
				StringBuffer userData = new StringBuffer();
				if (workflow.getUserData() != null) {
					for (Entry<String, String> param : workflow.getUserData().entrySet()) {
						userData.append(String.format("%s=%s", param.getKey(), param.getValue())).append(",");
					}
				}
				values.put("userData", userData.toString());
				results.add(values);
			}
			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, results, lastRefreshed);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}

	/**
	 * command wizard
	 */
	public static void wizard(String workflowId) {

		String page = "wizard";
		String topnav = "workflows";

		try {
			
			IWorkflowMeta workflow = UserDataProvider.getWorkflowConfigs().getFlowByName(workflowId);
			
			Map<String, INodeGroup> ngsMap = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getAllNodeGroups();
			ArrayList<Map<String, String>> ngs = new ArrayList<Map<String, String>>();
			for (String v : ngsMap.keySet()) {
				Map<String, String> kvp = new HashMap<String, String>(1);
				kvp.put("nodeGroupType", v);
				ngs.add(kvp);
			}
			String nodeGroupMetas = JsonUtil.encode(ngs);
			
			String wfName = workflow.getFlowName();
			
			render(page, topnav, nodeGroupMetas, wfName);
			
		} catch (Exception t) {

			t.printStackTrace();
			error(t);
		}

	}
	
	/**
	 * options for a command
	 * @param wfName
	 */
	public static void getOptions(String wfName) {
		
		try {
			IWorkflowMeta wf = UserDataProvider.getWorkflowConfigs().getFlowByName(wfName);
			ArrayList<Map<String, String>> result = new ArrayList<Map<String,String>>();
			
			if (wf.getUserData() != null) {
				HashMap<String, String> userData = new HashMap<String, String>(wf.getUserData());
				userData.remove("batchOption");
				userData.remove("hosts");
				result.add(DataUtil.createResultItem("var_values", JsonUtil.encodePretty(userData)));
			}
			renderJSON(result);
			
		} catch (Throwable t) {

			t.printStackTrace();
			renderJSON(DataUtil.jsonResult("Error occured in wizard"));
		}
		
	}

	/**
	 * run workflow on node group
	 * @param dataType
	 * @param nodeGroupType
	 * @param workflowType
	 * @param options
	 */
	public static void runWfOnNodeGroup(String dataType, String nodeGroupType, String workflowType, Map<String, String> options) 
	{
		DataType dType = DataType.valueOf(dataType.toUpperCase());
		INodeGroupData ngConfigs = UserDataProvider.getNodeGroupOfType(dType);
		try {
			// create flow from user request
			IWorkflowMeta workflow = UserDataProvider.getWorkflowConfigs().getFlowByName(workflowType);
			INodeGroup ng = ngConfigs.getNodeGroupByName(nodeGroupType);
			FlowSession flow = createFlowByRequest(ng, workflow, options); 
			
			// create the log
			FlowLog flowLog = new FlowLog();
			Map<String, String> optionCleanup = DataUtil.removeNullAndZero(options);
			flowLog.setUserData(optionCleanup);
			UserWorkflow userWorkflow = new UserWorkflow();
			userWorkflow.workflow = workflow;
			flowLog.setNodeGroup(ng);
			flowLog.setUserWorkflow(userWorkflow);

			// save and run flow
			flow.addEventListener(new FlowEventListener(flowLog));
			flow.save();
			flow.runFlow();
			
		} catch (Throwable t) {
			t.printStackTrace();
			error(	"Error occured in runWfOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}
	
	/**
	 * create workflow session from request (user or internal)
	 * @param ng
	 * @param workflow
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public static FlowSession createFlowByRequest(INodeGroup ng, IWorkflowMeta workflow, Map<String, String> options) throws IOException 
	{
		HashMap<String, String> varValues = JsonUtil.decode(
				DataUtil.getOptionValue(options, "var_values", "{}"), 
				new TypeReference<HashMap<String, String>>(){});
		HashMap<String, Object> values = new HashMap<String, Object>();
		for (Entry<String, String> entry : varValues.entrySet()) {
			String svalue = entry.getValue();
			Object value = null;
			if (svalue.startsWith("[") && svalue.endsWith("]")) {
				// string array
				value = JsonUtil.decode(svalue, String[].class);
			}
			else if (svalue.startsWith("{") && svalue.endsWith("}")) {
				// string map 
				value = JsonUtil.decode(svalue, new TypeReference<HashMap<String, String>>(){});
			}
			else {
				value = svalue;
			}
			values.put(entry.getKey().toString(), value);
		}
		
		Strategy strategy = Strategy.valueOf(DataUtil.getOptionValue(options, "thrStrategy", "UNLIMITED"));
		int maxRate = Integer.parseInt(DataUtil.getOptionValue(options, "thr_rate", "1000"));
		values.put("batchOption", new BatchOption(maxRate, strategy));
		
		String[] hosts = ng.getNodeList().toArray(new String[0]);
		values.put("hosts", hosts);
		
		FlowSession flow = FlowSessionFactory.getInstance().createSession(workflow.getFlowName());
		flow.getSessionContext().addUserData(values);
		return flow;
	}
	
	
	/**
	 * log flow execution log at flow stop
	 * @author biyu
	 *
	 */
	public static class FlowEventListener implements IFlowEventListener {
		
		FlowLog flowLog;
		public FlowEventListener(FlowLog flowLog) {
			this.flowLog = flowLog;
		}

		@Override
		public void handleStepEvent(FlowEvent event, FlowSession session,
				IFlowStep flowStep, StepTransition stepTransition) {
		}

		@Override
		public void handleFlowEvent(FlowEvent event, FlowSession session,
				String msg) {
			if (event == FlowEvent.stop) {
				flowLog.getUserWorkflow().jobInfo = session.getFlowInfo();
				try {
					UserDataProvider.getJobLoggerOfType(DataType.FLOWLOG).saveLog(flowLog);
				} catch (IOException e) {
					play.Logger.error(e, "fail to save log");
				}
			}
		}

		@Override
		public void handleError(Throwable t, FlowSession session) {
		}
		
	}


}
