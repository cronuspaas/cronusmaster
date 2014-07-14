package resources.log;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import resources.IUserDataDao.DataType;

@Component("jobLogger")
@Scope("singleton")
public class JobLogger extends LoggerImpl<JobLog> {
	
	public JobLogger() {
		super();
		this.dataType = DataType.JOBLOG;
		this.logDoKlass = JobLog.class;
	}

}
