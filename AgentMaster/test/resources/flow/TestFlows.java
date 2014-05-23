package resources.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.junit.Test;
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

import play.test.UnitTest;

import resources.flow.agent.InstallAgentFlow;
import resources.flow.agent.InstallAgentFlowContext;


public class TestFlows extends UnitTest {
	
	@Test
	public void testInstallAgentFlow() throws Exception {
		// create an instance of skeleton flow, fill in the flesh for each steps
		InstallAgentFlow flow = FlowSessionFactory.getInstance().createSession(InstallAgentFlow.class);
		InstallAgentFlowContext context = flow.getSessionContext();
		context.setHosts(Arrays.asList(new String[] {"192.168.1.127"}));
		context.setSshUser("binyu");
		context.setPrivateKeyFile("c:/Users/binyu/.ssh/id_rsa");
		context.setPassPhrase("il0v3j3nny");
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

}

