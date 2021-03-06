package com.stackscaling.agentmaster.resources.agent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.example.task.HttpTaskRequest.TaskType;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.task.ExecuteOption;
import org.lightj.task.IGlobalContext;
import org.lightj.task.MonitorOption;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.IHttpPollProcessor;
import org.lightj.task.asynchttp.IHttpProcessor;
import org.lightj.task.asynchttp.SimpleHttpTask;
import org.lightj.task.asynchttp.UrlRequest;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ning.http.client.Response;
import com.stackscaling.agentmaster.resources.command.ICommandEnhancer;
import com.stackscaling.agentmaster.resources.security.SecurityUtil;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

@Configuration
public class AgentResourceProvider {

	public static final String AGENT_PROCESSOR = "agentProcessor";
	public static final String AGENT_POLL_PROCESSOR = "agentPollProcessor";
	public static final String AGENT_AUTHKEY_BEAN = "agentAuthKeyContext";
	
	
	@Bean(name={"genericcommand", "genericcmd", "command", "system"}) 
	@Scope("singleton")
	public ICommandEnhancer genericCommandEnhancer() {

		// unlimited batching
		final BatchOption defBatchOption = new BatchOption(0, Strategy.UNLIMITED);
		// exec wait for up to 60 min
		final ExecuteOption defExecOption = new ExecuteOption(0, 60 * 60, 3, 1);
		// polling every 5 sec for up to 60 min
		final MonitorOption defMonOption = new MonitorOption(0, 10, 60 * 60, 3, 1);
		
		return new ICommandEnhancer() {
			
			@Override
			public void enhanceRequest(HttpTaskRequest request) {
				boolean isPolling = (StringUtil.equalIgnoreCase(request.getTaskType(), TaskType.asyncpoll.name()));
				if (request.getExecutionOption()==null) {
					request.setExecutionOption(defExecOption);
				}
				if (request.getBatchOption()==null) {
					request.setBatchOption(defBatchOption);
				}
				if (isPolling && request.getMonitorOption()==null) {
					request.setMonitorOption(defMonOption);
				}
			}
		};
		
	}
	
	@Bean(name={"agentCommandEnhancer", "agentcommand", "agentcmd", "agent"}) 
	@Scope("singleton")
	public ICommandEnhancer agentCommandEnhancer() {

		// unlimited batching
		final BatchOption defBatchOption = new BatchOption(0, Strategy.UNLIMITED);
		// exec wait for up to 60 min
		final ExecuteOption defExecOption = new ExecuteOption(0, 60 * 60, 3, 1);
		// polling every 5 sec for up to 60 min
		final MonitorOption defMonOption = new MonitorOption(0, 5, 60 * 60, 3, 1);
		
		return new ICommandEnhancer() {
			
			@Override
			public void enhanceRequest(HttpTaskRequest request) {
				boolean isPolling = (StringUtil.equalIgnoreCase(request.getTaskType(), TaskType.asyncpoll.name()));
				// execution and monitor options
				if (request.getExecutionOption()==null) {
					request.setExecutionOption(defExecOption);
				}
				if (request.getBatchOption()==null) {
					request.setBatchOption(defBatchOption);
				}
				if (isPolling && request.getMonitorOption()==null) {
					request.setMonitorOption(defMonOption);
				}
				// agent status polling
				if (isPolling && request.getPollTemplate()==null) {
					UrlTemplate pollTemplate = new UrlTemplate("https://<host>:19000/status/<uuid>");
					pollTemplate.addHeader("content-type", "application/json");
					request.setPollTemplate(pollTemplate);
				}
				// agent request headers
				request.getUrlTemplate().addHeader("content-type", "application/json");
				request.getUrlTemplate().addHeader("Authorization", "Basic <agentAuthKey>");
				request.getUrlTemplate().addHeader("X-CORRELATIONID", "<correlationId>");
				// agent auth
				if (request.getGlobalContext()==null) {
					request.setGlobalContext(AGENT_AUTHKEY_BEAN);
				}
				// agent response processor
				if (StringUtil.isNullOrEmpty(request.getResProcessorName())) {
					if (isPolling) {
						request.setResProcessorName(AGENT_POLL_PROCESSOR);
					}
					else {
						request.setResProcessorName(AGENT_PROCESSOR);
					}
				}
				
			}
		};
	}
	
