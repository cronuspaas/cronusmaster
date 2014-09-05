package com.stackscaling.agentmaster.resources;

import java.io.IOException;

import org.lightj.util.SpringContextUtil;
import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.command.CommandDataImpl;
import com.stackscaling.agentmaster.resources.command.ICommandData;
import com.stackscaling.agentmaster.resources.job.CmdIntervalJobData;
import com.stackscaling.agentmaster.resources.job.FlowIntervalJobData;
import com.stackscaling.agentmaster.resources.job.IntervalJobData;
import com.stackscaling.agentmaster.resources.log.CmdLogger;
import com.stackscaling.agentmaster.resources.log.FlowLogger;
import com.stackscaling.agentmaster.resources.log.IJobLogger;
import com.stackscaling.agentmaster.resources.log.JobLogger;
import com.stackscaling.agentmaster.resources.nodegroup.AdhocNodeGroupDataImpl;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroupData;
import com.stackscaling.agentmaster.resources.nodegroup.NodeGroupDataImpl;
import com.stackscaling.agentmaster.resources.oneclickcommand.IOneClickCommandData;
import com.stackscaling.agentmaster.resources.oneclickcommand.OneClickCommandDataImpl;
import com.stackscaling.agentmaster.resources.script.IScriptData;
import com.stackscaling.agentmaster.resources.script.PredefinedScriptData;
import com.stackscaling.agentmaster.resources.utils.VarUtils;
import com.stackscaling.agentmaster.resources.workflow.IWorkflowData;
import com.stackscaling.agentmaster.resources.workflow.WorkflowDataImpl;

@Configuration
public class UserDataProvider {

	/**
	 * IUserConfigs impl
	 * @return
	 */
	public @Bean(name="userConfigs") @Scope("singleton") IUserDataDao userConfigs() {
		if (StringUtil.equalIgnoreCase("file", VarUtils.userDataDaoType)) {
			return new FileUserDataDaoImpl();
		}
		else if (StringUtil.equalIgnoreCase("aws_s3", VarUtils.userDataDaoType)) {
			return new S3UserDataDaoImpl();
		}
		else if (StringUtil.equalIgnoreCase("openstack_swift", VarUtils.userDataDaoType)) {
			return new SwiftUserDataDaoImpl();
		}
		throw new RuntimeException("undefined user data dao type " + VarUtils.userDataDaoType);
	}

	/**
	 * Command DAO
	 * @return
	 * @throws IOException
	 */
	public @Bean(name="commandConfigs") @Scope("singleton") ICommandData commandConfigs() throws IOException {
		CommandDataImpl dao = new CommandDataImpl();
		return dao;
	}

	/**
	 * OneClickCommand DAO
	 * @return
	 * @throws IOException
	 */
	public @Bean(name="oneClickCommandConfigs") @Scope("singleton") 
	IOneClickCommandData oneClickCommandConfigs() throws IOException {
		OneClickCommandDataImpl dao = new OneClickCommandDataImpl();
		return dao;
	}

	/**
	 * predefined node group
	 * @return
	 */
	public @Bean @Scope("singleton") INodeGroupData predefinedNodeGroup() {
		NodeGroupDataImpl ngd = new NodeGroupDataImpl();
		ngd.setDataType(DataType.NODEGROUP);
		return ngd;
	}

	/**
	 * adhoc node group
	 * @return
	 */
	public @Bean @Scope("singleton") INodeGroupData adhocNodeGroup() {
		return new AdhocNodeGroupDataImpl();
	}

	/**
	 * job logger
	 * @return
	 */
	public @Bean @Scope("singleton") IJobLogger jobLogger() {
		return new JobLogger();
	}

	/**
	 * flow logger
	 * @return
	 */
	public @Bean @Scope("singleton") IJobLogger flowLogger() {
		return new FlowLogger();
	}

	/**
	 * cmd logger
	 * @return
	 */
	public @Bean @Scope("singleton") IJobLogger cmdLogger() {
		return new CmdLogger();
	}

	/**
	 * cmd interval jobs
	 * @return
	 */
	public @Bean @Scope("singleton") IntervalJobData cmdIntervalJob() {
		return new CmdIntervalJobData();
	}

