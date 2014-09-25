package com.stackscaling.agentmaster.resources.log;

import java.io.IOException;
import java.util.List;

import org.lightj.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.log.BaseLog.CommandResponse;
import com.stackscaling.agentmaster.resources.utils.ElasticSearchUtils;

/**
 * logger impl
 * @author binyu
 *
 */
public abstract class LoggerImpl<T extends ILog> implements IJobLogger<T> {
	
	static Logger LOG = LoggerFactory.getLogger(LoggerImpl.class);

	@Autowired(required=true)
	private IUserDataDao userDataDao;

	protected DataType dataType;

	protected Class<T> logDoKlass;

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
	public List<UserDataMeta> listLogs() throws IOException {
		return userDataDao.listNames(dataType);
	}

	@Override
	public void deleteLog(String logId) throws IOException {

		try {
			// asynchronously delete elastic search data
			T log = this.readLog(logId);

			// Index name
			String _index = "log";
			// Type name
			String _type = log.getClass().getSimpleName();

			if (log instanceof BaseLog) {
				for (CommandResponse res : ((BaseLog) log).getCommandResponses()) {

					// Document ID (generated or not)
					String _id = String.format("%s/%s/%s~%s", _index, _type, log.uuid(), res.host);
					LOG.debug("Completed delete elastic search document %s", _id);
					ElasticSearchUtils.deleteDocumentFromCmdResponse(_id);

				}
			}
		} finally {
			userDataDao.deleteData(dataType, logId);
		}

	}

}
