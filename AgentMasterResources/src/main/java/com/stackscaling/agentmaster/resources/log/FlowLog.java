package com.stackscaling.agentmaster.resources.log;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;


/**
 * command result
 * @author binyu
 *
 */
public class FlowLog extends BaseLog {

	private UserWorkflow userWorkflow;

	public FlowLog() {
		super(DataType.WORKFLOW);
	}

	public UserWorkflow getUserWorkflow() {
		return userWorkflow;
	}
	public void setUserWorkflow(UserWorkflow userWorkflow) {
		this.userWorkflow = userWorkflow;
	}

	public String uuid() {
		return String.format("%s~%s~%s~%s",
						timestamp,
						nodeGroup.getType(),
						nodeGroup.getName(),
						commandKey);
	}
}
