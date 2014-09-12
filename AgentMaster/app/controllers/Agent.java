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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.lightj.example.task.HttpTaskBuilder;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecutableTask;
import org.lightj.task.StandaloneTaskExecutor;
import org.lightj.task.StandaloneTaskListener;
import org.lightj.util.ConcurrentUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.TaskResourcesProvider.BlockingTaskResultCollector;
import com.stackscaling.agentmaster.resources.UserDataProvider;
import com.stackscaling.agentmaster.resources.agent.AgentResourceProvider.AgentStatus;
import com.stackscaling.agentmaster.resources.command.ICommand;
import com.stackscaling.agentmaster.resources.command.ICommandData;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroup;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroupData;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * 
 * @author ypei
 * 
 */
public class Agent extends Controller {

	static Comparator<Map<String, String>> cmdComparator = new Comparator<Map<String, String>>() {

		@Override
		public int compare(Map<String, String> o1, Map<String, String> o2) {
			return o1.get("name").compareTo(o2.get("name"));

		}
	};

	static Comparator<Map<String, String>> oneclickComparator = new Comparator<Map<String, String>>() {

		@Override
		public int compare(Map<String, String> o1, Map<String, String> o2) {
			return o1.get("displayName").compareTo(o2.get("displayName"));

		}
	};

	// this command definition must exist in user_data/cmd_sys
	private static final String KEY_CMD_SERVICES_INFO = "_Agent_Services_Info";

	/**
	 * services summary page
	 * @param ngName
	 * @throws Exception
	 */
	public static void services(String ngName) throws Exception {

		String page = "servcies";
		String topnav = "agent";

		try {
			String lastRefreshed = DateUtils.getNowDateTimeDotStr();
			Set<String> ngs = UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getAllNodeGroups().keySet();
			List<Map<String, String>> hostServices;
			if (StringUtil.isNullOrEmpty(ngName)) {
				hostServices = Collections.emptyList();
			} else {
				hostServices = servicesInternal(ngName); 
			}
			render(page, topnav, ngs, hostServices, ngName, lastRefreshed);
			
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}

	/**
	 * services summary
	 * 
	 * @throws Exception
	 */
	private static List<Map<String, String>> servicesInternal(String ngName) throws Exception {
		List<Map<String, String>> hostServices = new ArrayList<Map<String, String>>();
		ICommandData cmdDao = UserDataProvider.getSysCommandConfigs();
		ICommand cmd = cmdDao.getCommandByName(KEY_CMD_SERVICES_INFO);
		INodeGroupData ngDao = UserDataProvider
				.getNodeGroupOfType(DataType.NODEGROUP);

		String[] hosts = null;
		INodeGroup ng = ngDao.getNodeGroupByName(ngName);
		hosts = ng.getHosts();

		Map<String, Object> userData = Collections.emptyMap();
		Map<String, String> options = Collections.emptyMap();
		HttpTaskRequest reqTemplate = Commands.createTaskByRequest(hosts, cmd,
				options, userData);

		// fire task
		ExecutableTask reqTask = HttpTaskBuilder.buildTask(reqTemplate);
		StandaloneTaskListener listener = new StandaloneTaskListener();
		ReentrantLock lock = new ReentrantLock();
		Condition cond = lock.newCondition();
		BlockingTaskResultCollector<AgentStatus> handler = new BlockingTaskResultCollector<AgentStatus>(
				lock, cond, AgentStatus.class);
		listener.setDelegateHandler(handler);
		new StandaloneTaskExecutor(reqTemplate.getBatchOption(), listener,
				reqTask).execute();
		long timeoutMs = 30 * 1000L;
		ConcurrentUtil.wait(lock, cond, timeoutMs);

		Map<String, AgentStatus> results = handler.getResults();
		for (Entry<String, AgentStatus> entry : results.entrySet()) {
			String host = entry.getKey();
			AgentStatus as = entry.getValue();
			Object asRes = as.result;
			if (asRes instanceof List) {
				for (Object asResItm : ((List) asRes)) {
					if (asResItm instanceof Map) {
						Map<String, String> values = new HashMap<String, String>(
								(Map) asResItm);
						values.put("host", host);
						hostServices.add(values);
					}
				}
			}
		}

		return hostServices;
	}

}
