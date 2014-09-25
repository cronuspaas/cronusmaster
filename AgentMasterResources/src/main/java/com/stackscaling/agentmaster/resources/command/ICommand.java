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

	/** this command has raw logs */
	public boolean isHasRawLogs();

	/** the raw log of this command has been fetched and indexed */
	public void setHasRawLogs(boolean hasRawLogs);

}
