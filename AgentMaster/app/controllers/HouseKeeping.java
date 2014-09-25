package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.lightj.util.StringUtil;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.log.BaseLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;

/**
 * housekeeping
 * @author binyu
 *
 */
public class HouseKeeping extends Controller {

	/**
	 * archive application logs base on retain factor (name, or date) and num2keep (# of files per name, or # of days)
	 * 
	 * @param dataType type of logs
	 * @param retainFactor name, or time
	 * @param numToKeep # of files/name, or # of days
	 */
	public static void deleteLogs(String dataType, String retainFactor, int numToKeep) {
		try {
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			IJobLogger jobLogger = UserDataProviderFactory.getJobLoggerOfType(dType);
			List<UserDataMeta> logFiles = jobLogger.listLogs();
			ArrayList<String> deletedFiles = new ArrayList<String>();
			if (StringUtil.equalIgnoreCase("time", retainFactor)) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 0-numToKeep);
				long ts = cal.getTimeInMillis();
				for (UserDataMeta logFile : logFiles) {
					long fileTs = Long.parseLong(
							BaseLog.getLogMetaFromName(logFile.getName()).get("timeStamp"));
					if (fileTs < ts) {
						jobLogger.deleteLog(logFile.getName());
						deletedFiles.add(logFile.getName());
					}
				}
			}
			else if (StringUtil.equalIgnoreCase("name", retainFactor)) {
				HashMap<String, AtomicInteger> nameCount = new HashMap<String, AtomicInteger>();
				for (UserDataMeta logFile : logFiles) {
					String lastToken = BaseLog.getLogMetaFromName(logFile.getName()).get("lastToken");
					if (!nameCount.containsKey(lastToken)) {
						nameCount.put(lastToken, new AtomicInteger(0));
					}
					if (nameCount.get(lastToken).incrementAndGet() > numToKeep) {
						jobLogger.deleteLog(logFile.getName());
						deletedFiles.add(logFile.getName());
					}
				}
			}
			renderJSON(deletedFiles);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in delete logs " + StringUtil.getStackTrace(t));
		}

	}// end func

}
