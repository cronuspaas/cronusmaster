package resources.ebay.flow.deploymanifest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.exception.FlowExecutionException;
import org.lightj.session.step.IAroundExecution;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepBuilder;
import org.lightj.session.step.StepCallbackHandler;
import org.lightj.session.step.TaskFactoryStepExecution.IFlowContextTaskFactory;
import org.lightj.session.step.TaskFactoryStepExecution.TaskInFlow;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.ITaskEventHandler;
import org.lightj.task.MonitorOption;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.Task;
import org.lightj.task.TaskExecutionException;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.SimpleHttpResponse;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.ConcurrentUtil;
import org.lightj.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import resources.ebay.TaskResourceProvider.AgentStatus;

@SuppressWarnings("rawtypes")
@Configuration
public class DeployManifestFlowFactory {
	
	public @Bean @Scope("prototype") static DeployManifestFlow deployManifestFlow() {
		return new DeployManifestFlow();
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep createServiceIfNeededStep() {
		
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<DeployManifestFlowContext>() {

			@Override
			public TaskInFlow<DeployManifestFlowContext> createTaskInFlow(
					final DeployManifestFlowContext context, int sequence) {
				TaskInFlow taskInFlow = null;
				switch(sequence) {
				case 0:
					// check service exist
					HttpTaskRequest req = new HttpTaskRequest();
					UrlTemplate checkService = new UrlTemplate(UrlTemplate.encodeAllVariables("https://host:12020/services", "host"));
					checkService.addHeader("content-type", "application/json");
					req.setSyncTaskOptions("httpClient", checkService, new ExecuteOption(0, 0, 2, 0), "agentProcessor");
					req.setHosts(context.getUserInputs().agentHosts);
					ExecutableTask task = HttpTaskBuilder.buildTask(req);
					
					final Set<String> hostsNeedService = new HashSet<String>();
					final String serviceName = context.getUserInputs().serviceName;
					ITaskEventHandler handler = new SimpleTaskEventHandler<DeployManifestFlowContext>() {
						@Override
						public void executeOnResult(DeployManifestFlowContext ctx, Task task, TaskResult result) {
							try {
								if (result.getStatus() == TaskResultEnum.Success) {
									String response = result.<SimpleHttpResponse>getRawResult().getResponseBody();
									if (response.indexOf(serviceName) < 0) {
										hostsNeedService.add(result.getResultDetail("host"));
									}
								}
								else {
									ctx.addFailedAgentHost(result.getResultDetail("host"));
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
						@Override
						public TaskResultEnum executeOnCompleted(DeployManifestFlowContext ctx,
								Map<String, TaskResult> results) {
							context.addToScrapbook("hostsNeedService", hostsNeedService);
							return hostsNeedService.isEmpty() ? TaskResultEnum.Success : TaskResultEnum.Running;
						}
						
					};

					taskInFlow = new TaskInFlow(task, null, handler);
					
					break;
					
				case 1:
					
					HttpTaskRequest req2 = new HttpTaskRequest();
					UrlTemplate createService = new UrlTemplate(UrlTemplate.encodeAllVariables(
							"https://host:12020/services/serviceName", "host", "serviceName"), HttpMethod.POST, null);
					createService.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
					req2.setSyncTaskOptions("httpClient", createService, new ExecuteOption(0, 0, 2, 0), "agentProcessor");
					req2.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(
							"serviceName", context.getUserInputs().serviceName));
					req2.setHosts(((Set<String>) context.getFromScrapbook("hostsNeedService")).toArray(new String[0]));
					// set global context
					req2.setGlobalContext("agentAuthKeyContext");
					ExecutableTask task2 = HttpTaskBuilder.buildTask(req2);
					
					ITaskEventHandler handler2 = new SimpleTaskEventHandler<DeployManifestFlowContext>() {
						@Override
						public void executeOnResult(DeployManifestFlowContext ctx, Task task, TaskResult result) {
							try {
								if (result.getStatus() == TaskResultEnum.Success) {
									AgentStatus agentStatus = JsonUtil.decode(result.<SimpleHttpResponse>getRawResult().getResponseBody(), AgentStatus.class);
									if (agentStatus.progress != 100) {
										ctx.addFailedAgentHost(result.getResultDetail("host"));
									}
								}
								else {
									ctx.addFailedAgentHost(result.getResultDetail("host"));
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
						@Override
						public TaskResultEnum executeOnCompleted(DeployManifestFlowContext ctx,
								Map<String, TaskResult> results) {
							return TaskResultEnum.Success;
						}
					};
					
					taskInFlow = new TaskInFlow(task2, null, handler2);
					
					break;
				default:
					break;
				}
				return taskInFlow;
			}
			
		}).getFlowStep();
		
	}
	
	@Bean 
	@Scope("prototype")
	public static IFlowStep createManifestStep() {
		
		ITaskEventHandler<DeployManifestFlowContext> myHandler = new SimpleTaskEventHandler<DeployManifestFlowContext>() {
			@Override
			public void executeOnResult(DeployManifestFlowContext ctx, Task task, TaskResult result) {
				try {
					if (result.getStatus() == TaskResultEnum.Success) {
						AgentStatus agentStatus = JsonUtil.decode(result.<SimpleHttpResponse>getRawResult().getResponseBody(), AgentStatus.class);
						if (agentStatus.progress != 100) {
							ctx.addFailedAgentHost(result.getResultDetail("host"));
						}
					}
					else {
						ctx.addFailedAgentHost(result.getResultDetail("host"));
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		StepCallbackHandler callbackHandler = new StepCallbackHandler<DeployManifestFlowContext>("activateManifest").setDelegateHandler(myHandler);

		final String reqUrl = UrlTemplate.encodeAllVariables("https://host:12020/services/serviceName/manifests/manifestName", 
				"host", "serviceName", "manifestName");
		final String reqBody = UrlTemplate.encodeAllVariables("body", "body");
		final String pollUrl = UrlTemplate.encodeAllVariables("https://host:12020/status/uuid", "host", "uuid");
		final String taskNameInContext = "createManifestTask";
		final String batchOptionNameInContext = "createManifestTaskBatchOption";
		
		return new StepBuilder().executeTasksInContext(
				taskNameInContext, 
				batchOptionNameInContext, 
				new IAroundExecution<DeployManifestFlowContext>() 
		{

					@Override
					public void preExecute(DeployManifestFlowContext ctx)
							throws FlowExecutionException 
					{
						try {
							// create http task req
							HttpTaskRequest taskReq = new HttpTaskRequest();
							// set async poll req
							UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
							reqTemplate.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
							UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
							taskReq.setAsyncPollTaskOption("httpClient", reqTemplate, new ExecuteOption(0,0,3,0), pollTemplate, new MonitorOption(0, 5*1000L, 120*1000L, 3, 0), "agentPollProcessor");
							// host template
							taskReq.setHosts(ctx.getGoodHosts());
							// set template values
							HashMap<String, String[]> pkgNames = new HashMap<String, String[]>();
							pkgNames.put("package", ctx.getUserInputs().manifestPkgs);
							taskReq.setTemplateValuesForAllHosts(new HostTemplateValues()
										.addNewTemplateValue("serviceName", ctx.getUserInputs().serviceName)
										.addToCurrentTemplate("manifestName", ctx.getUserInputs().manifestName)
										.addToCurrentTemplate("body", JsonUtil.encode(pkgNames)));
							// set global context
							taskReq.setGlobalContext("agentAuthKeyContext");
							// build real task
							ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
							// add task to session context to be run
							ctx.addToScrapbook(taskNameInContext, task);
							ctx.addToScrapbook(batchOptionNameInContext, new BatchOption(0, Strategy.UNLIMITED));
						} catch (Exception e) {
							throw new FlowExecutionException(e);
						}
					}

					@Override
					public void postExecute(DeployManifestFlowContext ctx)
							throws FlowExecutionException {
					}
			
		}).onResult(callbackHandler).getFlowStep();
				
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep activateManifestStep() {
		
		ITaskEventHandler<DeployManifestFlowContext> myHandler = new SimpleTaskEventHandler<DeployManifestFlowContext>() {
			@Override
			public void executeOnResult(DeployManifestFlowContext ctx, Task task, TaskResult result) {
				try {
					if (result.getStatus() == TaskResultEnum.Success) {
						AgentStatus agentStatus = JsonUtil.decode(result.<SimpleHttpResponse>getRawResult().getResponseBody(), AgentStatus.class);
						if (agentStatus.progress != 100) {
							ctx.addFailedAgentHost(result.getResultDetail("host"));
						}
					}
					else {
						ctx.addFailedAgentHost(result.getResultDetail("host"));
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		StepCallbackHandler callbackHandler = new StepCallbackHandler<DeployManifestFlowContext>("stop").setDelegateHandler(myHandler);

		final String reqUrl = UrlTemplate.encodeAllVariables("https://host:12020/services/serviceName/action/activatemanifest", "host", "serviceName");
		final String reqBody = UrlTemplate.encodeAllVariables("body", "body");
		final String pollUrl = UrlTemplate.encodeAllVariables("https://host:12020/status/uuid", "host", "uuid");
		final String taskNameInContext = "activateManifestTask";
		final String batchOptionNameInContext = "activateManifestTaskBatchOption";
		
		return new StepBuilder().executeTasksInContext(
				taskNameInContext, 
				batchOptionNameInContext, 
				new IAroundExecution<DeployManifestFlowContext>() 
		{

					@Override
					public void preExecute(DeployManifestFlowContext ctx)
							throws FlowExecutionException 
					{
						try {
							// create http task req
							HttpTaskRequest taskReq = new HttpTaskRequest();
							// set async poll req
							UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
							reqTemplate.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
							UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
							taskReq.setAsyncPollTaskOption("httpClient", reqTemplate, new ExecuteOption(0,0,3,0), pollTemplate, new MonitorOption(0, 5*1000L, 120*1000L, 3, 0), "agentPollProcessor");
							taskReq.setHosts(ctx.getGoodHosts());
							// set template values
							HashMap<String, String> manifest = new HashMap<String, String>();
							manifest.put("manifest", ctx.getUserInputs().manifestName);
							taskReq.setTemplateValuesForAllHosts(new HostTemplateValues()
										.addNewTemplateValue("serviceName", ctx.getUserInputs().serviceName)
										.addToCurrentTemplate("body", JsonUtil.encode(manifest)));
							// set global context
							taskReq.setGlobalContext("agentAuthKeyContext");
							// build real task
							ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
							// add task to session context to be run
							ctx.addToScrapbook(taskNameInContext, task);
							ctx.addToScrapbook(batchOptionNameInContext, new BatchOption(0, Strategy.UNLIMITED));
						} catch (Exception e) {
							throw new FlowExecutionException(e);
						}
					}

					@Override
					public void postExecute(DeployManifestFlowContext ctx)
							throws FlowExecutionException {
					}
			
		}).onResult(callbackHandler).getFlowStep();
				
	}

}
