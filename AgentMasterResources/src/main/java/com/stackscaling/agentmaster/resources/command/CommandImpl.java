package com.stackscaling.agentmaster.resources.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

/**
 * command impl
 * @author biyu
 *
 */
public class CommandImpl implements ICommand {

	/** name of the command */
	private String name;

	/** http request template */
	private HttpTaskRequest httpTaskRequest;

	/** list of aggregation regex */
	private List<String> aggRegexs;

	/** user inputs */
	private Map<String, String> userData = new HashMap<String, String>();

	/** this command has more logs that can be fetched */
	private boolean hasRawLogs;

	public boolean isHasRawLogs() {
		return hasRawLogs;
	}

	public void setHasRawLogs(boolean hasRawLogs) {
		this.hasRawLogs = hasRawLogs;
	}

	public List<String> getAggRegexs() {
		return aggRegexs;
	}

	public void setAggRegexs(List<String> aggRegexs) {
		this.aggRegexs = aggRegexs;
	}

	/**
	 * do not use externally, use createCopy because ICommand is cached and shared (copy on write)
	 * @return
	 */
	public HttpTaskRequest getHttpTaskRequest() {
		return httpTaskRequest;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public HttpTaskRequest createCopy() {
		return httpTaskRequest.createNew();
	}

	public void setHttpTaskRequest(HttpTaskRequest httpTaskRequest) {
		this.httpTaskRequest = httpTaskRequest;
	}

	@Override
	public void setUserData(Map<String, String> userData) {
		this.userData = userData;
	}

	@Override
	public Map<String, String> getUserData() {
		return userData;
	}

	public CommandImpl addUserData(String key, String value) {
		userData.put(key, value);
		return this;
	}

}
