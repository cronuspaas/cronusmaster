package resources;

import java.io.IOException;
import java.util.Map;

import models.utils.LogUtils;

import org.lightj.session.FlowContext;
import org.lightj.task.ITaskEventHandler;
import org.lightj.task.SimpleTaskEventHandler;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.IHttpPollProcessor;
import org.lightj.task.asynchttp.SimpleHttpResponse;
import org.lightj.task.asynchttp.SimpleHttpTask;
import org.lightj.task.asynchttp.UrlRequest;
import org.lightj.util.SpringContextUtil;
import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import resources.IUserDataDao.DataType;
import resources.log.IJobLogger;
import resources.log.JobLog;
import resources.log.JobLog.CommandResponse;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfigBean;
import com.ning.http.client.Response;

@Configuration
public class TaskResourcesProvider {
	
	public static final class LogTaskEventHandler extends SimpleTaskEventHandler<FlowContext> {
		
		private final JobLog jobLog;
		private final DataType logType;
		public LogTaskEventHandler(DataType logType, JobLog jobLog) {
			this.jobLog = jobLog;
			this.logType = logType;
		}

		@Override
		public void executeOnResult(FlowContext ctx, Task task,
				TaskResult result) {
			if (task instanceof SimpleHttpTask) {
				String host = ((SimpleHttpTask) task).getReq().getHost();
				if (result.getStatus() == TaskResultEnum.Success) {
					SimpleHttpResponse res = result.<SimpleHttpResponse>getRealResult();
					jobLog.addCommandResponse(new CommandResponse(host, res.getStatusCode(), res.getResponseBody())); 
				}
				else {
					jobLog.addCommandResponse(new CommandResponse(host, 0, String.format("%s - %s", result.getMsg(), StringUtil.getStackTrace(result.getStackTrace()))));
				}
			}
		}

		@Override
		public TaskResultEnum executeOnCompleted(FlowContext ctx,
				Map<String, TaskResult> results) {
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(logType);
			try {
				logger.saveLog(jobLog);
			} catch (IOException e) {
				LogUtils.printLogError(e.getMessage());
			}
			return super.executeOnCompleted(ctx, results);
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
