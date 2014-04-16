package resources.ebay.flow.assetdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lightj.session.CtxProp;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.CtxProp.CtxSaveType;
import org.lightj.session.FlowContext;
import org.lightj.task.BatchOption;

@SuppressWarnings("rawtypes")
public class AssetDiscoveryFlowContext extends FlowContext {
	
	@CtxProp(isUserData=true, sampleUserDataValue="10.10.10.10", saveType=CtxSaveType.NoSave)
	public String[] hosts;
	@CtxProp(isUserData=true, sampleUserDataValue="http://host:port/somescript", saveType=CtxSaveType.NoSave)
	public String scriptLocation;
	@CtxProp(isUserData=true, sampleUserDataValue="fact.sh", saveType=CtxSaveType.NoSave)
	public String scriptName;
	@CtxProp(isUserData=true, sampleUserDataValue="UNLIMITED,0", saveType=CtxSaveType.NoSave)
	private BatchOption batchOption;
	@CtxProp(isUserData=true, sampleUserDataValue="10.10.10.10", saveType=CtxSaveType.NoSave)
	private String iaasHost;

	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private Set<String> failedHosts = new HashSet<String>();
	
	private List<Map<String, String>> iaasParams = new ArrayList<Map<String,String>>();
	
	/** agent executeScript uuid, use to at step 2 to retrieve discover os output */
	private final HashMap<String, String> agentUuidMap = new HashMap<String, String>();
	
	// USER DATA
	public String[] getHosts() {
		return hosts;
	}
	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}
	public String getScriptLocation() {
		return scriptLocation;
	}
	public void setScriptLocation(String scriptLocation) {
		this.scriptLocation = scriptLocation;
	}
	public String getScriptName() {
		return scriptName;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	public BatchOption getBatchOption() {
		return batchOption;
	}
	public void setBatchOption(BatchOption batchOption) {
		this.batchOption = batchOption;
	}
	public void setIaasHost(String iaasHost) {
		this.iaasHost = iaasHost;
	}
	public String getIaasHost() {
		return iaasHost;
	}
	// END USER DATA
	

	public void setFailedHosts(Set<String> failedAgentHosts) {
		this.failedHosts = failedAgentHosts;
	}
	public void addFailedHost(String host) {
		failedHosts.add(host);
	}
	public Set<String> getFailedHosts() {
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


	public List<Map<String, String>> getIaasParams() {
		return iaasParams;
	}
	public void setIaasParams(List<Map<String, String>> iaasParams) {
		this.iaasParams = iaasParams;
	}
	public void addIaaSParam(Map<String, String> iaasParam) {
		this.iaasParams.add(iaasParam);
	}
	
	public void addAgentUuid(String host, String uuid) {
		agentUuidMap.put(host, uuid);
	}
	public HashMap<String, String> getAgentUuidMap() {
		return agentUuidMap;
	}
}
