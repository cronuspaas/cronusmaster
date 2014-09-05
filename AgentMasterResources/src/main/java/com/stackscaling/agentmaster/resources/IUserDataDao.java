package com.stackscaling.agentmaster.resources;

import java.io.IOException;
import java.util.List;

import com.stackscaling.agentmaster.resources.command.CommandImpl;
import com.stackscaling.agentmaster.resources.job.CmdIntervalJobImpl;
import com.stackscaling.agentmaster.resources.job.FlowIntervalJobImpl;
import com.stackscaling.agentmaster.resources.log.CmdLog;
import com.stackscaling.agentmaster.resources.log.FlowLog;
import com.stackscaling.agentmaster.resources.log.JobLog;
import com.stackscaling.agentmaster.resources.nodegroup.AdhocNodeGroupDataImpl;
import com.stackscaling.agentmaster.resources.nodegroup.NodeGroupImpl;
import com.stackscaling.agentmaster.resources.oneclickcommand.OneClickCommandImpl;
import com.stackscaling.agentmaster.resources.script.ScriptImpl;
import com.stackscaling.agentmaster.resources.workflow.WorkflowMetaImpl;

/**
 * manage persistence and CRUD of user configs
 * actual implementation can be based of local storage, object store, or database
 *
 * @author binyu
 *
 */
public interface IUserDataDao {

	/**
	 * different type of data
	 *
	 * @author binyu
	 *
	 */
	public enum DataType
	{
		NODEGROUP("Predefined NodeGroup", "user_data/predefined_nodegroups", NodeGroupImpl.class),
		ADHOCNODEGROUP("Adhoc NodeGroup", "user_data/adhoc_nodegroups", AdhocNodeGroupDataImpl.class),
		SCRIPT("Predefined Script", "user_data/predefined_scripts", ScriptImpl.class),
		COMMAND("Command", "user_data/commands", CommandImpl.class),
		WORKFLOW("Workflow", "user_data/workflows", WorkflowMetaImpl.class),
		JOBLOG("Job Logs", "user_data/job_logs", JobLog.class),
		FLOWLOG("Workflow Logs", "user_data/flow_logs", FlowLog.class),
		CMDLOG("Command Logs", "user_data/cmd_logs", CmdLog.class),
		CMD_ONECLICK("One Click Command", "user_data/cmd_oneclick", OneClickCommandImpl.class),
		CMDJOB("Command Job", "user_data/cmd_jobs", CmdIntervalJobImpl.class),
		FLOWJOB("Workflow Job", "user_data/wf_jobs", FlowIntervalJobImpl.class),
		;

		private final String path;
		private final Class doKlass;
		private String uuid;
		private final String label;
		DataType(String label, String path, Class doKlass) {
			this.label = label;
			this.path = path;
			this.doKlass = doKlass;
		}
		public String getPath() {
			return path;
		}
		public Class getDoKlass() {
			return doKlass;
		}
		public String getUuid() {
			return uuid==null ? name() : uuid;
		}
		public String getLabel() {
			return label;
		}
	};


	/**
	 * read config file of type
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public String readData(DataType type, String name) throws IOException;

	/**
	 * save config file of type
	 * @param type
	 * @param content
	 * @throws IOException
	 */
	public void saveData(DataType type, String name, String content) throws IOException;

	/**
	 * delete config file
	 * @param type
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public void deleteData(DataType type, String name) throws IOException;

	/**
	 * list config files
	 * @param type
	 * @return
	 */
	public List<String> listNames(DataType type);

}
