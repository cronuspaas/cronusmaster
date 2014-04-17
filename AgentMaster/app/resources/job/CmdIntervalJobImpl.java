package resources.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
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

import controllers.Commands;

import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.log.BaseLog;
import resources.log.BaseLog.UserCommand;
import resources.log.JobLog;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.utils.DataUtil;
import resources.utils.DateUtils;

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
			String[] hosts = ng.getNodeList().toArray(new String[0]);
			HttpTaskRequest reqTemplate = Commands.createTaskByRequest(hosts, cmd, userData); 

			// builg log 
			JobLog jobLog = new JobLog();
			jobLog.setUserData(DataUtil.removeNullAndZero(userData));
			UserCommand userCommand = new UserCommand();
			userCommand.cmd = cmd;
			jobLog.setNodeGroup(ng);
			userCommand.jobId = getName();
			jobLog.setUserCommand(userCommand);
			
			// fire
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
			StandaloneTaskListener listener = new StandaloneTaskListener();
			listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventHandler(DataType.JOBLOG, jobLog));
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
