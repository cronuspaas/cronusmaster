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
import resources.UserDataProvider;
import resources.IUserDataDao.DataType;
import resources.log.IJobLogger;
import resources.log.JobLog;

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
			
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.CMDLOG);
			List<String> logs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			for (String logName : logs) {
				Map<String, String> logMeta = JobLog.getLogMetaFromName(logName);
				HashMap<String, String> log = new HashMap<String, String>();
				log.putAll(logMeta);
				log.put("name", logName);
				log.put("type", DataType.CMDLOG.name());
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

	public static void jobLogs(String date) {

		String page = "joblogs";
		String topnav = "logs";

		try {
			
			IJobLogger logger = UserDataProvider.getJobLoggerOfType(DataType.JOBLOG);
			List<String> logs = logger.listLogs();
			ArrayList<Map<String, String>> logFiles = new ArrayList<Map<String,String>>();
			
			for (String logName : logs) {
				Map<String, String> logMeta = JobLog.getLogMetaFromName(logName);
				HashMap<String, String> log = new HashMap<String, String>();
				log.putAll(logMeta);
				log.put("name", logName);
				log.put("type", DataType.JOBLOG.name());
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


	public static void download(String type, String name) {

		try {

			DataType dtype = DataType.valueOf(type);
			String filePath = String.format("%s/%s", dtype.getPath(), name);
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

}
