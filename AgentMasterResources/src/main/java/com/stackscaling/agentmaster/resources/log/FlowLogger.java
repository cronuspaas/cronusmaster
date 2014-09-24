package com.stackscaling.agentmaster.resources.log;

import com.stackscaling.agentmaster.resources.DataType;

public class FlowLogger extends LoggerImpl<FlowLog> {

	public FlowLogger() {
		super();
		this.dataType = DataType.FLOWLOG;
		this.logDoKlass = FlowLog.class;
	}
}
