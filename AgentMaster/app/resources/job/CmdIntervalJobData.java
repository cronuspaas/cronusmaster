package resources.job;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import resources.IUserDataDao.DataType;

@Component("cmdIntervalJob")
@Scope("singleton")
public class CmdIntervalJobData extends IntervalJobDataImpl {
	
	public CmdIntervalJobData() {
		super();
		this.jobType = DataType.CMDJOB;
	}

}
