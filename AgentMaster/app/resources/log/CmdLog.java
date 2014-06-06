package resources.log;

import resources.IUserDataDao.DataType;

public class CmdLog extends BaseLog {

	public CmdLog() {
		super(DataType.COMMAND);
	}
	
	public String uuid() {
		return String.format("%s~%s~%s~%s", 
						timestamp, 
						nodeGroup.getType(),
						nodeGroup.getName(),
						commandKey);
	}

}
