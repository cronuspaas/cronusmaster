package com.stackscaling.agentmaster.resources.job;

import java.io.IOException;

import org.lightj.util.JsonUtil;

import com.stackscaling.agentmaster.resources.DataType;

/**
 * interval based cmd job
 * @author binyu
 *
 */
public class CmdIntervalJobData extends IntervalJobDataImpl {

	public CmdIntervalJobData() {
		super();
		this.jobType = DataType.CMDJOB;
	}

	@Override
	public IntervalJob getJobById(String jobId) throws IOException  {
		String jobContent = userDataDao.readData(jobType, jobId);
		return JsonUtil.decode(jobContent, CmdIntervalJobImpl.class);
	}

}
