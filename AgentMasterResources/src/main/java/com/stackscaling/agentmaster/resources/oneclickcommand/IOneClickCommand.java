package com.stackscaling.agentmaster.resources.oneclickcommand;

import java.util.Map;

public interface IOneClickCommand {

	/** name of the command */
	public String getName();
	
	/** name of the command */
	public void setName(String name);

	/** user defined display name */
	public String getDisplayName();
	
	/** user defined display name */
	public void setDisplayName(String displayName);

	/** user data */
	public void setUserData(Map<String, String> userData);

	/** user data */
	public Map<String, String> getUserData();

	/** command */
	public String getCommandKey();
	
	/** command */
	public void setCommandKey(String commandKey);
	
	/** node group */
	public String getNodeGroupKey();

	/** node group */
	public void setNodeGroupKey(String nodeGroupKey);
	
	/** long description */
	public void setDescription(String description);
	
	/** long description */
	public String getDescription();

}
