package resources.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.utils.DateUtils;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.BatchOption;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.log.JobLog;
import resources.log.JobLog.UserCommand;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;

/**
 * run command on a node group with an interval
 * 
 * @author binyu
 *
 */
public class CmdIntervalJobImpl extends BaseIntervalJob {
	
	static Logger logger = LoggerFactory.getLogger(CmdIntervalJobImpl.class);

	/**
	 * command to be run
	 */
	private String cmdName;
	
	/**
	 * node group to run on
	 */
	private String nodeGroupName;
	
	/**
	 * user customization
	 */
	private BatchOption batchOption;
	private ExecuteOption executeOption;
	private MonitorOption monitorOption;
	private List<Map<String, String>> templateValues;
	
	public String getCmdName() {
		return cmdName;
	}

	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}

	public String getNodeGroupName() {
		return nodeGroupName;
	}

	public void setNodeGroupName(String nodeGroupName) {
		this.nodeGroupName = nodeGroupName;
	}

	public BatchOption getBatchOption() {
		return batchOption;
	}

	public void setBatchOption(BatchOption batchOption) {
		this.batchOption = batchOption;
	}

	public ExecuteOption getExecuteOption() {
		return executeOption;
	}

	public void setExecuteOption(ExecuteOption executeOption) {
		this.executeOption = executeOption;
	}

	public MonitorOption getMonitorOption() {
		return monitorOption;
	}

	public void setMonitorOption(MonitorOption monitorOption) {
		this.monitorOption = monitorOption;
	}

	public List<Map<String, String>> getTemplateValues() {
		return templateValues;
	}

	public void setTemplateValues(List<Map<String, String>> templateValues) {
		this.templateValues = templateValues;
	}
	
	public void addTemplateValue(Map<String, String> templateValues) {
		if (this.templateValues == null) {
			this.templateValues = new ArrayList<Map<String,String>>();
		}
		this.templateValues.add(templateValues);
	}

	@Override
	public void runJobAsync() 
	{
		INodeGroupData ngConfigs = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP);
		ICommandData cmdData = UserDataProvider.getCommandConfigs();
		try {
			ICommand cmd = cmdData.getCommandByName(cmdName);
			HttpTaskRequest reqTemplate = cmd.createCopy();
			reqTemplate.setExecutionOption(executeOption);
			reqTemplate.setMonitorOption(monitorOption);
			reqTemplate.setTemplateValuesForAllHosts(new HostTemplateValues().addAllTemplateValues(templateValues));

			INodeGroup ng = ngConfigs.getNodeGroupByName(nodeGroupName);
			String[] hosts = ng.getNodeList().toArray(new String[0]);
			reqTemplate.setHosts(hosts);
			
			ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
			JobLog jobLog = new JobLog();
			UserCommand userCommand = new UserCommand();
			userCommand.cmd = cmd;
			userCommand.nodeGroup = ng;
			userCommand.jobId = getName();
			jobLog.setUserCommand(userCommand);
			
			StandaloneTaskListener listener = new StandaloneTaskListener();
			listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventHandler(DataType.JOBLOG, jobLog));
			new StandaloneTaskExecutor(batchOption, listener, reqTask).execute();
			
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
