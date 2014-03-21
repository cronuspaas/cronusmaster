package resources;

import java.io.IOException;
import java.util.List;

/**
 * manage persistence and CRUD of user configs
 * actual implementation can be based of local storage, object store, or database
 * 
 * @author binyu
 *
 */
public interface IUserDataDao {
	
	public enum DataType {
		NODEGROUP("conf", true, "json.conf"), 
		AGGREGATION("conf", true, "json.conf"), 
		COMMAND("conf", true, "json.conf"), 
		ADHOCNODEGROUP("adhoc_nodegroups", false, "json.conf"), 
		LOG("app_logs", false, "json.log");
		private final String path;
		private final String ext;
		private final boolean isFile;
		DataType(String path, boolean isFile, String ext) {
			this.path = path;
			this.ext = ext;
			this.isFile = isFile;
		}
		public boolean isFile() {
			return isFile;
		}
		public String getPath() {
			return path;
		}
		public String getExt() {
			return ext;
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
	 * list config files
	 * @param type
	 * @return
	 */
	public List<String> listFiles(DataType type);

}
