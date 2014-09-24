package com.stackscaling.agentmaster.resources.log;

import com.stackscaling.agentmaster.resources.DataType;

public class CmdLog extends BaseLog {

	public CmdLog() {
		super(DataType.COMMAND, DataType.CMDLOG);
	}

	public String uuid() {
		return String.format("%s~%s~%s~%s",
						timestamp,
						nodeGroup.getType(),
						nodeGroup.getName(),
						commandKey);
	}

}