	/**
	 * global context to keep all agent auth key (pki based)
	 * @return
	 */
	public @Bean @Scope("singleton") IGlobalContext agentAuthKeyContext() {

		return new IGlobalContext() {

			private final HashMap<String, String> PkiAuthKeys = new HashMap<String, String>();

			@SuppressWarnings("unchecked")
			@Override
			public String getValueByName(String pivotValue, String name) {
				return PkiAuthKeys.containsKey(pivotValue) ? PkiAuthKeys.get(pivotValue) : VarUtils.agentPasswordBase64;
			}

			@Override
			public boolean hasName(String pivotValue, String name) {
				return StringUtil.equalIgnoreCase("agentAuthKey", name);
			}

			@Override
			public String getPivotKey() {
				return "host";
			}

			@Override
			public void setValueForName(String pivotValue, String name, Object value) {
				if (pivotValue != null && value != null) {
					PkiAuthKeys.put(pivotValue, value.toString());
				}
			}

		};

	}
	
	/**
	 * decode and remember agent pki key for future use
	 * @param userReq
	 * @param task
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private TaskResult decodeAgentPkiKey(UrlRequest userReq, Task task, Response response) throws IOException 
	{
		int sCode = response.getStatusCode();
		String body = response.getResponseBody();
		AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
		IGlobalContext gContext = userReq.getGlobalConext();
		if (agentStatus.result != null) {
			if (StringUtil.equalIgnoreCase("pki", ((Map<String, String>)agentStatus.result).get("scheme"))) {
				String pkiTokenEncrypted = ((Map<String, String>)agentStatus.result).get("key");
				String pkiTokenClear = SecurityUtil.decryptPki(pkiTokenEncrypted, VarUtils.agentPkiCert);
				String pkiTokenBase64 = Base64.encodeBase64String(pkiTokenClear.getBytes());
				gContext.setValueForName(userReq.getTemplateValue(gContext.getPivotKey()), "pkiTokenBase64", pkiTokenBase64);
			}
		}
		return task.failed(String.format("%s - %s", sCode, agentStatus!=null ? agentStatus.errorMsg : ""), null);
	}
	
	static final String successRegex = ".*\\\"progress\\\":\\s*(\\d*).*";
	static final String failureRegex = ".*\\\"error\\\":\\s*(\\d*).*";
	/**
	 * process general agent response
	 * @param task
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private TaskResult processAgentResponse(Task task, Response response) throws IOException 
	{
		int sCode = response.getStatusCode();
		String body = response.getResponseBody();
		TaskResult res = null;
		if (body.matches(successRegex)) {
			AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
			if (agentStatus.progress == 100) {
				res = task.succeeded();
			}
		}
		else if (body.matches(failureRegex)) {
			AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
			res = task.failed(String.format("%s - %s", sCode, agentStatus!=null ? agentStatus.errorMsg : ""), null);
		}
		else if (sCode >= 400) {
			res = task.failed(Integer.toString(sCode), null);
		}
		else {
			res = task.succeeded();
		}
		return res;
	}


	/**
	 * agent processor for sync requ
	 * @return
	 */
	public @Bean @Scope("singleton") IHttpProcessor agentProcessor() {

		return new IHttpProcessor() {

			@Override
			public TaskResult processHttpReponse(Task task, Response response)
					throws IOException {
				int sCode = response.getStatusCode();
				TaskResult res = null;
				if (sCode == 401) {
					// update key
					UrlRequest req = ((SimpleHttpTask) task).getReq();
					res = decodeAgentPkiKey(req, task, response);
				}
				else {
					res = processAgentResponse(task, response);
				}
				return res;
			}
		};
	}
	
	
	/**
	 * this handles agent polling and agent pki authentication
	 * @return
	 */
	public @Bean @Scope("singleton") IHttpPollProcessor agentPollProcessor() {

		// matching pattern "status": "/status/uuid"
		final String uuidRegex = ".*\\\"/status/(.*?)\\\".*,";
		final Pattern r = Pattern.compile(uuidRegex);
		return new IHttpPollProcessor() {

			@Override
			public TaskResult checkPollProgress(Task task, Response response) throws IOException {

				TaskResult res = processAgentResponse(task, response);
				if (res != null && res.getStatus() == TaskResultEnum.Success) {
					res.addResultDetail("uuid", task.<String>getContextValue("uuid"));
				}
				return res;
			}

			@Override
			public TaskResult preparePollTask(
					Task task,
					Response response,
					UrlRequest pollReq) throws IOException
			{
				int sCode = response.getStatusCode();
				if (sCode == 401) {
					// handle pki auth failure
					return decodeAgentPkiKey(pollReq, task, response);
				}
				// try to decode uuid from response for future polling
				String body = response.getResponseBody();
				Matcher m = r.matcher(body);
				if (m.find()) {
					String uuid = m.group(1);
					task.setExtTaskUuid(uuid);
					pollReq.addTemplateValue("uuid", uuid);
					task.addContext("uuid", pollReq.getTemplateValue("uuid"));
					return task.succeeded();
				} 
				else {
					return processAgentResponse(task, response);
				}
			}

		};

	}

	/** agent status */
	public static class AgentStatus {
		public String status;
		public int progress;
		public int error;
		public String errorMsg;
		public Object result;
	}


}
