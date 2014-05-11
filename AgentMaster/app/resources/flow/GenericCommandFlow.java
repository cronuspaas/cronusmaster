package resources.flow;

import org.lightj.session.FlowProperties;
import org.lightj.session.FlowResult;
import org.lightj.session.FlowSession;
import org.lightj.session.FlowState;
import org.lightj.session.FlowStepProperties;
import org.lightj.session.exception.FlowExecutionException;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.SimpleStepExecution;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.StepTransition;
import org.springframework.beans.factory.annotation.Autowired;

@FlowProperties(typeId="GenericCommand", desc="run generic commands in sequence", clustered=false, interruptible=false, timeoutInSec=0)
public class GenericCommandFlow extends FlowSession<GenericCommandFlowContext> {

	//////////////// step implementation /////////////////
	
	@Autowired(required=true)
	private IFlowStep executeCommandStep;
	
	private IFlowStep checkItineraryStep;
	
	// method with the same name as in flow step enum, framework will use reflection to run each step
	@FlowStepProperties(stepWeight=1, isFirstStep=true, stepIdx=10, onSuccess="checkItinerary", onElse="handleError")
	public IFlowStep executeCommand() {
		return executeCommandStep;
	}
	
	@FlowStepProperties(stepWeight=1, stepIdx=20, onException="handleError")
	public IFlowStep checkItinerary() {
		if (checkItineraryStep == null) {
			checkItineraryStep = new StepBuilder().execute(new SimpleStepExecution<GenericCommandFlowContext>() {
				@Override
				public StepTransition execute() throws FlowExecutionException {
					return StepTransition.runToStep(sessionContext.hasMoreCommand() ? "executeCommand" : "stop"); 
				}

			}).getFlowStep();
		}
		return checkItineraryStep;
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
