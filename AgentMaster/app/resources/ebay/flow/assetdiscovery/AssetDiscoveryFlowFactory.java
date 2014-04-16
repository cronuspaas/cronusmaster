package resources.ebay.flow.assetdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.TaskFactoryStepExecution.IFlowContextTaskFactory;
import org.lightj.session.step.TaskFactoryStepExecution.TaskInFlow;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.ITaskEventHandler;
import org.lightj.task.MonitorOption;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.SimpleHttpResponse;
import org.lightj.task.asynchttp.UrlTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@SuppressWarnings("rawtypes")
@Configuration
public class AssetDiscoveryFlowFactory {
	
	public @Bean @Scope("prototype") static AssetDiscoveryFlow assetDiscoveryFlow() {
		return new AssetDiscoveryFlow();
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep discoverAssetsStep() {
		
		final String reqUrl = UrlTemplate.encodeAllVariables("https://host:12020/admin/executeScript", "host");
		final String reqBody = String.format("{\"script-location\": \"%s\", \"script-name\": \"%s\", " +
				"\"need-sudo\": \"true\", \"sudo-target\": \"cronusapp\"}", "<scriptLocation>", "<scriptName>");
		final String pollUrl = UrlTemplate.encodeAllVariables("https://host:12020/status/uuid", "host", "uuid");
		
		final ITaskEventHandler<AssetDiscoveryFlowContext> myHandler = new SimpleTaskEventHandler<AssetDiscoveryFlowContext>() {
			@Override
			public void executeOnResult(AssetDiscoveryFlowContext ctx, Task task, TaskResult result) {
				if (result.getStatus() == TaskResultEnum.Success) {
					ctx.addAgentUuid(result.getResultDetail("host"), result.getResultDetail("uuid"));
				}
				else {
					ctx.addFailedHost(result.getResultDetail("host"));
				}
			}
		};
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<AssetDiscoveryFlowContext>() {

			@Override
			public TaskInFlow<AssetDiscoveryFlowContext> createTaskInFlow(
					AssetDiscoveryFlowContext context, int sequence) 
			{
				// create task request
				HttpTaskRequest taskReq = new HttpTaskRequest();
				// async poll task
				UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
				reqTemplate.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
				UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
				taskReq.setAsyncPollTaskOption("httpClient", reqTemplate, new ExecuteOption(0,0,3,0), pollTemplate, new MonitorOption(0, 5, 120, 3, 0), "agentPollProcessor");
				// set hosts
				taskReq.setHosts(context.getGoodHosts());
				// set template values
				HostTemplateValues varValues = new HostTemplateValues();
				varValues.addNewTemplateValue("scriptLocation", context.getScriptLocation())
						.addToCurrentTemplate("scriptName", context.getScriptName());
				taskReq.setTemplateValuesForAllHosts(varValues);
				// set global context
				taskReq.setGlobalContext("agentAuthKeyContext");
				// build real task
				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
				
				return new TaskInFlow<AssetDiscoveryFlowContext>(new BatchOption(0, Strategy.UNLIMITED), myHandler, task);
			}
			
		}).getFlowStep();
		
				
	}
	
	@Bean 
	@Scope("prototype")
	public static IFlowStep retrieveAssetPayloadStep() {
		
		final ITaskEventHandler<AssetDiscoveryFlowContext> myHandler = new SimpleTaskEventHandler<AssetDiscoveryFlowContext>() {
			@Override
			public void executeOnResult(AssetDiscoveryFlowContext ctx, Task task, TaskResult result) {
				if (result.getStatus() == TaskResultEnum.Success) {
					Map<String, String> iaasParam = new HashMap<String, String>();
					iaasParam.put("body", result.<SimpleHttpResponse>getRawResult().getResponseBody());
					System.out.println(result.<SimpleHttpResponse>getRawResult().getResponseBody());
					ctx.addIaaSParam(iaasParam);
				}
				else {
					ctx.addFailedHost(result.getResultDetail("host"));
				}
			}
		};

		final String reqUrl = UrlTemplate.encodeAllVariables("https://host:12020/agent/execoutput/uuid", "host", "uuid");
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<AssetDiscoveryFlowContext>() {

			@Override
			public TaskInFlow<AssetDiscoveryFlowContext> createTaskInFlow(
					AssetDiscoveryFlowContext context, int sequence) {
				// create http task req
				HttpTaskRequest taskReq = new HttpTaskRequest();
				// set sync req
				UrlTemplate reqTemplate = new UrlTemplate(reqUrl);
				reqTemplate.addHeader("content-type", "application/json");
				taskReq.setSyncTaskOptions("httpClient", reqTemplate, new ExecuteOption(0,0,3,0), null);
				// host template
				for (String host : context.getGoodHosts()) {
					HostTemplateValues hostTemplate = new HostTemplateValues().addNewTemplateValue("uuid", context.getAgentUuidMap().get(host));
					taskReq.addHostTemplateValues(host, hostTemplate);
				}
				taskReq.setHosts(context.getGoodHosts());
				// build real task
				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
				
				
				return new TaskInFlow<AssetDiscoveryFlowContext>(new BatchOption(0, Strategy.UNLIMITED), myHandler, task);
			}
			
		}).getFlowStep();
		
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep registerAssetsStep() {
		
		final String reqUrl = UrlTemplate.encodeAllVariables("http://host/onboarding/v1/onboardAsset", "host");
		final String reqBody = UrlTemplate.encodeAllVariables("body", "body");
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<AssetDiscoveryFlowContext>() {

			@Override
			public TaskInFlow<AssetDiscoveryFlowContext> createTaskInFlow(
					AssetDiscoveryFlowContext context, int sequence) 
			{
				// create req
				HttpTaskRequest taskReq = new HttpTaskRequest();
				// sync task req
				UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
				reqTemplate.addHeader("content-type", "application/json");
				taskReq.setSyncTaskOptions("httpClient", reqTemplate, new ExecuteOption(), null);
				// host
				taskReq.setHosts(context.getIaasHost());
				// multiple request to the same host
				HostTemplateValues templateValues = new HostTemplateValues().addAllTemplateValues(context.getIaasParams());
				taskReq.addHostTemplateValues(context.getIaasHost(), templateValues);
				// build real task
				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
				
				return new TaskInFlow<AssetDiscoveryFlowContext>(new BatchOption(0, Strategy.UNLIMITED), null, task);
			}
			
		}).getFlowStep();
		
	}

}
