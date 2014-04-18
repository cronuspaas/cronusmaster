package resources.job;

import java.util.HashMap;

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecutableTask;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.log.JobLog;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.utils.DataUtil;
import resources.utils.DateUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import controllers.Commands;

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

			HashMap<String, String> realUserData = JsonUtil.decode(
					DataUtil.getOptionValue(userData, "var_values", "{}"), 
					new TypeReference<HashMap<String, String>>(){});
			
			HttpTaskRequest reqTemplate = Commands.createTaskByRequest(hosts, cmd, userData, realUserData); 

			// builg log 
			JobLog jobLog = new JobLog();
			jobLog.setUserData(DataUtil.removeNullAndZero(userData));
			jobLog.setCommandKey(cmd.getName());
			jobLog.setNodeGroup(ng);
			jobLog.setJobId(getName());
			
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
