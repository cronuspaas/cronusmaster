package com.stackscaling.agentmaster.resources.oneclickcommand;

import java.util.HashMap;
import java.util.Map;

import com.stackscaling.agentmaster.resources.BaseUserData;

/**
 * one click command impl
 * @author biyu
 *
 */
public class OneClickCommandImpl extends BaseUserData implements IOneClickCommand {
	
	/** user defined display name */
	private String displayName;

	/** user inputs */
	private Map<String, String> userData = new HashMap<String, String>();
	
	/** cmd key */
	private String commandKey;

	/** ng key */
	private String nodeGroupKey;
	
	/** long description */
	private String description;

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setUserData(Map<String, String> userData) {
		this.userData = userData;
	}

	@Override
	public Map<String, String> getUserData() {
		return userData;
	}

	@Override
	public String getCommandKey() {
		return commandKey;
	}

	@Override
	public void setCommandKey(String commandKey) {
		this.commandKey = commandKey;
	}

	@Override
	public String getNodeGroupKey() {
		return nodeGroupKey;
	}

	@Override
	public void setNodeGroupKey(String nodeGroupKey) {
		this.nodeGroupKey = nodeGroupKey;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
}
