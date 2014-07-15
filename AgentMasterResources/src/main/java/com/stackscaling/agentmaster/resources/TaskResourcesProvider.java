package com.stackscaling.agentmaster.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lightj.session.FlowContext;
import org.lightj.session.FlowEvent;
import org.lightj.session.FlowSession;
import org.lightj.session.IFlowEventListener;
import org.lightj.session.step.IFlowStep;
import org.lightj.session.step.StepTransition;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.IHttpPollProcessor;
import org.lightj.task.asynchttp.SimpleHttpResponse;
import org.lightj.task.asynchttp.SimpleHttpTask;
import org.lightj.task.asynchttp.UrlRequest;
import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfigBean;
import com.ning.http.client.Response;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.log.BaseLog;
import com.stackscaling.agentmaster.resources.log.BaseLog.CommandResponse;
import com.stackscaling.agentmaster.resources.log.FlowLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;
import com.stackscaling.agentmaster.resources.log.ILog;
import com.stackscaling.agentmaster.resources.utils.ElasticSearchUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

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
	public static class LogFlowEventListener implements IFlowEventListener {

		FlowLog flowLog;
		public LogFlowEventListener(FlowLog flowLog) {
			this.flowLog = flowLog;
		}

		@Override
		public void handleStepEvent(FlowEvent event, FlowSession session,
				IFlowStep flowStep, StepTransition stepTransition) {
		}

		@Override
		public void handleFlowEvent(FlowEvent event, FlowSession session,
				String msg) {
			if (event == FlowEvent.stop) {
				flowLog.getUserWorkflow().jobInfo = session.getFlowInfo();
				try {
					UserDataProvider.getJobLoggerOfType(DataType.FLOWLOG).saveLog(flowLog);
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
	 * run a task and handle task event, save result in memory for other to access
	 *
	 * @author binyu
	 *
	 */
	public static final class LogTaskEventUpdater extends SimpleTaskEventHandler<FlowContext> {

		private ILog log;
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
	public static final class LogTaskEventHandler extends SimpleTaskEventHandler<FlowContext> {

		private final BaseLog jobLog;
		private final IJobLogger logger;
		private final int hostProgInc;
		public LogTaskEventHandler(DataType logType, BaseLog jobLog, int hostProgInc) {
			this.jobLog = jobLog;
			this.logger = UserDataProvider.getJobLoggerOfType(logType);
			this.hostProgInc = hostProgInc;
		}

		@Override
		public void executeOnResult(FlowContext ctx, Task task,
				TaskResult result) {
			if (task instanceof SimpleHttpTask) {
				String host = ((SimpleHttpTask) task).getReq().getHost();
				jobLog.incProgress(hostProgInc);
				if (result.getStatus() == TaskResultEnum.Success) {
					SimpleHttpResponse res = result.<SimpleHttpResponse>getRawResult();
					jobLog.addCommandResponse(new CommandResponse(host, result.getStatus().name(), res.getStatusCode(), res.getResponseBody()));
				}
				else {
					jobLog.addCommandResponse(new CommandResponse(host, result.getStatus().name(), -1, String.format("%s - %s", result.getMsg(), StringUtil.getStackTrace(result.getStackTrace()))));
				}
				saveLog(false);
			}
		}

		@Override
		public TaskResultEnum executeOnCompleted(FlowContext ctx, Map<String, TaskResult> results)
		{
			TaskResultEnum status = TaskResultEnum.Success;
			int suc=0, fail=0, other=0;
			for (TaskResult res : results.values()) {
				if (res.getStatus().getSeverity() > status.getSeverity()) {
					status = res.getStatus();
				}
				switch (res.getStatus()) {
				case Success:
					suc++;
					break;
				case Failed:
					fail++;
					break;
				default:
					other++;
					break;
				}
			}
			jobLog.setStatus(status.name());
			jobLog.setStatusDetail(suc, fail, other);
			jobLog.setProgress(jobLog.ProgressTotalUnit);
			saveLog(true);
			return super.executeOnCompleted(ctx, results);
		}

		/**
		 * save log
		 * @param isResult
		 */
		private void saveLog(boolean isResult) {
			if (isResult || VarUtils.LOG_PROGRESS_ENABLED) {
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

}