	/**
	 * flow based interval jobs
	 * @return
	 */
	public @Bean @Scope("singleton") IntervalJobData flowIntervalJob() {
		return new FlowIntervalJobData();
	}

	/**
	 * workflow configs
	 * @return
	 */
	public @Bean @Scope("singleton") IWorkflowData workflowConfigs() {
		return new WorkflowDataImpl();
	}

	/**
	 * predefined script
	 * @return
	 */
	public @Bean @Scope("singleton") IScriptData predefinedScripts() {
		return new PredefinedScriptData();
	}

	/**
	 * return current Command DAO
	 * @return
	 */
	public static ICommandData getCommandConfigs() {
		return SpringContextUtil.getBean("resources", "commandConfigs", ICommandData.class);
	}

	/**
	 * return current OneClickCommand DAO
	 * @return
	 */
	public static IOneClickCommandData getOneClickCommandConfigs() {
		return SpringContextUtil.getBean("resources", "oneClickCommandConfigs", IOneClickCommandData.class);
	}

	/**
	 * getscript configs of a type
	 * @param type
	 * @return
	 */
	public static IScriptData getScriptOfType(DataType type) {
		IScriptData sd = null;
		switch(type) {
		case SCRIPT:
			sd = SpringContextUtil.getBean("resources", "predefinedScripts", IScriptData.class);
			break;
		default:
			throw new IllegalArgumentException(String.format("Invalid script type %s", type));
		}
		return sd;
	}

	/**
	 * get node group configs of a type
	 * @param type
	 * @return
	 */
	public static INodeGroupData getNodeGroupOfType(DataType type) {
		INodeGroupData ng = null;
		switch(type) {
		case NODEGROUP:
			ng = SpringContextUtil.getBean("resources", "predefinedNodeGroup", INodeGroupData.class);
			break;
		case ADHOCNODEGROUP:
			ng = SpringContextUtil.getBean("resources", "adhocNodeGroup", INodeGroupData.class);
			break;
		default:
			throw new IllegalArgumentException(String.format("Invalid nodegroup type %s", type));
		}
		return ng;
	}

	/**
	 * get job logger
	 * @return
	 */
	public static IJobLogger getJobLoggerOfType(DataType type) {
		IJobLogger logger = null;
		switch(type) {
		case CMDLOG:
			logger = SpringContextUtil.getBean("resources", "cmdLogger", IJobLogger.class);
			break;
		case JOBLOG:
			logger = SpringContextUtil.getBean("resources", "jobLogger", IJobLogger.class);
			break;
		case FLOWLOG:
			logger = SpringContextUtil.getBean("resources", "flowLogger", IJobLogger.class);
			break;
		default:
			throw new IllegalArgumentException(String.format("Invalid logger type %s", type));
		}
		return logger;
	}

	/**
	 * get interval job data
	 * @param type
	 * @return
	 */
	public static IntervalJobData getIntervalJobOfType(DataType type) {
		IntervalJobData jobData = null;
		switch (type) {
		case CMDJOB:
			jobData = SpringContextUtil.getBean("resources", "cmdIntervalJob", IntervalJobData.class);
			break;
		case FLOWJOB:
			jobData = SpringContextUtil.getBean("resources", "flowIntervalJob", IntervalJobData.class);
			break;
		default:
			throw new IllegalArgumentException(String.format("Invalid interval job type %s", type));
		}
		return jobData;
	}

	/**
	 * get user data dao
	 * @return
	 */
	public static IUserDataDao getUserDataDao() {
		return SpringContextUtil.getBean("resources", IUserDataDao.class);
	}

	/**
	 * get workflow dao
	 * @return
	 */
	public static IWorkflowData getWorkflowConfigs() {
		return SpringContextUtil.getBean("resources", IWorkflowData.class);
	}

	/**
	 * reload all configs
	 * @throws IOException
	 */
	public static void reloadAllConfigs() throws IOException {
		getNodeGroupOfType(DataType.NODEGROUP).load();
		getCommandConfigs().load();
		getOneClickCommandConfigs().load();
		getWorkflowConfigs().load();
	}

}
