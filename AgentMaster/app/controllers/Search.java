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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lightj.session.FlowSession;
import org.lightj.session.FlowSessionFactory;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.util.JsonUtil;
import org.lightj.util.MapListPrimitiveJsonParser;

import play.mvc.Controller;
import resources.IUserDataDao.DataType;
import resources.TaskResourcesProvider.LogFlowEventListener;
import resources.UserDataProvider;
import resources.log.BaseLog.UserWorkflow;
import resources.log.FlowLog;
import resources.log.IJobLogger;
import resources.nodegroup.INodeGroup;
import resources.nodegroup.INodeGroupData;
import resources.utils.DataUtil;
import resources.utils.DateUtils;
import resources.workflow.IWorkflowMeta;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 
 * @author binyu
 *
 */
public class Search extends Controller {

	/**
	 * elastic search command logs
	 * @param logId
	 */
	public static void searchCmdLog(String logId) {

		String topnav = "commands";
		String page = "searchCmdLog";
		String logType = "CmdLog";
		
		searchLogs(logId, topnav, page, logType);
	}

	/**
	 * elastic search job logs
	 * @param logId
	 */
	public static void searchJobLog(String logId) {

		searchLogs(logId, "jobs", "searchJobLog", "JobLog");

	}
	
	/**
	 * elastic search wf logs
	 * @param logId
	 */
	public static void searchWfLog(String logId) {

		searchLogs(logId, "workflows", "searchWfLog", "WfLog");

	}

	/**
	 * elastic search logs
	 * 
	 * @param logId
	 * @param topnav
	 * @param page
	 * @param logType
	 */
	private static void searchLogs(String logId, String topnav, String page, String logType) {

		try {
			
			render(page, topnav, logType, logId);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}
}
