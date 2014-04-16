package resources.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * workflow metadata impl
 * @author biyu
 *
 */
public class WorkflowMetaImpl implements IWorkflowMeta {
	
	private String flowName;
	private String flowType;
	private String flowClass;
	private String flowDescription;
	private Map<String, String> userData = new HashMap<String, String>();

	@Override
	public String getFlowName() {
		return flowName;
	}

	@Override
	public String getFlowDescription() {
		return flowDescription;
	}

	@Override
	public Map<String, String> getUserData() {
		return Collections.unmodifiableMap(userData);
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public void setFlowDescription(String flowDescription) {
		this.flowDescription = flowDescription;
	}

	public void setUserData(Map<String, String> userInputs) {
		this.userData = userInputs;
	}
	
	public void addUserData(String name, String value) {
		this.userData.put(name, value);
	}

	@Override
	public String getFlowType() {
		return flowType;
	}

	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}

	public String getFlowClass() {
		return flowClass;
	}

	public void setFlowClass(String flowClass) {
		this.flowClass = flowClass;
	}

}
