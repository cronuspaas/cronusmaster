package com.stackscaling.agentmaster.resources.log;

import com.stackscaling.agentmaster.resources.DataType;

public class JobLog extends BaseLog {

	private String jobId;

	public JobLog() {
		super(DataType.CMDJOB, DataType.JOBLOG);
	}

	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String uuid() {
		return String.format("%s~%s~%s~%s~%s",
						timestamp,
						nodeGroup.getType(),
						nodeGroup.getName(),
						commandKey,
						jobId);
	}
}
