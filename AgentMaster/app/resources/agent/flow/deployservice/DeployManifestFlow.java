package resources.agent.flow.deployservice;

import org.lightj.session.FlowProperties;
import org.lightj.session.FlowResult;
import org.lightj.session.FlowSession;
import org.lightj.session.FlowState;
import org.lightj.session.FlowStepProperties;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.StepTransition;
import org.springframework.beans.factory.annotation.Autowired;

@FlowProperties(typeId="DeployManifest", desc="deploy manifest", clustered=false, interruptible=false, timeoutInSec=0)
public class DeployManifestFlow extends FlowSession<DeployManifestFlowContext> {

	//////////////// step implementation /////////////////
	
	@Autowired(required=true)
	private IFlowStep createServiceIfNeededStep;
	@Autowired(required=true)
	private IFlowStep createManifestStep;
	@Autowired(required=true)
	private IFlowStep activateManifestStep;
	
	// method with the same name as in flow step enum, framework will use reflection to run each step
	@FlowStepProperties(stepWeight=1, isFirstStep=true, stepIdx=10, onSuccess="createManifest", onElse="handleError")
	public IFlowStep createServiceIfNeeded() {
		return createServiceIfNeededStep;
	}	
	@FlowStepProperties(stepWeight=1, stepIdx=20, onSuccess="activateManifest", onElse="handleError")
	public IFlowStep createManifest() {
		return createManifestStep;
	}	
	@FlowStepProperties(stepWeight=1, stepIdx=30, onSuccess="stop", onElse="handleError")
	public IFlowStep activateManifest() {
		return activateManifestStep;
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
