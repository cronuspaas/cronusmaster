package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.job.BaseIntervalJob;
import com.stackscaling.agentmaster.resources.job.CmdIntervalJobImpl;
import com.stackscaling.agentmaster.resources.job.FlowIntervalJobImpl;
import com.stackscaling.agentmaster.resources.job.IntervalJob;
import com.stackscaling.agentmaster.resources.job.IntervalJobData;
import com.stackscaling.agentmaster.resources.log.ILog;
import com.stackscaling.agentmaster.resources.oneclickcommand.IOneClickCommand;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

public class Jobs extends Controller {

	/**
	 * show job index page
	 * @param date
	 */
	public static void index(String alert) {

		String page = "index";
		String topnav = "jobs";

		try {
			
			IntervalJobData jobData = UserDataProviderFactory.getIntervalJobOfType(DataType.CMDJOB);
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
			
			for (IntervalJob job : UserDataProviderFactory.getIntervalJobOfType(DataType.FLOWJOB).getAllJobs()) {
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

			String lastRefreshed = DateUtils.getNowDateTimeDotStr();

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
			IntervalJobData jobData = UserDataProviderFactory.getIntervalJobOfType(type);
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
			UserDataProviderFactory.getIntervalJobOfType(type).delete(jobId);

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
			IntervalJob job = UserDataProviderFactory.getIntervalJobOfType(type).getJobById(jobId);
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
			HashMap<String, String> meta = new HashMap<String, String>();
			meta.put("logType", logType);
			meta.put("logId", logId);
			if (dType == DataType.CMDLOG) {
				ILog log = UserDataProviderFactory.getJobLoggerOfType(dType).readLog(logId);
				if (!StringUtil.equalIgnoreCase(log.getNodeGroup().getType(), DataType.NODEGROUP.name())) {
					throw new RuntimeException("Only predefined nodegroup type is allowed for scheduled job");
				}
				meta.put("ng", log.getNodeGroup().getName());
				meta.put("cmdType", log.getCommandType().name());
				meta.put("cmdKey", log.getCommandKey());
				meta.put("userData", JsonUtil.encodePretty(log.getUserData()));
			}
			else if (dType == DataType.CMD_ONECLICK) {
				IOneClickCommand cmd = UserDataProviderFactory.getOneClickCommandConfigs().getCommandByName(logId);
				meta.put("ng", cmd.getNodeGroupKey());
				meta.put("cmdType", DataType.COMMAND.name());
				meta.put("cmdKey", cmd.getCommandKey());
				meta.put("userData", JsonUtil.encodePretty(cmd.getUserData()));
			}
			
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
			BaseIntervalJob job = null;
			
			switch (dType) {
			case CMDLOG:
			case CMD_ONECLICK:
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

			switch (dType) {
			case CMDLOG:
			case FLOWLOG:
				ILog log = UserDataProviderFactory.getJobLoggerOfType(dType).readLog(logId);
				job.setCmdName(log.getCommandKey());
				job.setNodeGroupName(log.getNodeGroup().getName());
				job.setUserData(log.getUserData());
				break;
			case CMD_ONECLICK:
				IOneClickCommand cmd = UserDataProviderFactory.getOneClickCommandConfigs().getCommandByName(logId);
				job.setCmdName(cmd.getCommandKey());
				job.setNodeGroupName(cmd.getNodeGroupKey());
				job.setUserData(cmd.getUserData());
				break;
				
			}

			job.setName(jobOptions.get("job_name"));
			int intervalInMinute = Integer.parseInt(DataUtil.getOptionValue(jobOptions, "job_interval", "1")) * 5;
			job.setIntervalInMinute(intervalInMinute);
			job.setEnabled("enable".equalsIgnoreCase(DataUtil.getOptionValue(jobOptions, "job_status", "disable")));
			
			UserDataProviderFactory.getIntervalJobOfType(jType).save(job);
			
		} catch (Exception e) {
			error(	"Error occured in saveJob: " + e.getLocalizedMessage());
		}

	}

}
