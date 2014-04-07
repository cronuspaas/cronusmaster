package resources.ebay.flow.cleanservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.lightj.session.CtxProp;
import org.lightj.session.CtxProp.CtxDbType;
import org.lightj.session.FlowContext;

import resources.IUserDataDao.DataType;
import resources.IUserInputs;
import resources.UserDataProvider;
import resources.ebay.flow.cleanservice.CleanServiceFlowContext.CleanServiceUserInput;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("rawtypes")
public class CleanServiceFlowContext extends FlowContext implements IUserInputs<CleanServiceUserInput> {
	
	@CtxProp(dbType=CtxDbType.BLOB)
	private CleanServiceUserInput userInputs;
	
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
	
	public CleanServiceUserInput getUserInputs() {
		return userInputs;
	}
	public void setUserInputs(CleanServiceUserInput userInput) {
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

	public CleanServiceUserInput getSampleUserInputs() {
		return new CleanServiceUserInput(
				"existing node group", 
				"service_name");
	}
	
	public static class CleanServiceUserInput {
		
		/** original agent requests */
		@JsonIgnore
		public String[] agentHosts;
		public String agentNodeGroup;
		public String serviceName;

		public CleanServiceUserInput() {}
		public CleanServiceUserInput(String agentNodeGroup, String serviceName) {
			this.agentNodeGroup = agentNodeGroup;
			this.serviceName = serviceName;
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
