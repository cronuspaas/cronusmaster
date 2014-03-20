package resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.lightj.util.DateUtil;

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
		return String.format("%s~%s~%s", 
				timestamp, 
				userCommand.nodeGroup.getName(), 
				userCommand.cmd.getName());
	}

	public static class UserCommand {
		
		@JsonDeserialize(as=CommandImpl.class)
		public ICommand cmd;
		@JsonDeserialize(as=NodeGroupImpl.class)
		public INodeGroup nodeGroup;
		
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
