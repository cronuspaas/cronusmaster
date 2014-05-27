package resources.agent.flow.cleanupservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.lightj.session.CtxProp;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.CtxProp.CtxSaveType;
import org.lightj.session.FlowContext;
import org.lightj.task.BatchOption;

@SuppressWarnings("rawtypes")
public class CleanServiceFlowContext extends FlowContext {
	
	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private HashSet<String> failedHosts = new HashSet<String>();
	
	// user data
	@CtxProp(isUserData=true, sampleUserDataValue="10.10.10.10", saveType=CtxSaveType.NoSave)
	private String[] hosts;
	@CtxProp(isUserData=true, sampleUserDataValue="myservice", saveType=CtxSaveType.NoSave)
	private String serviceName;
	@CtxProp(isUserData=true, sampleUserDataValue="UNLIMITED,0", saveType=CtxSaveType.NoSave)
	private BatchOption batchOption;
	
	public String[] getHosts() {
		return hosts;
	}
	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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

}
