package resources.ebay.flow.assetdiscovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lightj.session.CtxProp;
import org.lightj.session.FlowContext;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.exception.FlowExecutionException;

import resources.UserDataProvider;
import resources.IUserDataDao.DataType;

@SuppressWarnings("rawtypes")
public class AssetDiscoveryFlowContext extends FlowContext {
	
	public static String[] AgentParameters = new String[] {"scriptLocation", "scriptName"};
	
	@CtxProp(dbType=CtxDbType.BLOB)
	private AssetDiscoveryUserInput userInput;
	
	private List<Map<String, String>> iaasParams = new ArrayList<Map<String,String>>();
	
	/** agent executeScript uuid, use to at step 2 to retrieve discover os output */
	private final HashMap<String, String> agentUuidMap = new HashMap<String, String>();
	
	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private final Set<String> failedAgentHosts = new HashSet<String>();
	
	public String[] getAgentHosts() {
		return userInput.agentHosts;
	}
	public String[] getAgentParams() {
		return new String[] {"scriptLocation", userInput.scriptLocation, "scriptName", userInput.scriptName};
	}
	public String getIaasHost() {
		return userInput.iaasHost;
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
	
	public AssetDiscoveryUserInput getUserInput() {
		return userInput;
	}
	public void setUserInput(AssetDiscoveryUserInput userInput) {
		this.userInput = userInput;
	}

	/**
	 * user input
	 * @author biyu
	 *
	 */
	public static class AssetDiscoveryUserInput {
		/** original agent requests */
		String[] agentHosts;
		String scriptLocation;
		String scriptName;
	
		/** request to iaas */
		String iaasHost;
	}
	
}
