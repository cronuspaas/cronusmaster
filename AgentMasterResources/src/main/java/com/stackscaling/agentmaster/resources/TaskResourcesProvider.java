package com.stackscaling.agentmaster.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.session.FlowContext;
import org.lightj.session.FlowEvent;
import org.lightj.session.FlowSession;
import org.lightj.session.IFlowEventListener;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepTransition;
import org.lightj.task.BatchOption;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.IHttpPollProcessor;
import org.lightj.task.asynchttp.SimpleHttpResponse;
import org.lightj.task.asynchttp.SimpleHttpTask;
import org.lightj.task.asynchttp.UrlRequest;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.ConcurrentUtil;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfigBean;
import com.ning.http.client.Response;
import com.stackscaling.agentmaster.resources.agent.AgentResourceProvider;
import com.stackscaling.agentmaster.resources.log.BaseLog;
import com.stackscaling.agentmaster.resources.log.BaseLog.CommandResponse;
import com.stackscaling.agentmaster.resources.log.FlowLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;
import com.stackscaling.agentmaster.resources.log.ILog;
import com.stackscaling.agentmaster.resources.utils.ElasticSearchUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * Spring resources related to running tasks 
 * 
 * @author binyu
 *
 */
@Configuration
public class TaskResourcesProvider {

	static Logger LOG = LoggerFactory.getLogger(TaskResourcesProvider.class);

	public static String HTTP_CLIENT = "httpClient";

