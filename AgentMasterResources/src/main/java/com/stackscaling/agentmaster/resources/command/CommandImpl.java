package com.stackscaling.agentmaster.resources.command;

import java.util.HashMap;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

import com.stackscaling.agentmaster.resources.BaseUserData;

/**
 * command impl
 * @author biyu
 *
 */
public class CommandImpl extends BaseUserData implements ICommand {

	/** http request template */
	private HttpTaskRequest httpTaskRequest;

	/** user inputs */
	private Map<String, String> userData = new HashMap<String, String>();

	/** category of the command */
	private String category;

	/**
	 * do not use externally, use createCopy because ICommand is cached and shared (copy on write)
	 * @return
	 */
	public HttpTaskRequest getHttpTaskRequest() {
		return httpTaskRequest;
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

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public void setCategory(String category) {
		this.category = category;
	}

}
