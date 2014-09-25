package com.stackscaling.agentmaster.resources.command;

import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

import com.stackscaling.agentmaster.resources.IUserData;

public interface ICommand extends IUserData 
{

	/** http request */
	public HttpTaskRequest createCopy();

	/** set http request */
	public void setHttpTaskRequest(HttpTaskRequest httpTaskRequest);

	/** user data */
	public void setUserData(Map<String, String> userData);

	/** user data */
	public Map<String, String> getUserData();

	/** sub category of the command, 
	 * can be used to lookup and apply use case specific request enhancer 
	 * e.g. add common headers and parameters for all agent requests
	 */ 
	public String getCategory();
	
	/** set sub category of the command */
	public void setCategory(String category);

}
