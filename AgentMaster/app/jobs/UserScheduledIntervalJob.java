package jobs;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.job.IntervalJob;
import com.stackscaling.agentmaster.resources.job.IntervalJobData;

/**
 * this runs on interval and pick up user specified interval based "job" (command or flow)
 * @author binyu
 *
 */
@Every("5mn")
public class UserScheduledIntervalJob extends Job {
	
	/** logger */
	static Logger logger = LoggerFactory.getLogger(UserScheduledIntervalJob.class);
	static final int BASE_INTERVAL_UNIT = 5;

	private static final AtomicInteger totalRuns = new AtomicInteger(0);

	@Override
	public void doJob() throws Exception {
//		if (ClusteringModule.isMaster()) {
			// master node (including local) needs to pick up the jobs
			IntervalJobData cmdJobData = UserDataProviderFactory.getIntervalJobOfType(DataType.CMDJOB);
			List<IntervalJob> jobs = cmdJobData.getAllJobs();
			for (IntervalJob job : jobs) {
				if (!job.isEnabled()) continue;
				int intervalOfBase = job.getIntervalInMinute() / BASE_INTERVAL_UNIT;
				if (totalRuns.get()%intervalOfBase == 0) {
					// time to run this job
					job.runJobAsync();
				}
			}
			totalRuns.incrementAndGet();
//		}
	}

}
