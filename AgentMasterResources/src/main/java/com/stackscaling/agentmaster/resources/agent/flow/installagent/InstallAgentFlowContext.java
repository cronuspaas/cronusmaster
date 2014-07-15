package com.stackscaling.agentmaster.resources.agent.flow.installagent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.lightj.session.CtxProp;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.CtxProp.CtxSaveType;
import org.lightj.session.FlowContext;
import org.lightj.task.BatchOption;

public class InstallAgentFlowContext extends FlowContext {

	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private HashSet<String> failedHosts = new HashSet<String>();

	// user data
	@CtxProp(isUserData=true, sampleUserDataValue="[\"host\"]", saveType=CtxSaveType.NoSave)
	private List<String> hosts;

	@CtxProp(isUserData=true, sampleUserDataValue="user", saveType=CtxSaveType.NoSave)
	private String sshUser;

	@CtxProp(isUserData=true, sampleUserDataValue="path_to_rsaid_file", saveType=CtxSaveType.NoSave)
	private String privateKeyFile;

	@CtxProp(isUserData=true, sampleUserDataValue="rsa_id_passphrase", saveType=CtxSaveType.NoSave)
	private String passPhrase;

	@CtxProp(isUserData=true, sampleUserDataValue="/var", saveType=CtxSaveType.NoSave)
	private String installRoot;

	@CtxProp(isUserData=true, sampleUserDataValue="0.1.23", saveType=CtxSaveType.NoSave)
	private String version;

	@CtxProp(isUserData=true, sampleUserDataValue="UNLIMITED,0", saveType=CtxSaveType.NoSave)
	private BatchOption batchOption;

	public String getInstallRoot() {
		return installRoot;
	}
	public void setInstallRoot(String installRoot) {
		this.installRoot = installRoot;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public List<String> getHosts() {
		return hosts;
	}
	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}
	public BatchOption getBatchOption() {
		return batchOption;
	}
	public void setBatchOption(BatchOption batchOption) {
		this.batchOption = batchOption;
	}
	public String getSshUser() {
		return sshUser;
	}
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	public String getPrivateKeyFile() {
		return privateKeyFile;
	}
	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}
	public String getPassPhrase() {
		return passPhrase;
	}
	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
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
	public List<String> getGoodHosts() {
		// host template
		List<String> goodHosts = new ArrayList<String>();
		for (String host : this.getHosts()) {
			if (!this.getFailedHosts().contains(host)) {
				goodHosts.add(host);
			}
		}
		return goodHosts;
	}

}
