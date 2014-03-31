package resources.ebay.flow.assetdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lightj.session.FlowContext;

@SuppressWarnings("rawtypes")
public class AssetDiscoveryFlowContext extends FlowContext {
	
	public static String[] AgentParameters = new String[] {"scriptLocation", "scriptName"};
	
	/** original agent requests */
	private String[] agentHosts;
	private HashMap<String, String> agentParams;
	
	/** request to iaas */
	private String iaasHost;
	private List<Map<String, String>> iaasParams = new ArrayList<Map<String,String>>();
	
	/** agent executeScript uuid, use to at step 2 to retrieve discover os output */
	private final HashMap<String, String> agentUuidMap = new HashMap<String, String>();
	
	/** any agent failed in between */
	private final Set<String> failedAgentHosts = new HashSet<String>();
	
	public String[] getAgentHosts() {
		return agentHosts;
	}
	public void setAgentHosts(String[] hosts) {
		this.agentHosts = hosts;
	}
	public HashMap<String, String> getAgentParams() {
		return agentParams;
	}
	public void setAgentParams(HashMap<String, String> agentParams) {
		this.agentParams = agentParams;
	}
	public String getIaasHost() {
		return iaasHost;
	}
	public void setIaasHost(String iaasHost) {
		this.iaasHost = iaasHost;
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
	public void addFailedAgentHost(String host) {
		failedAgentHosts.add(host);
	}
	public Set<String> getFailedAgentHosts() {
		return failedAgentHosts;
	}
	
}
