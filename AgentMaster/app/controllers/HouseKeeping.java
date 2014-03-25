package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import models.asynchttp.actors.ActorConfig;
import models.utils.ConfUtils;
import models.utils.DateUtils;

import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;
import resources.log.IJobLogger;
import resources.log.JobLog;

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
			IJobLogger jobLogger = UserDataProvider.getJobLoggerOfType(dType);
			List<String> logFiles = jobLogger.listLogs();
			ArrayList<String> deletedFiles = new ArrayList<String>();
			if (StringUtil.equalIgnoreCase("time", retainFactor)) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 0-numToKeep);
				long ts = cal.getTimeInMillis();
				for (String logFile : logFiles) {
					long fileTs = Long.parseLong(JobLog.getLogMetaFromName(logFile).get("timeStamp"));
					if (fileTs < ts) {
						jobLogger.deleteLog(logFile);
						deletedFiles.add(logFile);
					}
				}
			}
			else if (StringUtil.equalIgnoreCase("name", retainFactor)) {
				HashMap<String, AtomicInteger> nameCount = new HashMap<String, AtomicInteger>();
				for (String logFile : logFiles) {
					String lastToken = JobLog.getLogMetaFromName(logFile).get("lastToken");
					if (!nameCount.containsKey(lastToken)) {
						nameCount.put(lastToken, new AtomicInteger(0));
					}
					if (nameCount.get(lastToken).incrementAndGet() > numToKeep) {
						jobLogger.deleteLog(logFile);
						deletedFiles.add(logFile);
					}
				}
			}
			renderJSON(deletedFiles);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in delete logs " + StringUtil.getStackTrace(t));
		}

	}// end func


	/**
	 * 20130718 add
	 * 
	 * @param runCronJob
	 */
	public static void setRunCronJob(boolean runCronJob) {

		ConfUtils.setRunCronJob(runCronJob);
		renderText("Set runCronJob as " + runCronJob + " at time: "
				+ DateUtils.getNowDateTimeStrSdsm());
	}
	
	/**
	 * gc
	 */
	public static void runGC() {

		try {
			ActorConfig.runGCWhenNoJobRunning();
			renderJSON( "Success in RunGC at " + DateUtils.getNowDateTimeStrSdsm());
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in RunGC");
		}

	}
	

}
