package com.stackscaling.agentmaster.resources.agent.flow.installagent;

import org.lightj.session.FlowProperties;
import org.lightj.session.FlowResult;
import org.lightj.session.FlowSession;
import org.lightj.session.FlowState;
import org.lightj.session.FlowStepProperties;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.StepTransition;
import org.springframework.beans.factory.annotation.Autowired;

@FlowProperties(
		typeId="InstallAgent",
		desc="install agent via ssh, use PKI authentication, the user must have sudo access to install the agent",
		clustered=false,
		interruptible=false,
		timeoutInSec=0
)
public class InstallAgentFlow extends FlowSession<InstallAgentFlowContext> {

	//////////////// step implementation /////////////////

	@Autowired(required=true)
	private IFlowStep installAgentStep;

	// method with the same name as in flow step enum, framework will use reflection to run each step
	@FlowStepProperties(stepWeight=1, isFirstStep=true, stepIdx=10, onSuccess="stop", onElse="handleError")
	public IFlowStep installAgent() {
		return installAgentStep;
	}

	@FlowStepProperties(stepWeight=1, stepIdx=40)
	public IFlowStep stop() {
		return new StepBuilder().parkInState(StepTransition.parkInState(FlowState.Completed, FlowResult.Success, null)).getFlowStep();
	}

	@FlowStepProperties(stepWeight=0, isErrorStep=true, stepIdx=100)
	public IFlowStep handleError() {
		return new StepBuilder().parkInState(StepTransition.parkInState(FlowState.Completed, FlowResult.Failed, "something wrong")).getFlowStep();
	}

}
