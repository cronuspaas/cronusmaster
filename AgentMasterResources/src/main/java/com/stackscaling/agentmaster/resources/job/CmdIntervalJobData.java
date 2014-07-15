package com.stackscaling.agentmaster.resources.job;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;

public class CmdIntervalJobData extends IntervalJobDataImpl {

	public CmdIntervalJobData() {
		super();
		this.jobType = DataType.CMDJOB;
	}

}
