package resources;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

/**
 * command configs
 * @author binyu
 *
 */
public interface INodeGroupData extends IUserData {
	
	public static final String NODEGROUP_NAME_TAG = "```NODEGROUP_NAME";
	public static final String NODEGROUP_LIST_START_TAG = "```NODEGROUP_START";
	public static final String NODEGROUP_LIST_END_TAG = "```NODEGROUP_END";
	
	public enum NodeGroupType {
		predefined, adhoc
	}

	/**
	 * get all commands back
	 * @return
	 */
	public Map<String, INodeGroup> getAllNodeGroups() throws IOException;
	
	/**
	 * find template by name
	 * @param name
	 * @return
	 */
	public INodeGroup getNodeGroupByName(String name) throws IOException;

	/**
	 * save command configs
	 * @param configFileContent
	 * @throws IOException
	 */
	public void save(String configFileContent) throws IOException;
	
	/**
	 * load all commands from backing storage
	 */
	public void load(String configFileName) throws IOException;
	
	/**
	 * validate config file content
	 * @param configFileContent
	 * @throws InvalidObjectException
	 */
	public void validate(String configFileContent) throws IOException;

	
}
