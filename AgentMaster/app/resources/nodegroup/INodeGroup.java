package resources.nodegroup;

import java.util.List;

/**
 * group of nodes
 * @author binyu
 *
 */
public interface INodeGroup {
	
	/** node group name */
	public String getName();
	
	/**
	 * set node group name
	 * @param name
	 */
	public void setName(String name);
	
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
