package resources;

import java.io.IOException;
import java.util.List;

/**
 * save command log
 * @author binyu
 *
 */
public interface IJobLogger extends IUserData {
	
	/**
	 * save log
	 * @param log
	 */
	public void saveLog(JobLog log) throws IOException;
	
	/**
	 * read from persistence job log
	 * @param jobUuid
	 * @return
	 * @throws IOException
	 */
	public JobLog readLog(String jobUuid) throws IOException;
	
	
	/**
	 * list all logs
	 * @return
	 * @throws IOException
	 */
	public List<JobLog> listLogs() throws IOException;

}
