/*  

Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.data.LogFile;
import models.data.LogFileGeneric;
import models.data.providers.LogProvider;
import models.utils.DateUtils;
import models.utils.FileIoUtils;
import models.utils.VarUtils;

import org.lightj.util.DateUtil;

import play.mvc.Controller;
import resources.IJobLogger;
import resources.JobLog;
import resources.UserDataProvider;

/**
 * 
 * @author ypei
 *
 */
public class Logs extends Controller {

	/**
	 * show logs
	 * @param date
	 */
	public static void index(String date) {

		String page = "index";
		String topnav = "logs";

		try {
			
			IJobLogger logger = UserDataProvider.getJobLogger();
			List<JobLog> jobLogs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			for (JobLog jobLog : jobLogs) {
				HashMap<String, String> log = new HashMap<String, String>();
				log.put("name", String.format("%s.json.log", jobLog.uuid()));
				log.put("command", jobLog.getUserCommand().cmd.getName());
				log.put("nodeGroup", jobLog.getUserCommand().nodeGroup.getName());
				log.put("nodeGroupType", jobLog.getUserCommand().nodeGroup.getType());
				log.put("timeStamp", Long.toString(DateUtil.parse(jobLog.getTimestamp(), JobLog.DateFormat).getTime()));
				log.put("timeStampDisplay", jobLog.getTimestamp());
				logFiles.add(log);
			}
			// List<>

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
			Collections.sort(logFiles, new Comparator<Map<String, String>>(){

				@Override
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return 0-(o1.get("timeStamp").compareTo(o2.get("timeStamp")));
					
				}});

			render(page, topnav, logFiles, date, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}
	}

	public static void adhocLog(String date) {

		String page = "adhoc";
		String topnav = "logs";

		try {
			LogProvider lp = LogProvider.getInstance();
			List<LogFile> logFiles = lp
					.getLogFilesInFolder(VarUtils.LOG_FOLDER_NAME_ADHOC_WITH_SLASH);

			if (date == null) {
				date = DateUtils.getTodaysDateStr();
			}

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, logFiles, date, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in adhocLog of logs");
		}
	}

	public static void noneStandardLog(String date) {

		String page = "noneStandard";
		String topnav = "logs";

		try {
			LogProvider lp = LogProvider.getInstance();
			List<LogFileGeneric> logFileGenerics = lp
					.getLogFileGenericsInFolder(VarUtils.LOG_FOLDER_NAME_NONESTARDARD_WITH_SLASH);

			if (date == null) {
				date = DateUtils.getTodaysDateStr();
			}

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, logFileGenerics, date, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in noneStandardLog of logs");
		}
	}


	public static void getLogContent(String logFileName) {

		try {

			String filePath = FileIoUtils.getLogFilePathPrefix(logFileName)
					+ logFileName;
			String fileContent = FileIoUtils.readFileToString(filePath);
			renderText(fileContent);
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in index of logs");
		}

	}

	public static void getFileContent(String filePath) {

		try {

			String fileContent = FileIoUtils.readFileToString(filePath);
			renderText(fileContent);
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in getFileContent of logs"
					+ DateUtils.getNowDateTimeStrSdsm());
		}

	}

	public static void archiveAppLogs(String archiveDate) {

		try {
			boolean success = LogProvider.archiveAppLogsOnDate(archiveDate);

			if (success) {

				renderText("Success archive app logs into folder "
						+ archiveDate);
			} else {
				renderText("Error occured in archive app logs " + archiveDate);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in archive app logs ");
		}

	}// end func

	public static void deleteAppLogs(String deleteDate) {

		try {
			boolean success = LogProvider.deleteAllAppLogsOnDate(deleteDate);

			if (success) {

				renderText("Success deleteAllAppLogsOnDate with date "
						+ deleteDate);
			} else {
				renderText("Error occured in deleteAllAppLogsOnDate on date "
						+ deleteDate);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in archive app logs ");
		}

	}// end func

	public static void archiveAppLogsDailyJob() {

		try {
			boolean success = LogProvider.archiveAppLogsDailyJob();

			if (success) {

				renderText("Success archiveAppLogsDailyJob");
			} else {
				renderText("Error archiveAppLogsDailyJob");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error archiveAppLogsDailyJob");
		}

	}// end func

	public static void deleteAppLogsDailyJob() {

		try {
			boolean success = LogProvider.deleteAppLogsDailyJob();
			if (success) {
				renderText("Success deleteAppLogsDailyJob");
			} else {
				renderText("Error deleteAppLogsDailyJob");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error deleteAppLogsDailyJob");
		}

	}// end func

	/**
	 * Generic display any files.
	 * 
	 * @param path
	 */
	public static void exploreFiles(String path) {

		if (path == null) {
			path = new String("");
		}
		String page = "exploreFiles";
		String topnav = "logs";

		try {

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();
			List<String> fileNames = new ArrayList<String>();
			List<String> dirNames = new ArrayList<String>();

			FileIoUtils.getFileAndDirNamesInFolder(path, fileNames, dirNames);

			render(page, topnav, fileNames, dirNames, path, lastRefreshed);

		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in exploreFiles of logs");
		}

	}// end func

	public static void deleteAllAppLogs(String pin) {
		if (pin == null) {
			renderText("Authorization required. Please input the right PIN (Password) to this command. Thanks for your cooperation. "
					+ DateUtils.getNowDateTimeStrSdsm());
		} else if (!pin.equals(VarUtils.SUPERMAN_PIN)) {
			renderText("Authorization required. Please input the right PIN (Password) to this command. Thanks for your cooperation. "
					+ DateUtils.getNowDateTimeStrSdsm());
		}

		try {
			FileIoUtils
					.deleteAllFileAndDirInFolder(VarUtils.LOG_FOLDER_NAME_APP_WITH_SLASH);
			FileIoUtils
					.deleteAllFileAndDirInFolder(VarUtils.LOG_FOLDER_NAME_ADHOC_WITH_SLASH);

			FileIoUtils
					.deleteAllFileAndDirInFolder(VarUtils.LOG_FOLDER_NAME_NONESTARDARD_WITH_SLASH);

			FileIoUtils
					.deleteAllFileAndDirInFolder(VarUtils.LOG_FOLDER_NAME_ADHOC_COMPONENTS_AGGREGATION_RULES
							+ "/");
			FileIoUtils
					.deleteAllFileAndDirInFolder(VarUtils.LOG_FOLDER_NAME_ADHOC_COMPONENTS_COMMANDS
							+ "/");
			FileIoUtils
					.deleteAllFileAndDirInFolder(VarUtils.LOG_FOLDER_NAME_ADHOC_COMPONENTS_NODE_GROUPS
							+ "/");

			renderText("Success deleteAllAppLogs logs in all logs folder ");
		} catch (Throwable t) {
			t.printStackTrace();
			renderText("Error occured in deleteAllAppLogs of Json logs");
		}

	}// end func

}
