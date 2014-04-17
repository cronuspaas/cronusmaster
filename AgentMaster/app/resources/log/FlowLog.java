package resources.log;

import resources.IUserDataDao.DataType;


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
						userWorkflow.workflow.getFlowName());
	}
	@Override
	public String getCommandKey() {
		return userWorkflow.workflow.getFlowName();
	}

}
