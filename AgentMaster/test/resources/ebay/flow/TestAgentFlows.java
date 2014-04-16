package resources.ebay.flow;

import java.util.concurrent.Executors;

import org.lightj.RuntimeContext;
import org.lightj.example.dal.LocalDatabaseEnum;
import org.lightj.initialization.BaseModule;
import org.lightj.initialization.InitializationException;
import org.lightj.initialization.ShutdownException;
import org.lightj.session.FlowModule;
import org.lightj.session.FlowSessionFactory;
import org.lightj.util.JsonUtil;
import org.lightj.util.SpringContextUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import resources.ebay.flow.deploymanifest.DeployManifestFlow;

public class TestAgentFlows extends BaseTestCase {
	
	public void testDeployManifestFlow() throws Exception {
		// create an instance of skeleton flow, fill in the flesh for each steps
		DeployManifestFlow flow = FlowSessionFactory.getInstance().createSession(DeployManifestFlow.class);
		flow.getSessionContext().setServiceName("perlserer");
		flow.getSessionContext().setManifestName("perlserver-1.0.0");
		flow.getSessionContext().setManifestPkgs(new String[]{"http://cronus-srepo.vip.ebay.com/packages/perlserver-1.0.0.unix.cronus"});
		flow.getSessionContext().setHosts(new String[] {"10.9.248.186"});
		flow.save();
		
		// kick off flow
		flow.runFlow();
		
		// checking flow state and print progress
		while (!flow.getState().isComplete()) {
			System.out.println(flow.getFlowInfo().getProgress());
			Thread.sleep(1000);
		}
		System.out.println(JsonUtil.encode(flow.getFlowInfo()));
		
	}

//	public void testCleanServiceFlow() throws Exception {
//		// create an instance of skeleton flow, fill in the flesh for each steps
//		CleanServiceFlow flow = FlowSessionFactory.getInstance()
//				.createSession(CleanServiceFlow.class);
//		flow.getSessionContext().addUserData("hosts", new String[] { "10.9.248.186" });
//		flow.getSessionContext().addUserData("serviceName", "perlserver");
//		
//		flow.save();
//
//		// kick off flow
//		flow.runFlow();
//
//		// checking flow state and print progress
//		while (!flow.getState().isComplete()) {
//			System.out.println(flow.getFlowInfo().getProgress());
//			Thread.sleep(1000);
//		}
//		System.out.println(JsonUtil.encode(flow.getFlowInfo()));
//
//	}

//	public void testAssetDiscoveryFlow() throws Exception {
//		// create an instance of skeleton flow, fill in the flesh for each steps
//		AssetDiscoveryFlow flow = FlowSessionFactory.getInstance().createSession(AssetDiscoveryFlow.class);
//		AssetDiscoveryUserInput userInput = new AssetDiscoveryUserInput();
//		userInput.agentHosts = new String[] {"10.9.248.186"};
//		userInput.scriptLocation = "http://cronus-srepo.vip.ebay.com/packages/discover_os_info.py";
//		userInput.scriptName ="discover_os_info.py";
//		flow.getSessionContext().setUserInputs(userInput);
//		flow.save();
//		
//		// kick off flow
//		flow.runFlow();
//		
//		// checking flow state and print progress
//		while (!flow.getState().isComplete()) {
//			System.out.println(flow.getFlowInfo().getProgress());
//			Thread.sleep(1000);
//		}
//		System.out.println(JsonUtil.encode(flow.getFlowInfo()));
//		
//	}

	@Override
	protected void afterInitialize(String home) throws InitializationException {
	}

	@Override
	protected void afterShutdown() throws ShutdownException {
	}

	@Override
	protected BaseModule[] getDependentModules() {
        RuntimeContext.setClusterUuid("restcommander", "prod", "all", Long.toString(System.currentTimeMillis()));

        AnnotationConfigApplicationContext flowCtx = new AnnotationConfigApplicationContext("resources");
		SpringContextUtil.registerContext("resources", flowCtx);
		return new BaseModule[] {
				new FlowModule().setDb(LocalDatabaseEnum.TESTMEMDB)
								.enableCluster()
								.setSpringContext(flowCtx)
								.setExectuorService(Executors.newFixedThreadPool(5))
								.getModule(),
		};
	}

}
