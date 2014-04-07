package resources.ebay.flow.assetdiscovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lightj.session.CtxProp;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.FlowContext;

import resources.IUserDataDao.DataType;
import resources.IUserInputs;
import resources.UserDataProvider;
import resources.ebay.flow.assetdiscovery.AssetDiscoveryFlowContext.AssetDiscoveryUserInput;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("rawtypes")
public class AssetDiscoveryFlowContext extends FlowContext implements IUserInputs<AssetDiscoveryUserInput> {
	
	public static String[] AgentParameters = new String[] {"scriptLocation", "scriptName"};
	
	@CtxProp(dbType=CtxDbType.BLOB)
	private AssetDiscoveryUserInput userInputs;
	
	private List<Map<String, String>> iaasParams = new ArrayList<Map<String,String>>();
	
	/** agent executeScript uuid, use to at step 2 to retrieve discover os output */
	private final HashMap<String, String> agentUuidMap = new HashMap<String, String>();
	
	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private Set<String> failedAgentHosts = new HashSet<String>();
	
	public void setFailedAgentHosts(Set<String> failedAgentHosts) {
		this.failedAgentHosts = failedAgentHosts;
	}
	public String[] getAgentHosts() {
		if (userInputs.agentHosts==null) {
			userInputs.populateAgentHosts();
		}
		return userInputs.agentHosts;			
	}
	public String[] getAgentParams() {
		return new String[] {"scriptLocation", userInputs.scriptLocation, "scriptName", userInputs.scriptName};
	}
	public String getIaasHost() {
		return userInputs.iaasHost;
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
	
	@Override
	public AssetDiscoveryUserInput getUserInputs() {
		return userInputs;
	}
	@Override
	public void setUserInputs(AssetDiscoveryUserInput userInput) {
		this.userInputs = userInput;
	}
	@Override
	public AssetDiscoveryUserInput getSampleUserInputs() {
		return new AssetDiscoveryUserInput(
				"existing node group", 
				"http://cronus-srepo.vip.ebay.com/packages/discover_os_info.py", 
				"discover_os_info.py", 
				"cmiaas.vip.ebay.com");
	}
	/**
	 * user input
	 * @author biyu
	 *
	 */
	public static class AssetDiscoveryUserInput {
		/** original agent requests */
		@JsonIgnore
		public String[] agentHosts;
		public String agentNodeGroup;
		public String scriptLocation;
		public String scriptName;
	
		/** request to iaas */
		public String iaasHost;
		
		public AssetDiscoveryUserInput() {}
		public AssetDiscoveryUserInput(String agentNodeGroup, String scriptLocation, String scriptName, String iaasHost) {
			this.agentNodeGroup = agentNodeGroup;
			this.scriptLocation = scriptLocation;
			this.scriptName = scriptName;
			this.iaasHost = iaasHost;
		}
		void populateAgentHosts() {
			if (agentHosts == null && agentNodeGroup != null) {
				try {
					agentHosts = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getNodeGroupByName(agentNodeGroup).getNodeList().toArray(new String[0]);
				} catch (IOException e) {
					agentHosts = new String[] {};
				}
			}
		}
	}

}
