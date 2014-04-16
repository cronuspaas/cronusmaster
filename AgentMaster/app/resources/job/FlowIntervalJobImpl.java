package resources.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.utils.DateUtils;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.FlowSession;
import org.lightj.task.BatchOption;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import controllers.Workflows;
import controllers.Workflows.FlowEventListener;

import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.log.BaseLog;
import resources.log.FlowLog;
import resources.log.BaseLog.UserCommand;
import resources.log.BaseLog.UserWorkflow;
import resources.log.JobLog;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.workflow.IWorkflowMeta;

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

			FlowSession flow = Workflows.createFlowByRequest(ng, workflow, userData); 

			// create the log
			FlowLog flowLog = new FlowLog();
			flowLog.setUserData(userData);
			UserWorkflow userWorkflow = new UserWorkflow();
			userWorkflow.workflow = workflow;
			flowLog.setNodeGroup(ng);
			flowLog.setUserWorkflow(userWorkflow);

			// save and run flow
			flow.addEventListener(new FlowEventListener(flowLog));
			flow.save();
			flow.runFlow();
			
		} catch (Throwable t) {
			logger.error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}

	@Override
	public String getDescription() {
		return String.format("Run %s on %s every %s minute", cmdName, nodeGroupName, intervalInMinute);
	}

}
