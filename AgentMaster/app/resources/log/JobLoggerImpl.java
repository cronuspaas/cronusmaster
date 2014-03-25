package resources.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.IUserDataDao.DataType;

/**
 * logger impl
 * @author binyu
 *
 */
public class JobLoggerImpl implements IJobLogger {
	
	@Autowired(required=true)
	private IUserDataDao userDataDao;
	
	private DataType dataType;
	
	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
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
	public void saveLog(JobLog log) throws IOException {
		userDataDao.saveConfigFile(dataType, log.uuid(), JsonUtil.encode(log));
	}

	@Override
	public JobLog readLog(String fileName) throws IOException {
		String log = userDataDao.readConfigFile(dataType, fileName);
		return JsonUtil.decode(log, JobLog.class);
	}

	@Override
	public List<String> listLogs() throws IOException {
		return userDataDao.listFiles(dataType);
	}

}
