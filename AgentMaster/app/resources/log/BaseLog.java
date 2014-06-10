package resources.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.lightj.session.FlowInfo;
import org.lightj.util.DateUtil;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import resources.IUserDataDao.DataType;
import resources.elasticsearch.EsResourceProvider;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.NodeGroupImpl;
import resources.utils.VarUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * base log for command, job, and workflow
 * @author binyu
 *
 */
public abstract class BaseLog implements ILog {
	
	public static final String DateFormat = "yyyy.MM.dd.HH.mm.ss.SSSZZZ";
	public static final int ProgressTotalUnit = 1000;
	
	/** timestamp of the log */
	protected String timestamp = DateUtil.format(new Date(), DateFormat);
	
	/** user data */
	protected Map<String, String> userData;

	/** node group */
	@JsonDeserialize(as=NodeGroupImpl.class)
	protected INodeGroup nodeGroup;

	/** command result */
	protected List<CommandResponse> commandResponses = new ArrayList<BaseLog.CommandResponse>();
	
	/** command type */
	protected DataType commandType;
	
	/** command key, command id, workflow id etc. */
	protected String commandKey;
	
	/** status */
	protected String status;
	
	/** progress 1 unit = 0.1%, et. 1000 = 100%*/
	protected int progress;
	
	/** status detail of #success-#failure-#other */
	protected String statusDetail;
	
	public BaseLog(DataType commandType) {
		this.commandType = commandType;
	}
	
	public List<CommandResponse> getCommandResponses() {
		return commandResponses;
	}
	public void setCommandResponses(List<CommandResponse> commandResponses) {
		this.commandResponses = commandResponses;
	}
	public void addCommandResponse(CommandResponse commandResponse) {
//		this.commandResponses.add(commandResponse);
		
		try {
			String jsonStr = JsonUtil.encode(commandResponse);
			
			Client client = EsResourceProvider.getEsClient();
			IndexResponse response = client.prepareIndex("log", "cmdLog")
					.setSource(jsonStr).execute().actionGet();
			
			// Index name
			String _index = response.getIndex();
			// Type name
			String _type = response.getType();
			// Document ID (generated or not)
			String _id = response.getId();
			// Version (if it's the first time you index this document, you will get: 1)
			long _version = response.getVersion();
			
			commandResponse.indexMeta = String.format("%s,%s,%s,%s", _index, _type, _id, _version);
			
		} catch (Exception e) {
			
			commandResponse.indexMeta = e.getMessage();
		
		}

		commandResponse.trimBodyToLength(VarUtils.BASELOG_CMDRES_LENGTH);
		this.commandResponses.add(commandResponse);
	}
	public String getTimestamp() {
		return timestamp;
	}
	/** do NOT use, this should only used by deserialization framework */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public Map<String, String> getUserData() {
		return userData;
	}
	public void setUserData(Map<String, String> userData) {
		this.userData = userData;
	}
	public INodeGroup getNodeGroup() {
		return nodeGroup;
	}
	public void setNodeGroup(INodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	public DataType getCommandType() {
		return commandType;
	}	
	public String getCommandKey() {
		return commandKey;
	}
	public void setCommandKey(String commandKey) {
		this.commandKey = commandKey;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getProgress() {
		return progress;
	}
	public String getDisplayProgress() {
		return String.format("%.1f%%", (((float) progress/ProgressTotalUnit) * 100));
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public void incProgress(int progDelta) {
		this.progress = Math.min(ProgressTotalUnit, this.progress + progDelta);
	}
	public String getStatusDetail() {
		return statusDetail;
	}
	public void setStatusDetail(String statusDetail) {
		this.statusDetail = statusDetail;
	}
	public void setStatusDetail(int numSuccess, int numFail, int numOther) {
		this.statusDetail = String.format("%s:%s:%s", numSuccess, numFail, numOther);
	}

	/**
	 * aggregation
	 * @param matchField
	 * @param matchRegex
	 * @return
	 */
	public LogAggregation aggregate(String matchField, String matchRegex) {
		LogAggregation aggregation = new LogAggregation(matchField, matchRegex);
		for (CommandResponse cmdRes : commandResponses) {
			aggregation.aggregateEntry(cmdRes);
		}
		return aggregation;
	}

	public static Map<String, String> getLogMetaFromName(String uuid) {
		HashMap<String, String> meta = new HashMap<String, String>();
		String[] tokens = uuid.split("~");
		meta.put("timeStampDisplay", tokens[0]);
		meta.put("timeStamp", Long.toString(DateUtil.parse(tokens[0], DateFormat).getTime()));
		meta.put("nodeGroupType", tokens[1]);
		meta.put("nodeGroup", tokens[2]);
		meta.put("command", tokens[3]);
		if (tokens.length > 4) {
			meta.put("jobName", tokens[4]);
			meta.put("lastToken", tokens[4]);
		} 
		else {
			meta.put("lastToken", tokens[3]);
		}
		return meta;
	}

	/**
	 * command response
	 * @author biyu
	 *
	 */
	public static class CommandResponse {
		
		public String host;
		public int httpStatusCode;
		public String responseBody;
		public long timeReceived;
		public String indexMeta;
		public CommandResponse() {}
		public CommandResponse(String host, int httpStatusCode, String responseBody) {
			this.host = host;
			this.httpStatusCode = httpStatusCode;
			this.responseBody = responseBody;
			this.timeReceived = System.currentTimeMillis();
		}
		public void trimBodyToLength(int length) {
			this.responseBody = StringUtil.trimToLength(this.responseBody, length);
		}
		public void setIndexMeta(String indexMeta) {
			this.indexMeta = indexMeta;
		}
	}
	
	/**
	 * user triggered workflow
	 * @author biyu
	 *
	 */
	public static class UserWorkflow {
		
		public FlowInfo jobInfo;
		
	}
	


}
