package com.stackscaling.agentmaster.resources.job;

import com.stackscaling.agentmaster.resources.IUserData;

/**
 * interval based job
 * @author binyu
 *
 */
public interface IntervalJob extends IUserData 
{

	/**
	 * interval in minute
	 * @return
	 */
	public int getIntervalInMinute();

	/**
	 * interval in minute
	 * @param intervalInMinute
	 */
	public void setIntervalInMinute(int intervalInMinute);

	/**
	 * detail description of the job
	 * @return
	 */
	public String getDescription();

	/**
	 * is job active
	 * @return
	 */
	public boolean isEnabled();

	/**
	 * job status
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * execute the job asynchronously
	 */
	public void runJobAsync();

}
