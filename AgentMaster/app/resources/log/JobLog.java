package resources.log;

import resources.IUserDataDao.DataType;
import resources.log.BaseLog.UserCommand;

public class JobLog extends BaseLog {

	protected UserCommand userCommand;
	
	public JobLog() {
		super(DataType.CMDJOB);
	}
	
	public UserCommand getUserCommand() {
		return userCommand;
	}
	public void setUserCommand(UserCommand userCommand) {
		this.userCommand = userCommand;
	}

	public String uuid() {
		return String.format("%s~%s~%s~%s~%s", 
						timestamp,
						nodeGroup.getType(),
						nodeGroup.getName(), 
						userCommand.cmd.getName(),
						userCommand.jobId);
	}
	@Override
	public String getCommandKey() {
		return userCommand.jobId;
	}
}
