package resources.utils;

import java.util.HashMap;
import java.util.Map;

import com.stackscaling.agentmaster.resources.utils.DateUtils;

public class JsonResponse {
	
	private JsonResponseStatus status;
	
	private Map<String, Object> results = new HashMap<String, Object>();
	
	public JsonResponseStatus getStatus() {
		return status;
	}

	public void setStatus(JsonResponseStatus status) {
		this.status = status;
	}

	public Map<String, Object> getResults() {
		return results;
	}

	public void setResults(Map<String, Object> results) {
		this.results = results;
	}

	public JsonResponse addResult(String name, Object value) {
		results.put(name, value);
		return this;
	}
	
	public static JsonResponse successResponse(String msg) {
		JsonResponse res = new JsonResponse();
		res.status = new JsonResponseStatus(
				JsonResponseStatusCode.success.name(), DateUtils.getNowDateTimeDotStr(), msg);
		return res;
	}
	
	public static JsonResponse failedResponse(String msg) {
		JsonResponse res = new JsonResponse();
		res.status = new JsonResponseStatus(
				JsonResponseStatusCode.failed.name(), DateUtils.getNowDateTimeDotStr(), msg);
		return res;
	}

	public static class JsonResponseStatus {
		private String statusCode;
		private String lastRefreshed;
		private String msg;
		JsonResponseStatus() {}
		JsonResponseStatus(String statusCode, String lastRefreshed, String msg) {
			this.statusCode = statusCode;
			this.lastRefreshed = lastRefreshed;
			this.msg = msg;
		}
		public String getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}
		public String getLastRefreshed() {
			return lastRefreshed;
		}
		public void setLastRefreshed(String lastRefreshed) {
			this.lastRefreshed = lastRefreshed;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
	}
	
	public static enum JsonResponseStatusCode {
		success, failed,
	}

}
