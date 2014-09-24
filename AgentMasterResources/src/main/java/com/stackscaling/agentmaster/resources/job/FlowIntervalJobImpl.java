package com.stackscaling.agentmaster.resources.job;

import org.lightj.session.FlowSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider.LogFlowEventListener;
import com.stackscaling.agentmaster.resources.UserDataProvider;
import com.stackscaling.agentmaster.resources.log.BaseLog.UserWorkflow;
import com.stackscaling.agentmaster.resources.log.FlowLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroup;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroupData;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
import com.stackscaling.agentmaster.resources.workflow.IWorkflowMeta;
import com.stackscaling.agentmaster.resources.workflow.WorkflowDataImpl;

/**
 * run command on a node group with an interval
 *
 * @author binyu
 *
 */
public class FlowIntervalJobImpl extends BaseIntervalJob {

	static Logger logger = LoggerFactory.getLogger(FlowIntervalJobImpl.class);

	@Override
	public void runJobAsync()
	{
		INodeGroupData ngConfigs = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP);
		try {
			IWorkflowMeta workflow = UserDataProvider.getWorkflowConfigs().getFlowByName(cmdName);
			INodeGroup ng = ngConfigs.getNodeGroupByName(nodeGroupName);

			FlowSession flow = WorkflowDataImpl.createFlowByRequest(ng, workflow, userData);

			// create the log
			FlowLog flowLog = new FlowLog();
			flowLog.setUserData(DataUtil.removeNullAndZero(userData));
			UserWorkflow userWorkflow = new UserWorkflow();
			flowLog.setCommandKey(workflow.getFlowName());
			flowLog.setNodeGroup(ng);
			flowLog.setUserWorkflow(userWorkflow);
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.FLOWLOG);
			logger.saveLog(flowLog);

			// save and run flow
			flow.addEventListener(new LogFlowEventListener(flowLog));
			flow.save();
			flow.runFlow();

		} catch (Throwable t) {
			logger.error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage());
		}

	}

	@Override
	public String getDescription() {
		return String.format("Run %s on %s every %s minute", cmdName, nodeGroupName, intervalInMinute);
	}

}
