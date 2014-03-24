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
import resources.job.CmdIntervalJobImpl;
import resources.job.IntervalJob;
import resources.job.IntervalJobData;
import resources.log.IJobLogger;
import resources.log.JobLog;
import resources.log.JobLog.UserCommand;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;

public class Jobs extends Controller {

	/**
	 * show job index page
	 * @param date
	 */
	public static void index() {

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
				jobDetails.add(jobDetail);
			}
			// List<>

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, jobDetails, lastRefreshed);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in index of logs");
		}

	}

	/**
	 * system job for house keeping
	 * @param nodeGroup
	 */
	public static void systemJobs(String nodeGroup) {

		String page = "systemjobs";
		String topnav = "jobs";

		render(page, topnav);
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
			renderJSON("Successfully toggle job status " + jobId);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error toggle job status " + jobId);
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
			renderJSON("Successfully deleted job " + jobId);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error delete job " + jobId);
		}
	}
	
	/**
	 * create job wizard
	 */
	public static void wizard() {

		String page = "wizard";
		String topnav = "jobs";

		try {
			
			Map<String, ICommand> cmds = UserDataProvider.getCommandConfigs().getAllCommands();
			List<Map<String, String>> cmdsMeta = new ArrayList<Map<String,String>>();
			for (ICommand cmd : cmds.values()) {
				HashMap<String, String> meta = new HashMap<String, String>();
				meta.put("agentCommandType", cmd.getName());
				cmdsMeta.add(meta);
			}
			
			Map<String, INodeGroup> ngsMap = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getAllNodeGroups();
			ArrayList<Map<String, String>> ngs = new ArrayList<Map<String, String>>();
			for (String v : ngsMap.keySet()) {
				Map<String, String> kvp = new HashMap<String, String>(1);
				kvp.put("nodeGroupType", v);
				ngs.add(kvp);
			}
			String nodeGroupSourceMetadataListJsonArray = JsonUtil.encode(ngs);
			
			String agentCommandMetadataListJsonArray = JsonUtil.encode(cmdsMeta);

			render(page, topnav, nodeGroupSourceMetadataListJsonArray, agentCommandMetadataListJsonArray);
		} catch (Throwable t) {

			t.printStackTrace();
			renderJSON(new JsonResult("Error occured in wizard"));
		}

	}
	
	public static void saveJob(
			String nodeGroup, String command, 
			Map<String, String> exeOptions, Map<String, String> jobOptions) 
	{
		try {
			long exeInitDelayMs = Long.parseLong(getOptionValue(exeOptions, "exe_initde", "0")) * 1000L;
			long exeTimoutMs = Long.parseLong(getOptionValue(exeOptions, "exe_initde", "0")) * 1000L;
			int exeRetry = Integer.parseInt(getOptionValue(exeOptions, "exe_rede", "0"));
			long retryDelayMs = Long.parseLong(getOptionValue(exeOptions, "exe_rede", "0")) * 1000L;
			ExecuteOption executeOption = new ExecuteOption(exeInitDelayMs, exeTimoutMs, exeRetry, retryDelayMs);
			
			long monIntervalMs = Integer.parseInt(getOptionValue(exeOptions, "mon_int", "1")) * 1000L;
			long monInitDelayMs = Long.parseLong(getOptionValue(exeOptions, "mon_initde", "0")) * 1000L;
			long monTimoutMs = Long.parseLong(getOptionValue(exeOptions, "mon_initde", "0")) * 1000L;
			int monRetry = Integer.parseInt(getOptionValue(exeOptions, "mon_rede", "0"));
			long monRetryDelayMs = Long.parseLong(getOptionValue(exeOptions, "mon_rede", "0")) * 1000L;
			MonitorOption monitorOption = new MonitorOption(monInitDelayMs, monIntervalMs, monTimoutMs, monRetry, monRetryDelayMs);
					
			Strategy strategy = Strategy.valueOf(getOptionValue(exeOptions, "thrStrategy", "UNLIMITED"));
			int maxRate = Integer.parseInt(getOptionValue(exeOptions, "thr_rate", "1000"));
			BatchOption batchOption = new BatchOption(maxRate, strategy);
			
			HashMap<String, String> varValues = JsonUtil.decode(getOptionValue(exeOptions, "var_values", "{}"), HashMap.class);
			
			CmdIntervalJobImpl job = new CmdIntervalJobImpl();
			job.setBatchOption(batchOption);
			job.setCmdName(command);
			job.setExecuteOption(executeOption);
			job.setMonitorOption(monitorOption);
			job.setNodeGroupName(nodeGroup);
			job.addTemplateValue(varValues);
			job.setName(jobOptions.get("job_name"));
			int intervalInMinute = Integer.parseInt(getOptionValue(jobOptions, "job_interval", "0")) * 5;
			job.setIntervalInMinute(intervalInMinute);
			job.setEnabled("enable".equalsIgnoreCase(getOptionValue(jobOptions, "job_status", "disable")));
			
			UserDataProvider.getIntervalJobOfType(DataType.CMDJOB).save(job);
			
		} catch (Throwable t) {
			error(	"Error occured in runCmdOnNodeGroup: " + t.getLocalizedMessage()
					+ " at: " + DateUtils.getNowDateTimeStrSdsm());
		}

	}

	private static String getOptionValue(Map<String, String> options, String key, String defVal) {
		return (options.containsKey(key) && !StringUtil.isNullOrEmpty(options.get(key))) ? options.get(key) : defVal;
	}
	


}
