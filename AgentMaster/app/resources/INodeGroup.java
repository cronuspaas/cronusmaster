package resources;

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

}
