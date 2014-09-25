package com.stackscaling.agentmaster.resources.job;

import java.util.Map;

import com.stackscaling.agentmaster.resources.BaseUserData;

/**
 * base interval job
 * @author binyu
 *
 */
public abstract class BaseIntervalJob extends BaseUserData implements IntervalJob {

	/** interval in minutes */
	protected int intervalInMinute;

	/** job status */
	protected boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	@Override
	public int getIntervalInMinute() {
		return intervalInMinute;
	}
	@Override
	public void setIntervalInMinute(int intervalInMinute) {
		this.intervalInMinute = intervalInMinute;
	}

	/** command to be run */
	protected String cmdName;
	/** node group to run on */
	protected String nodeGroupName;
	/** user data */
	protected Map<String, String> userData;

	public String getCmdName() {
		return cmdName;
	}
	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}
	public String getNodeGroupName() {
		return nodeGroupName;
	}
	public void setNodeGroupName(String nodeGroupName) {
		this.nodeGroupName = nodeGroupName;
	}
	public Map<String, String> getUserData() {
		return userData;
	}
	public void setUserData(Map<String, String> userData) {
		this.userData = userData;
	}

	public abstract void runJobAsync();

}
