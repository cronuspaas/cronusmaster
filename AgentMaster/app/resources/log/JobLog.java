package resources.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.lightj.util.DateUtil;

import resources.command.CommandImpl;
import resources.command.ICommand;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.NodeGroupImpl;

/**
 * command result
 * @author binyu
 *
 */
public class JobLog {
	
	public static final String DateFormat = "yyyy.MM.dd.HH.MM.ss.SSSZZZ";
	private String timestamp = DateUtil.format(new Date(), DateFormat);
	private UserCommand userCommand;
	private List<CommandResponse> commandResponses = new ArrayList<JobLog.CommandResponse>();
	
	public UserCommand getUserCommand() {
		return userCommand;
	}
	public void setUserCommand(UserCommand userCommand) {
		this.userCommand = userCommand;
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
	public String uuid() {
		return userCommand.jobId!=null ?
				String.format("%s~%s~%s~%s~%s", 
						timestamp,
						userCommand.nodeGroup.getType(),
						userCommand.nodeGroup.getName(), 
						userCommand.cmd.getName(),
						userCommand.jobId)
				: String.format("%s~%s~%s~%s", 
						timestamp, 
						userCommand.nodeGroup.getType(),
						userCommand.nodeGroup.getName(), 
						userCommand.cmd.getName());
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

	public static class UserCommand {
		
		@JsonDeserialize(as=CommandImpl.class)
		public ICommand cmd;
		@JsonDeserialize(as=NodeGroupImpl.class)
		public INodeGroup nodeGroup;
		public String jobId;
		
	}
	
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

}
