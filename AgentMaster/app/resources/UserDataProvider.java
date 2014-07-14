package resources;

import java.io.IOException;
import java.io.InvalidObjectException;

import org.lightj.util.SpringContextUtil;
import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import play.Play;

import resources.IUserDataDao.DataType;
import resources.command.CommandDataImpl;
import resources.command.ICommandData;
import resources.job.IntervalJobData;
import resources.job.IntervalJobDataImpl;
import resources.log.CmdLog;
import resources.log.FlowLog;
import resources.log.IJobLogger;
import resources.log.JobLog;
import resources.log.LoggerImpl;
import resources.nodegroup.AdhocNodeGroupDataImpl;
import resources.nodegroup.INodeGroupData;
import resources.nodegroup.NodeGroupDataImpl;
import resources.script.IScriptData;
import resources.script.ScriptDataImpl;
import resources.workflow.IWorkflowData;
import resources.workflow.WorkflowDataImpl;

@Configuration
public class UserDataProvider {
	
	/**
	 * IUserConfigs impl
	 * @return
	 */
//	public @Bean(name="userConfigs") @Scope("singleton") IUserDataDao userConfigs() {
//		String userDataDaoType = new Play().configuration.getProperty("agentmaster.userDataDao").toString();
//		if (StringUtil.equalIgnoreCase("file", userDataDaoType)) {
//			return new FileUserDataDaoImpl();
//		} 
//		else if (StringUtil.equalIgnoreCase("aws_s3", userDataDaoType)) {
//			// set bucket uuid if it is different from enum name
//			for (DataType dt : DataType.values()) {
//				dt.setUuid(new Play().configuration.getProperty(String.format("agentmaster.userDataDao.s3.%s.uuid", dt.name())));
//			}
//			return new S3UserDataDaoImpl();
//		} 
//		else if (StringUtil.equalIgnoreCase("openstack_swift", userDataDaoType)) {
//			// set container uuid if it is different from enum name
//			for (DataType dt : DataType.values()) {
//				dt.setUuid(new Play().configuration.getProperty(String.format("agentmaster.userDataDao.swift.%s.uuid", dt.name())));
//			}
//			return new SwiftUserDataDaoImpl();
//		}
//		throw new RuntimeException("undefined user data dao type " + userDataDaoType);
//	}
	
//	/**
//	 * IUserConfigsDao
//	 * @return
//	 * @throws IOException
//	 */
//	public @Bean(name="commandConfigs") @Scope("singleton") ICommandData commandConfigs() throws IOException {
//		CommandDataImpl dao = new CommandDataImpl();
//		return dao;
//	}
//	
//	/**
//	 * predefined node group
//	 * @return
//	 */
//	public @Bean @Scope("singleton") INodeGroupData predefinedNodeGroup() {
//		NodeGroupDataImpl ngd = new NodeGroupDataImpl();
//		ngd.setDataType(DataType.NODEGROUP);
//		return ngd;		
//	}
//
//	/**
//	 * adhoc node group
//	 * @return
//	 */
//	public @Bean @Scope("singleton") INodeGroupData adhocNodeGroup() {
//		return new AdhocNodeGroupDataImpl();
//	}
//	
//	/**
//	 * job logger
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IJobLogger jobLogger() {
//		LoggerImpl logger = new LoggerImpl<JobLog>();
//		logger.setDataType(DataType.JOBLOG);
//		logger.setLogDoKlass(JobLog.class);
//		return logger;
//	}
//
//	/**
//	 * flow logger
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IJobLogger flowLogger() {
//		LoggerImpl logger = new LoggerImpl<FlowLog>();
//		logger.setDataType(DataType.FLOWLOG);
//		logger.setLogDoKlass(FlowLog.class);
//		return logger;
//	}
//	
//	/**
//	 * cmd logger
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IJobLogger cmdLogger() {
//		LoggerImpl logger = new LoggerImpl<CmdLog>();
//		logger.setDataType(DataType.CMDLOG);
//		logger.setLogDoKlass(CmdLog.class);
//		return logger;
//	}
//	
//	/**
//	 * cmd interval jobs
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IntervalJobData cmdIntervalJob() {
//		IntervalJobDataImpl jobDataImpl = new IntervalJobDataImpl();
//		jobDataImpl.setJobType(DataType.CMDJOB);
//		return jobDataImpl;
//	}
//
//	/**
//	 * flow based interval jobs
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IntervalJobData flowIntervalJob() {
//		IntervalJobDataImpl jobDataImpl = new IntervalJobDataImpl();
//		jobDataImpl.setJobType(DataType.FLOWJOB);
//		return jobDataImpl;
//	}
//
//	/**
//	 * workflow configs
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IWorkflowData workflowConfigs() {
//		return new WorkflowDataImpl();
//	}
//	
//	/**
//	 * predefined script
//	 * @return
//	 */
//	public @Bean @Scope("singleton") IScriptData predefinedScripts() {
//		ScriptDataImpl sd = new ScriptDataImpl();
//		sd.setDataType(DataType.SCRIPT);
//		return sd;
//	}
//	
	/**
	 * return current IUserConfigsDao
	 * @return
	 */
	public static ICommandData getCommandConfigs() {
		return SpringContextUtil.getBean("resources", "commandConfigs", ICommandData.class);
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
//		String userDataDaoType = new Play().configuration.getProperty("agentmaster.userDataDao").toString();
//		if (StringUtil.equalIgnoreCase("file", userDataDaoType)) {
//			return SpringContextUtil.getBean("resources", FileUserDataDaoImpl.class);
//		} 
//		else if (StringUtil.equalIgnoreCase("aws_s3", userDataDaoType)) {
//			return SpringContextUtil.getBean("resources", S3UserDataDaoImpl.class);
//		} 
//		else if (StringUtil.equalIgnoreCase("openstack_swift", userDataDaoType)) {
//			return SpringContextUtil.getBean("resources", SwiftUserDataDaoImpl.class);
//		}
//		throw new RuntimeException("undefined user data dao type " + userDataDaoType);
		
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
		getWorkflowConfigs().load();
	}

}
