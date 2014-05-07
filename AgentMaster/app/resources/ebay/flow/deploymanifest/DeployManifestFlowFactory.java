package resources.ebay.flow.deploymanifest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.SimpleHttpResponse;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import resources.ebay.TaskResourceProvider;
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
					req.setHosts(context.getGoodHosts());
					ExecutableTask task = HttpTaskBuilder.buildTask(req);
					
					final Set<String> hostsNeedService = new HashSet<String>();
					final String serviceName = context.getServiceName();
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
									ctx.addFailedHost(result.getResultDetail("host"));
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

					taskInFlow = new TaskInFlow(null, handler, task);
					
					break;
					
				case 1:
					
					HttpTaskRequest req2 = new HttpTaskRequest();
					UrlTemplate createService = new UrlTemplate(UrlTemplate.encodeAllVariables(
							"https://host:12020/services/serviceName", "host", "serviceName"), HttpMethod.POST, null);
					createService.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
					req2.setSyncTaskOptions("httpClient", createService, new ExecuteOption(0, 0, 2, 0), "agentProcessor");
					req2.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(
							"serviceName", context.getServiceName()));
					req2.setHosts(((Set<String>) context.getFromScrapbook("hostsNeedService")).toArray(new String[0]));
					// set global context
					req2.setGlobalContext("agentAuthKeyContext");
					ExecutableTask task2 = HttpTaskBuilder.buildTask(req2);
					
					ITaskEventHandler handler2 = createHandler();					
					taskInFlow = new TaskInFlow(null, handler2, task2);
					
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
		
		final String reqUrl = UrlTemplate.encodeAllVariables(
				"https://host:12020/services/serviceName/manifests/manifestName", 
				"host", "serviceName", "manifestName");
		
		final String reqBody = UrlTemplate.encodeAllVariables("body", "body");
		
		final String pollUrl = UrlTemplate.encodeAllVariables(
				"https://host:12020/status/uuid", "host", "uuid");
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<DeployManifestFlowContext>() {

			@Override
			public TaskInFlow<DeployManifestFlowContext> createTaskInFlow(
					DeployManifestFlowContext context, int sequence) 
			{
				try {
					// create http task req
					HttpTaskRequest taskReq = new HttpTaskRequest();
					// set async poll req
					UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
					reqTemplate.addHeader("content-type", "application/json")
							.addHeader("Authorization", "Basic <agentAuthKey>");
					UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
					taskReq.setAsyncPollTaskOption("httpClient", 
							reqTemplate, 
							new ExecuteOption(0,0,3,0), 
							pollTemplate, 
							new MonitorOption(0, 5, 120, 3, 0), 
							TaskResourceProvider.AGENT_POLL_PROCESSOR);
					
					// host template
					taskReq.setHosts(context.getGoodHosts());
					// set template values
					HashMap<String, String[]> pkgNames = new HashMap<String, String[]>();
					pkgNames.put("package", context.getManifestPkgs().toArray(new String[0]));
					taskReq.setTemplateValuesForAllHosts(
							new HostTemplateValues()
								.addNewTemplateValue("serviceName", context.getServiceName())
								.addToCurrentTemplate("manifestName", context.getManifestName())
								.addToCurrentTemplate("body", JsonUtil.encode(pkgNames)));
					// set global context
					taskReq.setGlobalContext(TaskResourceProvider.AGENT_AUTHKEY_BEAN);
					// build real task
					ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
					return new TaskInFlow<DeployManifestFlowContext>(new BatchOption(0, Strategy.UNLIMITED), createHandler(), task);
				} 
				catch (Throwable t) {
					throw new FlowExecutionException(t);
				}
			}
			
		}).getFlowStep();
		
	}

	@Bean 
	@Scope("prototype")
	public static IFlowStep activateManifestStep() {
		
		final String reqUrl = UrlTemplate.encodeAllVariables(
				"https://host:12020/services/serviceName/action/activatemanifest", 
				"host", "serviceName");
		
		final String reqBody = UrlTemplate.encodeAllVariables("body", "body");
		
		final String pollUrl = UrlTemplate.encodeAllVariables(
				"https://host:12020/status/uuid", "host", "uuid");
		
		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<DeployManifestFlowContext>() {

			@Override
			public TaskInFlow<DeployManifestFlowContext> createTaskInFlow(
					DeployManifestFlowContext context, int sequence) 
			{
				try {
					// create http task req
					HttpTaskRequest taskReq = new HttpTaskRequest();
					// set async poll req
					UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.POST, reqBody);
					reqTemplate.addHeader("content-type", "application/json")
							.addHeader("Authorization", "Basic <agentAuthKey>");
					UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
					taskReq.setAsyncPollTaskOption(
							"httpClient", 
							reqTemplate, 
							new ExecuteOption(0,0,3,0), 
							pollTemplate, 
							new MonitorOption(0, 5, 120, 3, 0), 
							TaskResourceProvider.AGENT_POLL_PROCESSOR);
					taskReq.setHosts(context.getGoodHosts());
					// set template values
					HashMap<String, String> manifest = new HashMap<String, String>();
					manifest.put("manifest", context.getManifestName());
					taskReq.setTemplateValuesForAllHosts(
							new HostTemplateValues()
								.addNewTemplateValue("serviceName", context.getServiceName())
								.addToCurrentTemplate("body", JsonUtil.encode(manifest)));
					// set global context
					taskReq.setGlobalContext(TaskResourceProvider.AGENT_AUTHKEY_BEAN);
					// build real task
					ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
					
					return new TaskInFlow<DeployManifestFlowContext>(new BatchOption(0, Strategy.UNLIMITED), createHandler(), task);
					
				} 
				catch (Throwable t) {
					throw new FlowExecutionException(t);
				}
			}
			
		}).getFlowStep();
		
	}
	
	/**
	 * common handler
	 * @return
	 */
	private static ITaskEventHandler<DeployManifestFlowContext> createHandler() {
		return new SimpleTaskEventHandler<DeployManifestFlowContext>() {
			@Override
			public void executeOnResult(DeployManifestFlowContext ctx, Task task, TaskResult result) {
				try {
					if (result.getStatus() == TaskResultEnum.Success) {
						AgentStatus agentStatus = JsonUtil.decode(result.<SimpleHttpResponse>getRawResult().getResponseBody(), AgentStatus.class);
						if (agentStatus.progress != 100) {
							ctx.addFailedHost(result.getResultDetail("host"));
						}
					}
					else {
						ctx.addFailedHost(result.getResultDetail("host"));
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
	}

}
