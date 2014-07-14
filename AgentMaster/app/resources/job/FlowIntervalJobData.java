package resources.job;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import resources.IUserDataDao.DataType;

@Component("flowIntervalJob")
@Scope("singleton")
public class FlowIntervalJobData extends IntervalJobDataImpl {
	
	public FlowIntervalJobData() {
		super();
		this.jobType = DataType.FLOWJOB;
	}

}
