package com.stackscaling.agentmaster.resources.job;

import java.util.HashMap;

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecutableTask;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider.LogTaskEventHandler;
import com.stackscaling.agentmaster.resources.UserDataProvider;
import com.stackscaling.agentmaster.resources.command.CommandDataImpl;
import com.stackscaling.agentmaster.resources.command.ICommand;
import com.stackscaling.agentmaster.resources.command.ICommandData;
import com.stackscaling.agentmaster.resources.log.JobLog;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroup;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroupData;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * run command on a node group with an interval
 *
 * @author binyu
 *
 */
public class CmdIntervalJobImpl extends BaseIntervalJob {

	static Logger logger = LoggerFactory.getLogger(CmdIntervalJobImpl.class);

	@Override
	public void runJobAsync()
	{
		INodeGroupData ngConfigs = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP);
		ICommandData cmdData = UserDataProvider.getCommandConfigs();
		try {
			// create task
			ICommand cmd = cmdData.getCommandByName(cmdName);
			INodeGroup ng = ngConfigs.getNodeGroupByName(nodeGroupName);
			String[] hosts = ng.getHosts();

			HashMap<String, String> realUserData = JsonUtil.decode(
					DataUtil.getOptionValue(userData, "var_values", "{}"),
					new TypeReference<HashMap<String, String>>(){});

			HttpTaskRequest reqTemplate = CommandDataImpl.createTaskByRequest(hosts, cmd, userData, realUserData);

			// builg log
			int numOfHost = hosts!=null ? hosts.length : 1;
			JobLog jobLog = new JobLog();
			jobLog.setUserData(DataUtil.removeNullAndZero(userData));
			jobLog.setCommandKey(cmd.getName());
			jobLog.setNodeGroup(ng);
			jobLog.setHasRawLogs(cmd.isHasRawLogs());
			jobLog.setJobId(getName());
			jobLog.setStatusDetail(0, 0, numOfHost);
			reqTemplate.getTemplateValuesForAllHosts().addToCurrentTemplate("correlationId", jobLog.uuid());

			// fire
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
			StandaloneTaskListener listener = new StandaloneTaskListener();
			LogTaskEventHandler handler = new TaskResourcesProvider.LogTaskEventHandler(jobLog, numOfHost);
			handler.saveLog(true);
			listener.setDelegateHandler(handler);
			new StandaloneTaskExecutor(reqTemplate.getBatchOption(), listener, reqTask).execute();

		} catch (Throwable t) {
			logger.error(	"Error occured in runJobAsync: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}

	@Override
	public String getDescription() {
		return String.format("Run %s on %s every %s minute", cmdName, nodeGroupName, intervalInMinute);
	}

}
