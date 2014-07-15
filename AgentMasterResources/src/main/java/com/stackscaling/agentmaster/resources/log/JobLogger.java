package com.stackscaling.agentmaster.resources.log;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;

public class JobLogger extends LoggerImpl<JobLog> {

	public JobLogger() {
		super();
		this.dataType = DataType.JOBLOG;
		this.logDoKlass = JobLog.class;
	}

}
