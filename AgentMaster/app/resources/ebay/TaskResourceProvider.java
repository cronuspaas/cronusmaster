package resources.ebay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.utils.VarUtils;

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

import resources.security.SecurityUtil;

import com.ning.http.client.Response;

@Configuration
public class TaskResourceProvider {
	
	/**
	 * global context to keep all agent auth key (pki based)
	 * @return
	 */
	public static final String AGENT_AUTHKEY_BEAN = "agentAuthKeyContext";
	public @Bean @Scope("singleton") IGlobalContext agentAuthKeyContext() {
		
		return new IGlobalContext() {
			
			private final String BaseAuthKey = "YWdlbnQ6dG95YWdlbnQ=";
			private final HashMap<String, String> PkiAuthKeys = new HashMap<String, String>();

			@Override
			public String getValueByName(String pivotValue, String name) {
				return PkiAuthKeys.containsKey(pivotValue) ? PkiAuthKeys.get(pivotValue) : BaseAuthKey;
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
	 * agent processor for sync requ
	 * @return
	 */
	public static final String AGENT_PROCESSOR = "agentProcessor";
	public @Bean @Scope("singleton") IHttpProcessor agentProcessor() {
		
		final String successRegex = ".*\\\"progress\\\"\\s*:\\s*100.*";
		final String failureRegex = ".*\\\"error\\\"\\s*:\\s*(.*),.*";

		return new IHttpProcessor() {
			
			@Override
			public TaskResult processHttpReponse(Task task, Response response)
					throws IOException {
				int sCode = response.getStatusCode();
				TaskResult res = null;
				if (sCode >= 400) {
					if (sCode == 401) {
						// update key 
						UrlRequest req = ((SimpleHttpTask) task).getReq();
						String body = response.getResponseBody();
						AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
						IGlobalContext gContext = req.getGlobalConext();
						if (agentStatus.result != null) {
							if (StringUtil.equalIgnoreCase("pki", ((Map<String, String>)agentStatus.result).get("scheme"))) {
								String pkiTokenEncrypted = ((Map<String, String>)agentStatus.result).get("key");
								String pkiTokenClear = SecurityUtil.decryptPki(pkiTokenEncrypted, VarUtils.AGENT_CRT_LOCATION);
								String pkiTokenBase64 = Base64.encodeBase64String(pkiTokenClear.getBytes());
								gContext.setValueForName(req.getTemplateValue(gContext.getPivotKey()), "pkiTokenBase64", pkiTokenBase64);
							}
						}
					}
					res = task.failed(Integer.toString(sCode), null);
				}
				else {
					String body = response.getResponseBody();
					if (body.matches(successRegex)) {
						res = task.succeeded();
					}
					else if (body.matches(failureRegex)) {
						res = task.failed(body, null);
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
	public static final String AGENT_POLL_PROCESSOR = "agentPollProcessor";
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
				if (sCode >= 400) {
					res = task.failed(Integer.toString(sCode), null);
				}
				else {
					String body = response.getResponseBody();
					if (body.matches(successRegex)) {
						res = task.succeeded();
					}
					else if (body.matches(failureRegex)) {
						res = task.failed(body, null);
					}
				}
				if (res != null) {
					if (res.getStatus() == TaskResultEnum.Success) {
						res.addResultDetail("uuid", task.<String>getContextValue("uuid"));
					}
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
				if (sCode >= 400) {
					if (sCode == 401) {
						// update key 
						String body = response.getResponseBody();
						AgentStatus agentStatus = JsonUtil.decode(body, AgentStatus.class);
						IGlobalContext gContext = pollReq.getGlobalConext();
						if (agentStatus.result != null) {
							if (StringUtil.equalIgnoreCase("pki", ((Map<String, String>)agentStatus.result).get("scheme"))) {
								String pkiTokenEncrypted = ((Map<String, String>)agentStatus.result).get("key");
								String pkiTokenClear = SecurityUtil.decryptPki(pkiTokenEncrypted, VarUtils.AGENT_CRT_LOCATION);
								String pkiTokenBase64 = Base64.encodeBase64String(pkiTokenClear.getBytes());
								gContext.setValueForName(pollReq.getTemplateValue(gContext.getPivotKey()), "pkiTokenBase64", pkiTokenBase64);
							}
						}
					}
					return task.failed(Integer.toString(sCode), null);
				}
				String body = response.getResponseBody();
				Matcher m = r.matcher(body);
				if (m.find()) {
					String uuid = m.group(1);
					task.setExtTaskUuid(uuid);
					pollReq.addTemplateValue("uuid", uuid);
					task.addContext("uuid", pollReq.getTemplateValue("uuid"));
					return task.succeeded();
				} else {
					return task.failed("cannot find uuid", null);
				}
			}
			
		};
		
	}
	
	/**
	 * handling paas result polling
	 * @return
	 */
	public @Bean @Scope("singleton") IHttpPollProcessor paasPollProcessor() {
		
		return new IHttpPollProcessor() {
			
			@Override
			public TaskResult preparePollTask(Task task, Response response, UrlRequest pollReq)
					throws IOException {
				int sCode = response.getStatusCode();
				String body = response.getResponseBody();
				if (sCode >= 200 && sCode < 300) {
					PaaSJobResponse paasRes = JsonUtil.decode(body, PaaSJobResponse.class);
					if (paasRes.getLinks().length > 1) {
						String jobLink = paasRes.getLinks()[0];
						task.setExtTaskUuid(jobLink);
						pollReq.getUrlTemplate().setUrl(jobLink);
						return task.succeeded();
					}
					return task.failed(body, TaskResultEnum.Failed, "Cannot find status link", null);
				}
				return task.failed(body, TaskResultEnum.Failed, String.format("Request failed with http code = %s", sCode), null);
				
			}
			
			@Override
			public TaskResult checkPollProgress(Task task, Response response)
					throws IOException {
				int sCode = response.getStatusCode();
				String body = response.getResponseBody();
				if (sCode >= 200 && sCode < 300) {
					PaaSJobProgress paasRes = JsonUtil.decode(body, PaaSJobProgress.class);
					int percentComplete = paasRes.getPercentComplete();
					String status = paasRes.getExecutionStatus();
					if (percentComplete == 100 && "COMPLETED".equals(status)) {
						// successful
						return task.succeeded();
					}
					else if ("PAUSED".equals(status)) {
						// failed
						return task.failed(body, TaskResultEnum.Failed, paasRes.getResult(), null);
					}
					return task.hasResult(TaskResultEnum.Running, null);
				}
				return task.failed(body, TaskResultEnum.Failed, String.format("Polling failed with http code = %s", sCode), null);
			}
		};
		
	}
	
	/** paas job response, with polling url */
	static class PaaSJobResponse {
		private String[] links;

		public String[] getLinks() {
			return links;
		}
		public void setLinks(String[] links) {
			this.links = links;
		}
	}
	
	/** paas job progress, with status and percentage */
	static class PaaSJobProgress {
		private String executionStatus;
		private int percentComplete;
		private String result;
		public String getExecutionStatus() {
			return executionStatus;
		}
		public void setExecutionStatus(String executionStatus) {
			this.executionStatus = executionStatus;
		}
		public int getPercentComplete() {
			return percentComplete;
		}
		public void setPercentComplete(int percentComplete) {
			this.percentComplete = percentComplete;
		}
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
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
