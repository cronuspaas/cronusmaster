package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.data.JsonResult;
import models.utils.DateUtils;

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.BatchOption;
import org.lightj.task.ExecutableTask;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.util.DateUtil;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.TaskResourcesProvider;
import resources.UserDataProvider;
import resources.IUserDataDao.DataType;
import resources.command.ICommand;
import resources.command.ICommandData;
import resources.job.BaseIntervalJob;
import resources.job.CmdIntervalJobImpl;
import resources.job.FlowIntervalJobImpl;
import resources.job.IntervalJob;
import resources.job.IntervalJobData;
import resources.log.IJobLogger;
import resources.log.BaseLog;
import resources.log.BaseLog.UserCommand;
import resources.log.ILog;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;

public class Jobs extends Controller {

	/**
	 * show job index page
	 * @param date
	 */
	public static void index(String alert) {

		String page = "index";
		String topnav = "jobs";

		try {
			
			IntervalJobData jobData = UserDataProvider.getIntervalJobOfType(DataType.CMDJOB);
			List<IntervalJob> jobs = jobData.getAllJobs();
			
			ArrayList<Map<String, String>> jobDetails = new ArrayList<Map<String,String>>();
			
			for (IntervalJob job : jobs) {
				HashMap<String, String> jobDetail = new HashMap<String, String>();
				jobDetail.put("name", job.getName());
				jobDetail.put("interval", String.format("Every %s min", job.getIntervalInMinute()));
				jobDetail.put("description", job.getDescription());
				jobDetail.put("status", job.isEnabled() ? "Enabled" : "Disabled");
				jobDetail.put("statustoggle", job.isEnabled() ? "Disable" : "Enable");
				jobDetail.put("type", DataType.CMDJOB.name());
				jobDetail.put("typeLabel", DataType.CMDJOB.getLabel());
				jobDetails.add(jobDetail);
			}
			
			for (IntervalJob job : UserDataProvider.getIntervalJobOfType(DataType.FLOWJOB).getAllJobs()) {
				HashMap<String, String> jobDetail = new HashMap<String, String>();
				jobDetail.put("name", job.getName());
				jobDetail.put("interval", String.format("Every %s min", job.getIntervalInMinute()));
				jobDetail.put("description", job.getDescription());
				jobDetail.put("status", job.isEnabled() ? "Enabled" : "Disabled");
				jobDetail.put("statustoggle", job.isEnabled() ? "Disable" : "Enable");
				jobDetail.put("type", DataType.FLOWJOB.name());
				jobDetail.put("typeLabel", DataType.FLOWJOB.getLabel());
				jobDetails.add(jobDetail);
			}

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, jobDetails, alert, lastRefreshed);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}

	/**
	 * toggle job enable status
	 */
	public static void toggleJobStatus(String dataType, String jobId, String status) {
		try {
			DataType type = DataType.valueOf(dataType.toUpperCase());
			IntervalJobData jobData = UserDataProvider.getIntervalJobOfType(type);
			IntervalJob job = jobData.getJobById(jobId);
			job.setEnabled(StringUtil.equalIgnoreCase(status, "enable") ? true : false);
			jobData.save(job);
			
			redirect("Jobs.index", "Successfully toggle job status " + jobId);
			
		} catch (Exception e) {
			e.printStackTrace();
			error(String.format("Error toggle job status %s, %s", jobId, e.getLocalizedMessage()));
		}
		
		
	}
	
	/**
	 * delete job 
	 * @param jobId
	 */
	public static void deleteJob(String dataType, String jobId) {
		try {
			DataType type = DataType.valueOf(dataType.toUpperCase());
			UserDataProvider.getIntervalJobOfType(type).delete(jobId);

			redirect("Jobs.index", "Successfully deleted job " + jobId);
			
		} catch (Exception e) {
			e.printStackTrace();
			error(String.format("Error delete job %s, %s", jobId, e.getLocalizedMessage()));
		}
	}
	
	/**
	 * delete job 
	 * @param jobId
	 */
	public static void runJobNow(String dataType, String jobId) {
		try {
			DataType type = DataType.valueOf(dataType.toUpperCase());
			IntervalJob job = UserDataProvider.getIntervalJobOfType(type).getJobById(jobId);
			job.runJobAsync();

			redirect("Jobs.index", "Successfully launched job " + jobId);

		} catch (Exception e) {
			e.printStackTrace();
			error(String.format("Error launch job %s, %s", jobId, e.getLocalizedMessage()));
		}
	}

	/**
	 * create job wizard
	 */
	public static void wizard(String logType, String logId) {

		String page = "wizard";
		String topnav = "jobs";

		try {
			
			DataType dType = DataType.valueOf(logType.toUpperCase());
			ILog log = UserDataProvider.getJobLoggerOfType(dType).readLog(logId);
			HashMap<String, String> meta = new HashMap<String, String>();
			meta.put("logType", logType);
			meta.put("logId", logId);
			meta.put("ng", log.getNodeGroup().getName());
			meta.put("cmdType", log.getCommandType().name());
			meta.put("cmdKey", log.getCommandKey());
			meta.put("userData", JsonUtil.encode(log.getUserData()));
			
			render(page, topnav, meta);

		} catch (Exception e) {
			e.printStackTrace();
			error(String.format("Error occur in job wizard, %s", e.getLocalizedMessage()));
		}

	}

	/**
	 * save job from wizard
	 * @param command
	 * @param exeOptions
	 * @param jobOptions
	 */
	public static void saveJob(String dataType, String logId, Map<String, String> jobOptions) 
	{
		try {
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			DataType jType = null;
			ILog log = UserDataProvider.getJobLoggerOfType(dType).readLog(logId);
			BaseIntervalJob job = null;
			
			switch (dType) {
			case CMDLOG:
				CmdIntervalJobImpl cjob = new CmdIntervalJobImpl();
				job = cjob;
				jType = DataType.CMDJOB;
				break;
			case FLOWLOG:
				FlowIntervalJobImpl fjob = new FlowIntervalJobImpl();
				job = fjob;
				jType = DataType.FLOWJOB;
				break;
			}
			
			job.setCmdName(log.getCommandKey());
			job.setNodeGroupName(log.getNodeGroup().getName());
			job.setUserData(log.getUserData());

			job.setName(jobOptions.get("job_name"));
			int intervalInMinute = Integer.parseInt(getOptionValue(jobOptions, "job_interval", "1")) * 5;
			job.setIntervalInMinute(intervalInMinute);
			job.setEnabled("enable".equalsIgnoreCase(getOptionValue(jobOptions, "job_status", "disable")));
			
			UserDataProvider.getIntervalJobOfType(jType).save(job);
			
		} catch (Exception e) {
			error(	"Error occured in saveJob: " + e.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}

	private static String getOptionValue(Map<String, String> options, String key, String defVal) {
		return (options.containsKey(key) && !StringUtil.isNullOrEmpty(options.get(key))) ? options.get(key) : defVal;
	}

}
