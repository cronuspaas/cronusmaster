package com.stackscaling.agentmaster.resources.agent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.lightj.task.IGlobalContext;
import org.lightj.task.Task;
import org.lightj.task.TaskResult;
import org.lightj.task.TaskResultEnum;
import org.lightj.task.asynchttp.IHttpPollProcessor;
import org.lightj.task.asynchttp.IHttpProcessor;
import org.lightj.task.asynchttp.SimpleHttpTask;
import org.lightj.task.asynchttp.UrlRequest;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.stackscaling.agentmaster.resources.security.SecurityUtil;
import com.stackscaling.agentmaster.resources.utils.VarUtils;
import com.ning.http.client.Response;

@Configuration
public class AgentResourceProvider {

	public static final String AGENT_PROCESSOR = "agentProcessor";
	public static final String AGENT_POLL_PROCESSOR = "agentPollProcessor";
	public static final String AGENT_AUTHKEY_BEAN = "agentAuthKeyContext";
	
	/**
	 * global context to keep all agent auth key (pki based)
	 * @return
	 */
	public @Bean @Scope("singleton") IGlobalContext agentAuthKeyContext() {

		return new IGlobalContext() {

			private final HashMap<String, String> PkiAuthKeys = new HashMap<String, String>();

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


	/**
	 * agent processor for sync requ
	 * @return
	 */
	public @Bean @Scope("singleton") IHttpProcessor agentProcessor() {

		final String successRegex = ".*\\\"progress\\\"\\s*:\\s*100.*";
		final String failureRegex = ".*\\\"error\\\"\\s*:\\s*(.*),.*";

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
					String body = response.getResponseBody();
					if (body.matches(successRegex)) {
						res = task.succeeded();
					}
					else if (body.matches(failureRegex)) {
						AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
						res = task.failed(String.format("%s - %s", sCode, agentStatus!=null ? agentStatus.errorMsg : ""), null);
					}
					else {
						res = task.failed(String.format("invalid agent response %s", body), null);
					}
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

		final String successRegex = ".*\\\"progress\\\"\\s*:\\s*100.*";
		final String failureRegex = ".*\\\"error\\\"\\s*:\\s*(.*),.*";
		// matching pattern "status": "/status/uuid"
		final String uuidRegex = ".*\\\"/status/(.*?)\\\".*,";
		final Pattern r = Pattern.compile(uuidRegex);
		return new IHttpPollProcessor() {

			@Override
			public TaskResult checkPollProgress(Task task, Response response) throws IOException {

				int sCode = response.getStatusCode();
				TaskResult res = null;
				String body = response.getResponseBody();
				if (body.matches(successRegex)) {
					res = task.succeeded();
				}
				else if (body.matches(failureRegex)) {
					AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
					res = task.failed(String.format("%s - %s", sCode, agentStatus.errorMsg), null);
				}
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
					return task.failed("cannot find uuid", null);
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
