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
		NODEGROUP("conf", true), 
		AGGREGATION("conf", true), 
		COMMAND("conf", true), 
		ADHOCNODEGROUP("app_logs", false), 
		LOG("app_logs", false);
		private final String path;
		private final boolean isFile;
		DataType(String path, boolean isFile) {
			this.path = path;
			this.isFile = isFile;
		}
		private DataType() {
			this(null, true);
		}
		public boolean isFile() {
			return isFile;
		}
		public String getPath() {
			return path;
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
