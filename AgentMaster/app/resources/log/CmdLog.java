package resources.log;

import resources.IUserDataDao.DataType;
import resources.log.BaseLog.UserCommand;

public class CmdLog extends BaseLog {

	protected UserCommand userCommand;
	
	public CmdLog() {
		super(DataType.COMMAND);
	}
	
	public UserCommand getUserCommand() {
		return userCommand;
	}
	public void setUserCommand(UserCommand userCommand) {
		this.userCommand = userCommand;
	}
	public String uuid() {
		return String.format("%s~%s~%s~%s", 
						timestamp, 
						nodeGroup.getType(),
						nodeGroup.getName(), 
						userCommand.cmd.getName());
	}
	@Override
	public String getCommandKey() {
		return userCommand.cmd.getName();
	}

}
