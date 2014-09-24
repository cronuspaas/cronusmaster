package com.stackscaling.agentmaster.resources.job;

import java.io.IOException;

import org.lightj.util.JsonUtil;

import com.stackscaling.agentmaster.resources.DataType;

/**
 * interval based flow job
 * @author binyu
 *
 */
public class FlowIntervalJobData extends IntervalJobDataImpl {

	public FlowIntervalJobData() {
		super();
		this.jobType = DataType.FLOWJOB;
	}

	@Override
	public IntervalJob getJobById(String jobId) throws IOException {
		String jobContent = userDataDao.readData(jobType, jobId);
		return JsonUtil.decode(jobContent, FlowIntervalJobImpl.class);
	}

}
