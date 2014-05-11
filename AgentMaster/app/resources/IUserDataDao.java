package resources;

import java.io.IOException;
import java.util.List;

import resources.command.CommandImpl;
import resources.job.CmdIntervalJobImpl;
import resources.job.FlowIntervalJobImpl;
import resources.log.FlowLog;
import resources.log.BaseLog;
import resources.nodegroup.AdhocNodeGroupDataImpl;
import resources.nodegroup.NodeGroupImpl;
import resources.workflow.WorkflowMetaImpl;

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
		COMMAND("Command", "user_data/commands", CommandImpl.class), 
		WORKFLOW("Workflow", "user_data/workflows", WorkflowMetaImpl.class),
		ADHOCNODEGROUP("Adhoc NodeGroup", "user_data/adhoc_nodegroups", AdhocNodeGroupDataImpl.class), 
		JOBLOG("Job Logs", "user_data/job_logs", BaseLog.class),
		FLOWLOG("Workflow Logs", "user_data/flow_logs", FlowLog.class),
		CMDLOG("Command Logs", "user_data/cmd_logs", BaseLog.class),
		CMDJOB("Command Job", "user_data/cmd_jobs", CmdIntervalJobImpl.class),
		FLOWJOB("Workflow Job", "user_data/wf_jobs", FlowIntervalJobImpl.class);
		
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
		public void setUuid(String uuid) {
			this.uuid = uuid;
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
