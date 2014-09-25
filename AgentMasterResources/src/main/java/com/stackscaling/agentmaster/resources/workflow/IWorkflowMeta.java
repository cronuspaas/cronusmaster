package com.stackscaling.agentmaster.resources.workflow;

import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserData;

/**
 * flow data, read only
 * @author biyu
 *
 */
public interface IWorkflowMeta extends IUserData 
{

	public static enum FlowType {
		Predefined, Adhoc
	}

	/** name of the flow */
	public String getFlowName();

	/** type of the flow predefined or adhoc */
	public String getFlowType();

	/** flow class */
	public String getFlowClass();

	/** description */
	public String getFlowDescription();

	/** user data */
	public Map<String, String> getUserData();

}
