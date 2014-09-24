package com.stackscaling.agentmaster.resources.log;

import com.stackscaling.agentmaster.resources.DataType;

public class CmdLogger extends LoggerImpl<CmdLog> {

	public CmdLogger() {
		super();
		this.dataType = DataType.CMDLOG;
		this.logDoKlass = CmdLog.class;
	}
}
