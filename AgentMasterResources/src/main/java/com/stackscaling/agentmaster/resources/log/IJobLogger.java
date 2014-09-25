package com.stackscaling.agentmaster.resources.log;

import java.io.IOException;
import java.util.List;

import com.stackscaling.agentmaster.resources.IUserDataProvider;

/**
 * save command log
 * @author binyu
 *
 */
public interface IJobLogger<T extends ILog> extends IUserDataProvider {

	/**
	 * save log
	 * @param log
	 */
	public void saveLog(T log) throws IOException;

	/**
	 * read from persistence job log
	 * @param jobUuid
	 * @return
	 * @throws IOException
	 */
	public T readLog(String jobUuid) throws IOException;


	/**
	 * list all logs
	 * @return
	 * @throws IOException
	 */
	public List<String> listLogs() throws IOException;

	/**
	 * delete a log
	 * @param logId
	 * @throws IOException
	 */
	public void deleteLog(String logId) throws IOException;


}
