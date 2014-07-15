package com.stackscaling.agentmaster.resources.nodegroup;

import java.io.IOException;
import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserData;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;

/**
 * command configs
 * @author binyu
 *
 */
public interface INodeGroupData extends IUserData {

	public static final INodeGroup NG_EMPTY = new NodeGroupImpl("NG-EMPTY", DataType.NODEGROUP.name(), null);

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
	public void save(String ngName, String configFileContent) throws IOException;

	/**
	 * load all commands from backing storage
	 */
	public void load() throws IOException;


	/** total node count */
	public int getNodeCount();
}
