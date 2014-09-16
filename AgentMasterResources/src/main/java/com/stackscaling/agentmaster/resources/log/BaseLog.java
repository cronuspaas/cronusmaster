package com.stackscaling.agentmaster.resources.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.session.FlowInfo;
import org.lightj.task.TaskResultEnum;
import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroup;
import com.stackscaling.agentmaster.resources.nodegroup.NodeGroupImpl;
import com.stackscaling.agentmaster.resources.utils.DateUtils;
import com.stackscaling.agentmaster.resources.utils.ElasticSearchUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * base log for command, job, and workflow
 * @author binyu
 *
 */
public abstract class BaseLog implements ILog {
	
	static Logger logger = LoggerFactory.getLogger(BaseLog.class);

	/** timestamp of the log */
	protected String timestamp = DateUtils.getNowDateTimeDotStr();

	/** user data */
	protected Map<String, String> userData;
	
	/** type of log */
	protected DataType logType;

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
	protected String status = TaskResultEnum.Running.name();

	/** status detail of #success-#failure-#other */
	protected String statusDetail;
	
	/** percentile progress */
	protected float progress;

	/** whether this task have more raw logs */
	protected boolean hasRawLogs;

	/** whether raw logs for this task is already fetched */
	protected boolean rawLogsFetched;

	public BaseLog(DataType commandType, DataType logType) {
		this.commandType = commandType;
		this.logType = logType;
	}

	public List<CommandResponse> getCommandResponses() {
		return commandResponses;
	}
	public void setCommandResponses(List<CommandResponse> commandResponses) {
		this.commandResponses = commandResponses;
	}
	public void addCommandResponse(CommandResponse commandResponse) {

		try {
			// add logId for elastic search only
			commandResponse.logId = this.uuid();
			String jsonStr = VarUtils.esDataMapper.writeValueAsString(commandResponse);

			// Index name
			String _index = "log";
			// Type name
			String _type = this.getClass().getSimpleName();
			// Document ID (generated or not)
			String _id = String.format("%s~%s", this.uuid(), commandResponse.host);

			ElasticSearchUtils.insertDocument(_index, _type, _id, jsonStr);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}

		// trim response length so we don't persist too much
		commandResponse.trimBodyToLength(VarUtils.cmdResLength);
		// reset logId back to null before we persisted it as part of user data
		commandResponse.logId = null;
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
	public String getStatusDetail() {
		return statusDetail;
	}
	public void setStatusDetail(String statusDetail) {
		this.statusDetail = statusDetail;
	}
	public void setStatusDetail(int numSuccess, int numFail, int numRunning) {
		this.statusDetail = String.format("%s:%s:%s", numSuccess, numFail, numRunning);
	}
	public boolean isHasRawLogs() {
		return hasRawLogs;
	}
	public void setHasRawLogs(boolean hasRawLogs) {
		this.hasRawLogs = hasRawLogs;
	}
	public boolean isRawLogsFetched() {
		return rawLogsFetched;
	}
	public void setRawLogsFetched(boolean rawLogsFetched) {
		this.rawLogsFetched = rawLogsFetched;
	}
	public DataType getLogType() {
		return logType;
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
		Date ts = DateUtils.fromDateTimeDotStr(tokens[0]);
		meta.put("timeStampDisplay", DateUtils.getDateTimeStr(ts));
		meta.put("timeStamp", Long.toString(ts.getTime()));
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
		public Date timeReceived;
		public String status;
		public String logId;
		public CommandResponse() {}
		public CommandResponse(String host, String status, int httpStatusCode, String responseBody) {
			this.host = host;
			this.status = status;
			this.httpStatusCode = httpStatusCode;
			this.responseBody = responseBody;
			this.timeReceived = new Date();
		}
		public void trimBodyToLength(int length) {
			this.responseBody = StringUtil.trimToLength(this.responseBody, length);
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
