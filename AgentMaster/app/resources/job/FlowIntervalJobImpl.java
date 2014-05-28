package resources.job;

import org.lightj.session.FlowSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider.LogFlowEventListener;
import resources.UserDataProvider;
import resources.log.BaseLog.UserWorkflow;
import resources.log.FlowLog;
import resources.log.IJobLogger;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.utils.DataUtil;
import resources.utils.DateUtils;
import resources.workflow.IWorkflowMeta;
import controllers.Workflows;

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
			logger.error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}

	@Override
	public String getDescription() {
		return String.format("Run %s on %s every %s minute", cmdName, nodeGroupName, intervalInMinute);
	}

}
