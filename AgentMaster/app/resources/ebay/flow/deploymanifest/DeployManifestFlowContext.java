package resources.ebay.flow.deploymanifest;

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


import resources.IUserInputs;

@SuppressWarnings("rawtypes")
public class DeployManifestFlowContext extends FlowContext implements IUserInputs<DeployManifestUserInput> {
	
	@CtxProp(dbType=CtxDbType.BLOB)
	private DeployManifestUserInput userInputs;
	
	/** any agent failed in between */
	@CtxProp(dbType=CtxDbType.BLOB)
	private HashSet<String> failedAgentHosts = new HashSet<String>();
	
	public void setFailedAgentHosts(HashSet<String> failedAgentHosts) {
		this.failedAgentHosts = failedAgentHosts;
	}
	public void addFailedAgentHost(String host) {
		failedAgentHosts.add(host);
	}
	public HashSet<String> getFailedAgentHosts() {
		return failedAgentHosts;
	}
	
	public DeployManifestUserInput getUserInputs() {
		return userInputs;
	}
	public void setUserInputs(DeployManifestUserInput userInput) {
		this.userInputs = userInput;
	}

	public String[] getAllHosts() {
		if (userInputs.agentHosts==null) {
			userInputs.populateAgentHosts();
		}
		return userInputs.agentHosts;			
	}

	public String[] getGoodHosts() {
		// host template
		List<String> goodHosts = new ArrayList<String>();
		for (String host : this.getAllHosts()) {
			if (!this.getFailedAgentHosts().contains(host)) {
				goodHosts.add(host);
			}
		}
		return goodHosts.toArray(new String[0]);
	}

	public DeployManifestUserInput getSampleUserInputs() {
		return new DeployManifestUserInput(
				"existing node group", 
				"service_name", 
				"manifest_name", 
				new String[] {"manifest_pkg"});
	}
}
