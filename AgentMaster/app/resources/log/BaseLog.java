package resources.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.session.FlowInfo;
import org.lightj.util.DateUtil;

import resources.IUserDataDao.DataType;
import resources.command.CommandImpl;
import resources.command.ICommand;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.NodeGroupImpl;
import resources.workflow.IWorkflowMeta;
import resources.workflow.WorkflowMetaImpl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * base log for command, job, and workflow
 * @author binyu
 *
 */
public abstract class BaseLog implements ILog {
	
	public static final String DateFormat = "yyyy.MM.dd.HH.mm.ss.SSSZZZ";
	
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
	 * user command 
	 * @author biyu
	 *
	 */
	public static class UserCommand {
		
		@JsonDeserialize(as=CommandImpl.class)
		public ICommand cmd;
		public String jobId;
		
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
		public CommandResponse() {}
		public CommandResponse(String host, int httpStatusCode, String responseBody) {
			this.host = host;
			this.httpStatusCode = httpStatusCode;
			this.responseBody = responseBody;
			this.timeReceived = System.currentTimeMillis();
		}
	}
	
	/**
	 * user triggered workflow
	 * @author biyu
	 *
	 */
	public static class UserWorkflow {
		
		@JsonDeserialize(as=WorkflowMetaImpl.class)
		public IWorkflowMeta workflow;
		public FlowInfo jobInfo;
		
	}
	


}
