package resources;

import java.io.IOException;
import java.util.List;

import resources.command.CommandImpl;
import resources.job.CmdIntervalJobImpl;
import resources.log.JobLog;
import resources.nodegroup.AdhocNodeGroupDataImpl;
import resources.nodegroup.NodeGroupImpl;

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
	public enum DataType {
		NODEGROUP("user_data/predefined_nodegroups", false, NodeGroupImpl.class), 
		AGGREGATION("conf", true, Object.class), 
		COMMAND("user_data/commands", false, CommandImpl.class), 
		ADHOCNODEGROUP("user_data/adhoc_nodegroups", false, AdhocNodeGroupDataImpl.class), 
		CMDLOG("user_data/cmd_logs", false, JobLog.class),
		JOBLOG("user_data/job_logs", false, JobLog.class),
		CMDJOB("user_data/cmd_jobs", false, CmdIntervalJobImpl.class);
		
		private final String path;
		private final boolean isFile;
		private final Class doKlass;
		DataType(String path, boolean isFile, Class doKlass) {
			this.path = path;
			this.isFile = isFile;
			this.doKlass = doKlass;
		}
		public boolean isFile() {
			return isFile;
		}
		public String getPath() {
			return path;
		}
		public Class getDoKlass() {
			return doKlass;
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
