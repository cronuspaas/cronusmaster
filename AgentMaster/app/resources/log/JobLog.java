package resources.log;

import resources.IUserDataDao.DataType;

public class JobLog extends BaseLog {

	private String jobId;
	
	public JobLog() {
		super(DataType.CMDJOB);
	}
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String uuid() {
		return String.format("%s~%s~%s~%s~%s", 
						timestamp,
						nodeGroup.getType(),
						nodeGroup.getName(), 
						commandKey,
						jobId);
	}
}
