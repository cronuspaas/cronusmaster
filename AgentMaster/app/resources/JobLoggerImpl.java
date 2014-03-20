package resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao.DataType;

/**
 * logger impl
 * @author binyu
 *
 */
public class JobLoggerImpl implements IJobLogger {
	
	@Autowired(required=true)
	private IUserDataDao userConfigs;
	
	@Override
	public IUserDataDao getUserConfigs() {
		return userConfigs;
	}

	@Override
	public void setUserConfigs(IUserDataDao userConfigs) {
		this.userConfigs = userConfigs;
	}

	@Override
	public void saveLog(JobLog log) throws IOException {
		userConfigs.saveConfigFile(DataType.LOG, log.uuid(), JsonUtil.encode(log));
	}

	@Override
	public JobLog readLog(String fileName) throws IOException {
		String log = userConfigs.readConfigFile(DataType.LOG, fileName);
		return JsonUtil.decode(log, JobLog.class);
	}

	@Override
	public List<JobLog> listLogs() throws IOException {
		ArrayList<JobLog> jobLogs = new ArrayList<JobLog>();
		for (String fileName : userConfigs.listFiles(DataType.LOG)) {
			jobLogs.add(readLog(fileName));
		}
		return jobLogs;
	}

}
