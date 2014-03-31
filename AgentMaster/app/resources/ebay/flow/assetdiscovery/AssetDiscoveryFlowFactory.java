package resources.ebay.flow.assetdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.example.session.simplehttpflow.SimpleHttpFlowContext;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.exception.FlowExecutionException;
import org.lightj.session.step.IAroundExecution;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.StepCallbackHandler;
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
import org.lightj.task.asynchttp.SimpleHttpTask;
import org.lightj.task.asynchttp.UrlRequest;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.SpringContextUtil;
import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ning.http.client.AsyncHttpClient;

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
		
		final String taskNameInContext = "assetDiscoverTask";
		final String batchOptionNameInContext = "assetDiscoverBatchOption";
		
		ITaskEventHandler<AssetDiscoveryFlowContext> myHandler = new SimpleTaskEventHandler<AssetDiscoveryFlowContext>() {
			@Override
			public void executeOnResult(AssetDiscoveryFlowContext ctx, Task task, TaskResult result) {
				if (result.getStatus() == TaskResultEnum.Success) {
					ctx.addAgentUuid(result.getDetails().get("host"), result.getDetails().get("uuid"));
				}
				else {
					ctx.addFailedAgentHost(result.getDetails().get("host"));
				}
			}
			@Override
			public TaskResultEnum executeOnCompleted(AssetDiscoveryFlowContext ctx,
					Map<String, TaskResult> results) {
				return super.executeOnCompleted(ctx, results);
			}
			
		};
		StepCallbackHandler callbackHandler = new StepCallbackHandler<AssetDiscoveryFlowContext>("retrieveAssetPayload").setDelegateHandler(myHandler);
		
		IAroundExecution<AssetDiscoveryFlowContext> extrExecution = new IAroundExecution<AssetDiscoveryFlowContext>() {

			@Override
			public void preExecute(AssetDiscoveryFlowContext ctx)
					throws FlowExecutionException {
				HttpTaskRequest taskReq = new HttpTaskRequest();
				UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
				reqTemplate.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
				UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
				taskReq.setAsyncPollTaskOption(reqTemplate, new ExecuteOption(0,0,3,0), pollTemplate, new MonitorOption(0, 5*1000L, 120*1000L, 3, 0), "agentPollProcessor");
				taskReq.setHosts(ctx.getAgentHosts());
				HashMap<String, String> varValues = new HashMap<String, String>();
				Map<String, String> agentParams = ctx.getAgentParams();
				for (String param : ctx.AgentParameters) {
					varValues.put(UrlTemplate.encodeIfNeeded(param), agentParams.get(param));
				}
				taskReq.addTemplateValue(varValues);
				taskReq.setHttpClientType("httpClient");
				taskReq.setGlobalContext("agentAuthKeyContext");
				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
				ctx.addToScrapbook(taskNameInContext, task);
				ctx.addToScrapbook(batchOptionNameInContext, new BatchOption(0, Strategy.UNLIMITED));
			}

			@Override
			public void postExecute(AssetDiscoveryFlowContext ctx)
					throws FlowExecutionException {
			}
	
		};
		
		return new StepBuilder().executeTasksInContext(taskNameInContext, batchOptionNameInContext, extrExecution)
				.onResult(callbackHandler)
				.getFlowStep();
				
	}
	
	@Bean 
	@Scope("prototype")
	public static IFlowStep retrieveAssetPayloadStep() {
		
		final String reqUrl = UrlTemplate.encodeAllVariables("https://host:12020/agent/execoutput/uuid", "host", "uuid");
		
		final String taskNameInContext = "retrieveAssetPayloadTask";
		final String batchOptionNameInContext = "retrieveAssetPayloadBatchOption";
		
		ITaskEventHandler<AssetDiscoveryFlowContext> myHandler = new SimpleTaskEventHandler<AssetDiscoveryFlowContext>() {
			@Override
			public void executeOnResult(AssetDiscoveryFlowContext ctx, Task task, TaskResult result) {
				if (result.getStatus() == TaskResultEnum.Success) {
					Map<String, String> iaasParam = new HashMap<String, String>();
					iaasParam.put("boday", result.<SimpleHttpResponse>getRawResult().getResponseBody());
					ctx.addIaaSParam(iaasParam);
				}
				else {
					ctx.addFailedAgentHost(result.getDetails().get("host"));
				}
			}
			@Override
			public TaskResultEnum executeOnCompleted(AssetDiscoveryFlowContext ctx,
					Map<String, TaskResult> results) {
				return super.executeOnCompleted(ctx, results);
			}
			
		};
		StepCallbackHandler callbackHandler = new StepCallbackHandler<AssetDiscoveryFlowContext>("stop").setDelegateHandler(myHandler);

		return new StepBuilder().executeTasksInContext(
				taskNameInContext, 
				batchOptionNameInContext, 
				new IAroundExecution<AssetDiscoveryFlowContext>() {

					@Override
					public void preExecute(AssetDiscoveryFlowContext ctx)
							throws FlowExecutionException {

						List<ExecutableTask> uuidTasks = new ArrayList<ExecutableTask>();
						AsyncHttpClient client = SpringContextUtil.getBean("resources", "httpClient", AsyncHttpClient.class);
						UrlTemplate reqTemplate = new UrlTemplate(reqUrl);
						reqTemplate.addHeader("content-type", "application/json");
						ExecuteOption executeOption = new ExecuteOption(0,0,3,0);
						
						for (String host : ctx.getAgentHosts()) {
							if (!ctx.getFailedAgentHosts().contains(host)) {
								SimpleHttpTask atask = new SimpleHttpTask(client, executeOption);
								UrlRequest urlReq = new UrlRequest(reqTemplate).setHost(host);
								urlReq.addTemplateValue("uuid", ctx.getAgentUuidMap().get(host));
								atask.setReq(urlReq);
								uuidTasks.add(atask);
							}
						}
						ctx.addToScrapbook(taskNameInContext, uuidTasks);
						ctx.addToScrapbook(batchOptionNameInContext, new BatchOption(0, Strategy.UNLIMITED));
					}

					@Override
					public void postExecute(AssetDiscoveryFlowContext ctx)
							throws FlowExecutionException {
					}
			
		}).onResult(callbackHandler).getFlowStep();
				
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep registerAssetsStep() {
		
		final String reqUrl = UrlTemplate.encodeAllVariables("http://host/onboarding/v1/onboardAsset", "host");
		final String reqBody = UrlTemplate.encodeAllVariables("body", "body");
		
		final String taskNameInContext = "updateCmsTask";
		final String batchOptionNameInContext = "updateCmsBatchOption";
		return new StepBuilder().executeTasksInContext(
				taskNameInContext, 
				batchOptionNameInContext, 
				new IAroundExecution<AssetDiscoveryFlowContext>() {

					@Override
					public void preExecute(AssetDiscoveryFlowContext ctx)
							throws FlowExecutionException {
						HttpTaskRequest taskReq = new HttpTaskRequest();
						UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
						reqTemplate.addHeader("content-type", "application/json");
						taskReq.setSyncTaskOptions(reqTemplate, new ExecuteOption());
						taskReq.setHost(ctx.getIaasHost());
						taskReq.addAllTemplateValues(ctx.getIaasParams());
						taskReq.setHttpClientType("httpClient");
						ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
						ctx.addToScrapbook(taskNameInContext, task);
						ctx.addToScrapbook(batchOptionNameInContext, new BatchOption(20, Strategy.MAX_CONCURRENT_RATE_SLIDING));
					}

					@Override
					public void postExecute(AssetDiscoveryFlowContext ctx)
							throws FlowExecutionException {
					}
			
		}).getFlowStep();
		
	}

}
