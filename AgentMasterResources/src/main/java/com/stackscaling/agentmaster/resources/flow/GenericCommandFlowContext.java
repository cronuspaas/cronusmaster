package com.stackscaling.agentmaster.resources.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.lightj.session.CtxProp;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.CtxProp.CtxSaveType;
import org.lightj.session.FlowContext;
import org.lightj.task.BatchOption;

public class GenericCommandFlowContext extends FlowContext {

	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private HashSet<String> failedHosts = new HashSet<String>();

	// user data
	@CtxProp(isUserData=true, sampleUserDataValue="[\"host\"]", saveType=CtxSaveType.NoSave)
	private String[] hosts;
	@CtxProp(isUserData=true, sampleUserDataValue="UNLIMITED,0", saveType=CtxSaveType.NoSave)
	private BatchOption batchOption;
	@CtxProp(isUserData=true, sampleUserDataValue="[\"command1\", \"command2\"]", saveType=CtxSaveType.NoSave)
	private List<String> commands;
	@CtxProp(isUserData=true, sampleUserDataValue="{\"command1\" : \"{key: value}\", \"command2\": \"{key: value}\"}", saveType=CtxSaveType.NoSave)
	private Map<String, Map<String, String>> cmdUserData;

	@CtxProp(dbType=CtxDbType.VARCHAR, saveType=CtxSaveType.AutoSave)
	private int curCmdIdx = 0;

	public String[] getHosts() {
		return hosts;
	}
	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}
	public Map<String, Map<String, String>> getCmdUserData() {
		return cmdUserData;
	}
	public void setCmdUserData(Map<String, Map<String, String>> cmdUserData) {
		this.cmdUserData = cmdUserData;
	}
	public List<String> getCommands() {
		return commands;
	}
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	public BatchOption getBatchOption() {
		return batchOption;
	}
	public void setBatchOption(BatchOption batchOption) {
		this.batchOption = batchOption;
	}

	// failed hosts
	public void setFailedHosts(HashSet<String> failedAgentHosts) {
		this.failedHosts = failedAgentHosts;
	}
	public void addFailedHost(String host) {
		failedHosts.add(host);
	}
	public HashSet<String> getFailedHosts() {
		return failedHosts;
	}
	public String[] getGoodHosts() {
		// host template
		List<String> goodHosts = new ArrayList<String>();
		for (String host : this.getHosts()) {
			if (!this.getFailedHosts().contains(host)) {
				goodHosts.add(host);
			}
		}
		return goodHosts.toArray(new String[0]);
	}

	public void incCurCmdIdx() {
		curCmdIdx++;
	}
	public int getCurCmdIdx() {
		return curCmdIdx;
	}
	public void setCurCmdIdx(int curCmdIdx) {
		this.curCmdIdx = curCmdIdx;
	}
	public boolean hasMoreCommand() {
		return curCmdIdx < commands.size();
	}
	public String getCurrentCommand() {
		return commands.get(curCmdIdx);
	}
	public Map<String, String> getCommandUserData(String cmdName) {
		return cmdUserData.containsKey(cmdName) ? cmdUserData.get(cmdName) : new HashMap<String, String>();
	}


}
