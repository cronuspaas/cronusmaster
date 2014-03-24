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
	
	public enum DataType {
		NODEGROUP("conf", true, NodeGroupImpl.class), 
		AGGREGATION("conf", true, Object.class), 
		COMMAND("conf", true, CommandImpl.class), 
		ADHOCNODEGROUP("adhoc_nodegroups", false, AdhocNodeGroupDataImpl.class), 
		CMDLOG("cmd_logs", false, JobLog.class),
		JOBLOG("job_logs", false, JobLog.class),
		CMDJOB("cmd_jobs", false, CmdIntervalJobImpl.class);
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
	public String readConfigFile(DataType type, String fileName) throws IOException;
	
	/**
	 * save config file of type
	 * @param type
	 * @param configFileContent
	 * @throws IOException
	 */
	public void saveConfigFile(DataType type, String fileName, String configFileContent) throws IOException;
	
	/**
	 * delete config file
	 * @param type
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public void deleteConfigFile(DataType type, String fileName) throws IOException;
	
	/**
	 * list config files
	 * @param type
	 * @return
	 */
	public List<String> listFiles(DataType type);

}
