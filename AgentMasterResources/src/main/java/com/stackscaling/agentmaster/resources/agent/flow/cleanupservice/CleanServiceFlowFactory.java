package com.stackscaling.agentmaster.resources.agent.flow.cleanupservice;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.lightj.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.stackscaling.agentmaster.resources.agent.AgentResourceProvider.AgentStatus;


@SuppressWarnings("rawtypes")
@Configuration
public class CleanServiceFlowFactory {

	public @Bean @Scope("prototype") static CleanServiceFlow cleanServiceFlow() {
		return new CleanServiceFlow();
	}

	@Bean
	@Scope("prototype")
	public static IFlowStep deactivateIfNeededStep() {

		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<CleanServiceFlowContext>() {

			@SuppressWarnings("unchecked")
			@Override
			public TaskInFlow<CleanServiceFlowContext> createTaskInFlow(
					final CleanServiceFlowContext context, int sequence) {

				TaskInFlow<CleanServiceFlowContext> taskInFlow = null;

				switch(sequence) {
				case 0:

					// check service have active manifest
					HttpTaskRequest req = new HttpTaskRequest();
					UrlTemplate checkService = new UrlTemplate(UrlTemplate.encodeAllVariables("https://host:19000/services/serviceName", "host", "serviceName"));
					checkService.addHeader("content-type", "application/json");
					req.setSyncTaskOptions("httpClient", checkService, new ExecuteOption(0, 0, 2, 0), "agentProcessor");
					req.setHosts(context.getGoodHosts());
					req.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(
							"serviceName", context.getServiceName()));
					ExecutableTask task = HttpTaskBuilder.buildTask(req);

					// populate hosts that need deactivation
					final Set<String> hostsNeedDeactivate = new HashSet<String>();
					ITaskEventHandler handler = new SimpleTaskEventHandler<CleanServiceFlowContext>() {
						@Override
						public void executeOnResult(CleanServiceFlowContext ctx, Task task, TaskResult result) {
							try {
								if (result.getStatus() == TaskResultEnum.Success) {
									String response = result.<SimpleHttpResponse>getRawResult().getResponseBody();
									AgentStatus agentStatus = JsonUtil.decode(response, AgentStatus.class);
									if (agentStatus.result != null && agentStatus.result instanceof Map) {
										Object activeManifest = ((Map) agentStatus.result).get("activemanifest");
										if (activeManifest != null) {
											hostsNeedDeactivate.add(result.getResultDetail("host"));
										}
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
						public TaskResultEnum executeOnCompleted(CleanServiceFlowContext ctx,
								Map<String, TaskResult> results) {
							context.addToScrapbook("hostNeedDeactivate", hostsNeedDeactivate);
							return hostsNeedDeactivate.isEmpty() ? TaskResultEnum.Success : TaskResultEnum.Running;
						}

					};

					taskInFlow = new TaskInFlow<CleanServiceFlowContext>(null, handler, task);

					break;
				case 1:

					// deactivate manifest
					HttpTaskRequest req2 = new HttpTaskRequest();
					UrlTemplate deactivateReq = new UrlTemplate(UrlTemplate.encodeAllVariables(
							"https://host:19000/services/serviceName/action/deactivatemanifest", "host", "serviceName"), HttpMethod.POST);
					deactivateReq.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
					UrlTemplate pollReq = new UrlTemplate(UrlTemplate.encodeAllVariables("https://host:19000/status/uuid", "host", "uuid"));
					req2.setAsyncPollTaskOption("httpClient", deactivateReq, new ExecuteOption(0, 0, 3, 0), pollReq, new MonitorOption(0, 5, 120, 3, 0), "agentPollProcessor");
					req2.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(
							"serviceName", context.getServiceName()));
					req2.setHosts(((HashSet<String>)context.getFromScrapbook("hostNeedDeactivate")).toArray(new String[0]));
					// set global context
					req2.setGlobalContext("agentAuthKeyContext");
					ExecutableTask task2 = HttpTaskBuilder.buildTask(req2);

					ITaskEventHandler handler2 = new SimpleTaskEventHandler<CleanServiceFlowContext>() {
						@Override
						public void executeOnResult(CleanServiceFlowContext ctx, Task task, TaskResult result) {
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
						public TaskResultEnum executeOnCompleted(CleanServiceFlowContext ctx,
								Map<String, TaskResult> results) {
							return TaskResultEnum.Success;
						}
					};

					taskInFlow = new TaskInFlow<CleanServiceFlowContext>(null, handler2, task2);

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
	public static IFlowStep cleanServiceStep() {

		final ITaskEventHandler<CleanServiceFlowContext> myHandler = new SimpleTaskEventHandler<CleanServiceFlowContext>() {
			@Override
			public void executeOnResult(CleanServiceFlowContext ctx, Task task, TaskResult result) {
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
		};

		final String reqUrl = UrlTemplate.encodeAllVariables("https://host:19000/services/serviceName", "host", "serviceName");
		final String pollUrl = UrlTemplate.encodeAllVariables("https://host:19000/status/uuid", "host", "uuid");

		return new StepBuilder().executeTasks(new IFlowContextTaskFactory<CleanServiceFlowContext>() {

			@Override
			public TaskInFlow<CleanServiceFlowContext> createTaskInFlow(
					CleanServiceFlowContext context, int sequence)
			{
				HttpTaskRequest taskReq = new HttpTaskRequest();
				// set async poll req
				UrlTemplate reqTemplate = new UrlTemplate(reqUrl, HttpMethod.DELETE);
				reqTemplate.addHeader("content-type", "application/json").addHeader("Authorization", "Basic <agentAuthKey>");
				UrlTemplate pollTemplate = new UrlTemplate(pollUrl);
				taskReq.setAsyncPollTaskOption("httpClient", reqTemplate, new ExecuteOption(0,0,3,0), pollTemplate, new MonitorOption(0, 5, 120, 3, 0), "agentPollProcessor");
				// host template
				taskReq.setHosts(context.getGoodHosts());
				// set template values
				taskReq.setTemplateValuesForAllHosts(new HostTemplateValues()
							.addNewTemplateValue("serviceName", context.getServiceName()));
				// set global context
				taskReq.setGlobalContext("agentAuthKeyContext");
				// build real task
				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);

				return new TaskInFlow<CleanServiceFlowContext>(new BatchOption(0, Strategy.UNLIMITED), myHandler, task);
			}

		}).getFlowStep();

	}

}
