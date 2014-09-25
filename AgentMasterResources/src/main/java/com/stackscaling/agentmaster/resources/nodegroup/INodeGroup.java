package com.stackscaling.agentmaster.resources.nodegroup;

import java.util.List;

import com.stackscaling.agentmaster.resources.IUserData;

/**
 * group of nodes
 * @author binyu
 *
 */
public interface INodeGroup extends IUserData 
{

	/**
	 * get groups of nodes
	 * @return
	 */
	public List<String> getNodeList();

	/**
	 * get all nodes as an array
	 * @return
	 */
	public String[] getHosts();

	/**
	 * type of node group
	 * @return
	 */
	public String getType();

	/**
	 * set type of node group
	 * @param ngType
	 */
	public void setType(String ngType);

}
