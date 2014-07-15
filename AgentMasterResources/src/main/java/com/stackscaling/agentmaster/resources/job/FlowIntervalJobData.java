package com.stackscaling.agentmaster.resources.job;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;

public class FlowIntervalJobData extends IntervalJobDataImpl {

	public FlowIntervalJobData() {
		super();
		this.jobType = DataType.FLOWJOB;
	}

}