	/**
	 * log flow execution log at flow stop, and save in FlowLog
	 *
	 * @author biyu
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static class LogFlowEventListener implements IFlowEventListener {

		FlowLog flowLog;
		public LogFlowEventListener(FlowLog flowLog) {
			this.flowLog = flowLog;
		}

		@Override
		public void handleStepEvent(FlowEvent event, FlowSession session,
				IFlowStep flowStep, StepTransition stepTransition) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleFlowEvent(FlowEvent event, FlowSession session,
				String msg) {
			if (event == FlowEvent.stop) {
				flowLog.getUserWorkflow().jobInfo = session.getFlowInfo();
				try {
					UserDataProviderFactory.getJobLoggerOfType(DataType.FLOWLOG).saveLog(flowLog);
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
		}

		@Override
		public void handleError(Throwable t, FlowSession session) {
		}

	}

	/**
	 * blocking till all http responses are collected in host-http response map
	 * 
	 * @author binyu
	 *
	 */
	public static final class BlockingTaskResultCollector<R> 
		extends SimpleTaskEventHandler<FlowContext> 
	{

		private final Map<String, R> results;
		private final Map<String, String> failures;
		private ReentrantLock lock;
		private Condition cond;
		private Class<R> resBeanKlass;
		
		public BlockingTaskResultCollector(
				ReentrantLock lock, 
				Condition cond, 
				Class<R> resBeanKlass) 
		{
			this.results = new HashMap<String, R>();
			this.failures = new HashMap<String, String>();
			this.lock = lock;
			this.cond = cond;
			this.resBeanKlass = resBeanKlass;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void executeOnResult(FlowContext ctx, Task task, TaskResult result) {
			if (task instanceof SimpleHttpTask) {
				String host = ((SimpleHttpTask) task).getReq().getHost();
				if (result.getStatus() == TaskResultEnum.Success) {
					SimpleHttpResponse res = result.<SimpleHttpResponse>getRawResult();
					try {
						if (resBeanKlass != String.class) {
							R resBean = JsonUtil.decode(res.getResponseBody(), resBeanKlass);
							results.put(host, resBean);
						} else {
							results.put(host, ((R)res.getResponseBody()));
						}
					} catch (IOException e) {
						failures.put(host, e.getLocalizedMessage());
					}
				} else {
					failures.put(host, result.getMsg());
				}
			}
		}
		
		@Override
		public TaskResultEnum executeOnCompleted(FlowContext ctx, 
				Map<String, TaskResult> results)
		{
			ConcurrentUtil.signalAll(lock, cond);
			return super.executeOnCompleted(ctx, results);
		}
		
		public Map<String, R> getResults() {
			return results;
		}
		
		public Map<String, String> getFailures() {
			return failures;
		}

	}
	
	/**
	 * run task to fetch raw log for a task and add it to elastic search index
	 *
	 * @author binyu
	 *
	 */
	public static final class LogTaskEventUpdater extends SimpleTaskEventHandler<FlowContext> {

		private final ILog log;
		public LogTaskEventUpdater(ILog log) {
			this.log = log;
		}

		@Override
		public void executeOnResult(FlowContext ctx, Task task, TaskResult result) {
			if (task instanceof SimpleHttpTask) {
				String host = ((SimpleHttpTask) task).getReq().getHost();
				if (result.getStatus() == TaskResultEnum.Success) {
					SimpleHttpResponse res = result.<SimpleHttpResponse>getRawResult();
					HashMap<String, String> values = new HashMap<String, String>();
					values.put("rawScriptLogs", res.getResponseBody());
					String id = String.format("%s~%s", log.uuid(), host);
					LOG.info(String.format("%s - %s - %s", log.getClass().getSimpleName(), id, values));
					ElasticSearchUtils.updateDocument("log", log.getClass().getSimpleName(), id, values);
				}
			}
		}
	}

	/**
	 * handle task execution event by task runner, and save log in CmdLog or JobJog
	 *
	 * @author binyu
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static final class LogTaskEventHandler extends SimpleTaskEventHandler<FlowContext> {

		private final BaseLog jobLog;
		private final IJobLogger logger;
		private int suc=0, fail=0, other=0;

		public LogTaskEventHandler(BaseLog jobLog, int numOfHosts) {
			this.jobLog = jobLog;
			this.logger = UserDataProviderFactory.getJobLoggerOfType(jobLog.getLogType());
			this.other = numOfHosts;
		}

		@Override
		public void executeOnResult(FlowContext ctx, Task task,
				TaskResult result) {
			if (task instanceof SimpleHttpTask) {
				String host = ((SimpleHttpTask) task).getReq().getHost();
				if (result.getStatus() == TaskResultEnum.Success) {
					SimpleHttpResponse res = result.<SimpleHttpResponse>getRawResult();
					jobLog.addCommandResponse(
							new CommandResponse(host, 
									result.getStatus().name(), 
									res.getStatusCode(), 
									res.getResponseBody()));
				}
				else {
					jobLog.addCommandResponse(
							new CommandResponse(host, 
									result.getStatus().name(), 
									-1, 
									String.format("%s - %s", result.getMsg(), StringUtil.getStackTrace(result.getStackTrace()))));
				}
				switch (result.getStatus()) {
				case Success:
					suc++;
					other--;
					break;
				default:
					fail++;
					other--;
					break;
				}
				saveLog(false);
			}
		}

		@Override
		public TaskResultEnum executeOnCompleted(FlowContext ctx, Map<String, TaskResult> results)
		{
			jobLog.setStatus(fail==0 ? TaskResultEnum.Success.name() : TaskResultEnum.Failed.name());
			
			// if job has raw log, fetch logs and add to elastic search index
			if (jobLog.isHasRawLogs()) {
				HttpTaskRequest taskReq = new HttpTaskRequest();
				UrlTemplate urlTemplate = new UrlTemplate("https://<host>:19000/status/guidoutput/<guid>", HttpMethod.GET);
				taskReq.setSyncTaskOptions(TaskResourcesProvider.HTTP_CLIENT, urlTemplate, new ExecuteOption(), AgentResourceProvider.AGENT_PROCESSOR);
				taskReq.setHosts(jobLog.getNodeGroup().getHosts());
				taskReq.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue("guid", jobLog.uuid()));

				ExecutableTask task = HttpTaskBuilder.buildTask(taskReq);
				StandaloneTaskListener listener = new StandaloneTaskListener();
				listener.setDelegateHandler(new TaskResourcesProvider.LogTaskEventUpdater(jobLog));
				
				new StandaloneTaskExecutor(new BatchOption(), listener, task).execute();
			}
			jobLog.setRawLogsFetched(true);

			saveLog(true);

			return super.executeOnCompleted(ctx, results);
		}

		/**
		 * save log
		 * @param isResult
		 */
		@SuppressWarnings("unchecked")
		public void saveLog(boolean isResult) {
			jobLog.setStatusDetail(suc, fail, other);
			if (isResult || VarUtils.isLogProgEnabled) {
				try {
					logger.saveLog(jobLog);
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * dummy poll processor
	 * @return
	 */
	public @Bean @Scope("singleton") IHttpPollProcessor dummyPollProcessor() {

		return new IHttpPollProcessor() {

			@Override
			public TaskResult checkPollProgress(Task task, Response response) {
				return task.succeeded();
			}

			@Override
			public TaskResult preparePollTask(Task task, Response response,
					UrlRequest pollReq) {
				return task.succeeded();
			}

		};
	}

	/**
	 * ning http client
	 * @return
	 */
	public @Bean @Scope("singleton") AsyncHttpClient httpClient() {
		// create and configure async http client
		AsyncHttpClientConfigBean config = new AsyncHttpClientConfigBean();
		config.setConnectionTimeOutInMs(3000);
		return new AsyncHttpClient(config);
	}
	
	/**
	 * ning http client for proxy
	 * @return
	 */
	public @Bean @Scope("singleton") AsyncHttpClient proxyHttpClient() {
		// create and configure async http client for proxy
		AsyncHttpClientConfigBean config = new AsyncHttpClientConfigBean();
		config.setConnectionTimeOutInMs(3000);
		config.setUseRawUrl(true);
		return new AsyncHttpClient(config);
	}

}
