package com.stackscaling.agentmaster.resources.oneclickcommand;

import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserData;

public interface IOneClickCommand extends IUserData 
{

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
