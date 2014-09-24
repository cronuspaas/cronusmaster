package com.stackscaling.agentmaster.resources;


/**
 * different type of data
 *
 * @author binyu
 *
 */
public enum DataType
{
	// nodes
	NODEGROUP("Predefined NodeGroup", "user_data/predefined_nodegroups"),
	ADHOCNODEGROUP("Adhoc NodeGroup", "user_data/adhoc_nodegroups"),
	// cmd
	COMMAND("Command", "user_data/commands"),
	SYSCMD("System Commands", "user_data/cmd_sys"),
	CMD_ONECLICK("One Click Command", "user_data/cmd_oneclick"),
	// wf
	WORKFLOW("Workflow", "user_data/workflows"),
	// log
	JOBLOG("Job Logs", "user_data/job_logs"),
	FLOWLOG("Workflow Logs", "user_data/flow_logs"),
	CMDLOG("Command Logs", "user_data/cmd_logs"),
	// job
	CMDJOB("Command Job", "user_data/cmd_jobs"),
	FLOWJOB("Workflow Job", "user_data/wf_jobs"),
	// script
	SCRIPT("Predefined Script", "user_data/predefined_scripts"),
	// file uploads
	CRONUSPKG("Uploaded Cronus Package", "user_data/cronus_pkgs"),
	;

	private final String path;
	private String uuid;
	private final String label;
	DataType(String label, String path) {
		this.label = label;
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public String getUuid() {
		return uuid==null ? name() : uuid;
	}
	public String getLabel() {
		return label;
	}
}