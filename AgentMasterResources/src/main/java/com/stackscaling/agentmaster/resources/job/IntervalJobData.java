package com.stackscaling.agentmaster.resources.job;

import java.io.IOException;
import java.util.List;

import com.stackscaling.agentmaster.resources.IUserData;

public interface IntervalJobData extends IUserData {

	/**
	 * get job by id
	 * @param jobId
	 * @return
	 * @throws IOException
	 */
	public IntervalJob getJobById(String jobId) throws IOException;

	/**
	 * get all commands back
	 * @return
	 */
	public List<IntervalJob> getAllJobs() throws IOException;

	/**
	 * save jobs
	 * @param configFileContent
	 * @throws IOException
	 */
	public void save(IntervalJob job) throws IOException;

	/**
	 * delete a job
	 * @param job
	 * @throws IOException
	 */
	public void delete(String jobId) throws IOException;

}
