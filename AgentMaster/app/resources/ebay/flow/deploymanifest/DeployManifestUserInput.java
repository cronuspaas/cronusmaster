package resources.ebay.flow.deploymanifest;

import java.io.IOException;

import resources.UserDataProvider;
import resources.IUserDataDao.DataType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * user input
 * @author biyu
 *
 */
public class DeployManifestUserInput {
	/** original agent requests */
	@JsonIgnore
	public String[] agentHosts;
	public String agentNodeGroup;
	public String serviceName;
	public String manifestName;
	public String[] manifestPkgs;

	public DeployManifestUserInput() {}
	public DeployManifestUserInput(String agentNodeGroup, String serviceName, String manifestName, String[] manifestPkgs) {
		this.agentNodeGroup = agentNodeGroup;
		this.serviceName = serviceName;
		this.manifestName = manifestName;
		this.manifestPkgs = manifestPkgs;
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