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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecutableTask;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.util.ConcurrentUtil;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider.BlockingTaskResultCollector;
import com.stackscaling.agentmaster.resources.agent.AgentResourceProvider.AgentStatus;
import com.stackscaling.agentmaster.resources.command.BaseCommandData;
import com.stackscaling.agentmaster.resources.command.ICommand;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

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

		searchLogs(logId, "commands", "searchCmdLog", "CmdLog", VarUtils.esEp);
	}

	/**
	 * elastic search job logs
	 * @param logId
	 */
	public static void searchJobLog(String logId) {

		searchLogs(logId, "jobs", "searchJobLog", "JobLog", VarUtils.esEp);

	}
	
	/**
	 * elastic search wf logs
	 * @param logId
	 */
	public static void searchWfLog(String logId) {

		searchLogs(logId, "workflows", "searchWfLog", "WfLog", VarUtils.esEp);

	}
	
	/**
	 * proxy search to elastic search backend
	 */
	public static void proxySearch(String logType) {
		
		try {

			ICommand cmd = UserDataProviderFactory.getSysCommandConfigs().getCommandByName("_CM_Proxy_Search");
			if (cmd != null) 
			{
				Map<String, String> userData = new HashMap<String, String>();
				userData.put("<uri>", "log/cmdlog/_search");
				userData.put("<source>", request.querystring);
				HttpTaskRequest reqTemplate = BaseCommandData.createTaskByRequest(
						new String[] {"localhost"}, cmd, null, userData);

				// fire task
				ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
				StandaloneTaskListener listener = new StandaloneTaskListener();
				ReentrantLock lock = new ReentrantLock();
				Condition cond = lock.newCondition();
				BlockingTaskResultCollector<String> handler = new BlockingTaskResultCollector<String>(lock, cond, String.class);
				listener.setDelegateHandler(handler);
				new StandaloneTaskExecutor(reqTemplate.getBatchOption(), listener, reqTask).execute();
				long timeoutMs = 30 * 1000L;
				ConcurrentUtil.wait(lock, cond, timeoutMs);

				Map<String, String> results = handler.getResults();
				if (!results.isEmpty()) {
					renderJSON(results.values().iterator().next());
				}
			}

		} catch (Throwable t) {
			
			error(t.getMessage());
			
		}
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
