package com.stackscaling.agentmaster.resources.command;

import java.util.List;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

public interface ICommand {

	/** name of the command */
	public String getName();

	/** name of the command */
	public void setName(String name);

	/** http request */
	public HttpTaskRequest createCopy();

	/** aggregation regex */
	public List<String> getAggRegexs();

	/** aggregation regex */
	public void setAggRegexs(List<String> aggRegexs);

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
