package resources.log;

import java.io.IOException;
import java.util.List;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.UserDataProvider;
import resources.IUserDataDao.DataType;
import resources.log.BaseLog.CommandResponse;
import resources.utils.ElasticSearchUtils;

/**
 * logger impl
 * @author binyu
 *
 */
public class LoggerImpl<T extends ILog> implements IJobLogger<T> {
	
	@Autowired(required=true)
	private IUserDataDao userDataDao;
	
	private DataType dataType;
	
	private Class<T> logDoKlass;
	
	public Class getLogDoKlass() {
		return logDoKlass;
	}

	public void setLogDoKlass(Class logDoKlass) {
		this.logDoKlass = logDoKlass;
	}

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
	public void saveLog(T log) throws IOException {
		userDataDao.saveData(dataType, log.uuid(), JsonUtil.encode(log));
	}

	@Override
	public T readLog(String fileName) throws IOException {
		String log = userDataDao.readData(dataType, fileName);
		return JsonUtil.decode(log, logDoKlass);
	}

	@Override
	public List<String> listLogs() throws IOException {
		return userDataDao.listNames(dataType);
	}

	@Override
	public void deleteLog(String logId) throws IOException {
		
		try {
			// asynchronously delete elastic search data
			T log = this.readLog(logId);
			if (log instanceof BaseLog) {
				for (CommandResponse res : ((BaseLog) log).getCommandResponses()) {
					
					ElasticSearchUtils.deleteDocumentFromCmdResponse(res.indexMeta);
					play.Logger.debug("Completed delete elastic search document %s", res.indexMeta);
				}
			}
		} finally {
			userDataDao.deleteData(dataType, logId);
		}
		
	}

}
