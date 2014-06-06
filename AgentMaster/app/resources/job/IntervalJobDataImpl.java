package resources.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.IUserDataDao.DataType;
import resources.log.BaseLog;

public class IntervalJobDataImpl implements IntervalJobData {

	@Autowired(required=true)
	private IUserDataDao userDataDao;

	/** type of job */
	private DataType jobType;

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
		for (String fileName : userDataDao.listNames(jobType)) {
			String jobDataContent = userDataDao.readData(jobType, fileName);
			jobs.add(JsonUtil.decode(jobDataContent, CmdIntervalJobImpl.class));
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
	public IntervalJob getJobById(String jobId) throws IOException {
		String jobContent = userDataDao.readData(jobType, jobId);
		return (IntervalJob) JsonUtil.decode(jobContent, jobType.getDoKlass());
	}

}
