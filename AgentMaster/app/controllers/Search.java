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

import com.stackscaling.agentmaster.resources.utils.VarUtils;

import play.mvc.Controller;

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

		searchLogs(logId, "commands", "searchCmdLog", "CmdLog", VarUtils.ELASTICSEARCH_EP);
	}

	/**
	 * elastic search job logs
	 * @param logId
	 */
	public static void searchJobLog(String logId) {

		searchLogs(logId, "jobs", "searchJobLog", "JobLog", VarUtils.ELASTICSEARCH_EP);

	}
	
	/**
	 * elastic search wf logs
	 * @param logId
	 */
	public static void searchWfLog(String logId) {

		searchLogs(logId, "workflows", "searchWfLog", "WfLog", VarUtils.ELASTICSEARCH_EP);

	}

	/**
	 * elastic search logs
	 * 
	 * @param logId
	 * @param topnav
	 * @param page
	 * @param logType
	 */
	private static void searchLogs(String logId, String topnav, String page, String logType, String esEp) {

		try {
			
			render(page, topnav, logType, logId, esEp);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}
}
