package com.stackscaling.agentmaster.resources.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;

/**
 * base class of interval job
 * @author binyu
 *
 */
public abstract class IntervalJobDataImpl implements IntervalJobData {

	@Autowired(required=true)
	protected IUserDataDao userDataDao;

	/** type of job */
	protected DataType jobType;

	public IntervalJobDataImpl() {
	}

	public DataType getJobType() {
		return jobType;
	}

	public void setJobType(DataType jobType) {
		this.jobType = jobType;
	}

	@Override
	public IUserDataDao getUserDataDao() {
		return userDataDao;
	}

	@Override
	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userDataDao = userConfigs;
	}

	@Override
	public List<IntervalJob> getAllJobs() throws IOException {
		ArrayList<IntervalJob> jobs = new ArrayList<IntervalJob>();
		for (UserDataMeta logMeta : userDataDao.listNames(jobType)) {
			String jobDataContent = userDataDao.readData(jobType, logMeta.getName());
			CmdIntervalJobImpl jobImpl = JsonUtil.decode(jobDataContent, CmdIntervalJobImpl.class);
			jobImpl.setUserDataMeta(logMeta);
			jobs.add(jobImpl);
		}
		return jobs;
	}

	@Override
	public void save(IntervalJob job) throws IOException {
		userDataDao.saveData(jobType, job.getName(), JsonUtil.encode(job));
	}

	@Override
	public void delete(String jobId) throws IOException {
		userDataDao.deleteData(jobType, jobId);
	}

	@Override
	public abstract IntervalJob getJobById(String jobId) throws IOException;

}
