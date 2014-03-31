package resources.ebay.flow.assetdiscovery;

import org.lightj.session.FlowProperties;
import org.lightj.session.FlowResult;
import org.lightj.session.FlowSession;
import org.lightj.session.FlowState;
import org.lightj.session.FlowStepProperties;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.StepImpl;
import org.lightj.session.step.StepTransition;
import org.springframework.beans.factory.annotation.Autowired;

@FlowProperties(typeId="AssetDiscovery", desc="run asset discovery", clustered=false, interruptible=false, timeoutInSec=0)
public class AssetDiscoveryFlow extends FlowSession<AssetDiscoveryFlowContext> {

	//////////////// step implementation /////////////////
	
	@Autowired(required=true)
	private IFlowStep discoverAssetsStep;
	@Autowired(required=true)
	private IFlowStep retrieveAssetPayloadStep;
	@Autowired(required=true)
	private IFlowStep registerAssetsStep;
	
	// method with the same name as in flow step enum, framework will use reflection to run each step
	@FlowStepProperties(stepWeight=1, isFirstStep=true, stepIdx=10, onSuccess="retrieveAssetPayload", onElse="handleError")
	public IFlowStep discoverAssets() {
		return discoverAssetsStep;
	}	
	@FlowStepProperties(stepWeight=1, stepIdx=20, onSuccess="registerAssets", onElse="handleError")
	public IFlowStep retrieveAssetPayload() {
		return retrieveAssetPayloadStep;
	}	
	@FlowStepProperties(stepWeight=1, stepIdx=30, onSuccess="stop", onElse="handleError")
	public IFlowStep registerAssets() {
		return registerAssetsStep;
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
