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

import play.*;
import play.mvc.*;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;

import java.util.*;
import java.util.Map.Entry;

import models.*;
import models.asynchttp.actors.ActorConfig;
import models.data.NodeGroupSourceMetadata;
import models.data.providers.AgentDataProvider;
import models.monitor.MonitorProvider;
import models.monitor.MonitorProvider.DiskUsage;
import models.monitor.MonitorProvider.PerformUsage;
import models.utils.ConfUtils;
import models.utils.DateUtils;
import models.utils.VarUtils;

/**
 * 
 * @author ypei
 *
 */
public class Application extends Controller {

	public static void index() 
	{
		HashMap<String, String> metricMap = new HashMap<String, String>();
		
		try {
			metricMap.put("totalNodeCount",
					Integer.toString(UserDataProvider.getNodeGroupOfType(DataType.NODEGROUP).getNodeCount()));
			metricMap.put("totalCmdCount",
					Integer.toString(UserDataProvider.getCommandConfigs().getAllCommands().size()));
			metricMap.put("totalWfCount",
					Integer.toString(UserDataProvider.getWorkflowConfigs().getAllFlows().size()));
			metricMap.put("totalJobCount",
					Integer.toString(UserDataProvider.getIntervalJobOfType(DataType.CMDJOB).getAllJobs().size()
					+ UserDataProvider.getIntervalJobOfType(DataType.FLOWJOB).getAllJobs().size()));

			int runningJobCount = ActorConfig.runningJobCount.get();
			MonitorProvider mp = MonitorProvider.getInstance();
			PerformUsage performaUsage = mp.currentJvmPerformUsage;
			DiskUsage diskUsage = mp.currentDiskUsage;

			metricMap.put("runningJobCount", Integer.toString(runningJobCount));

			String runCronJobStr = Boolean.toString(ConfUtils.runCronJob);
			String localHostName = ConfUtils.localHostName;
			metricMap.put("runCronJob", runCronJobStr);

			metricMap.put("localHostName", localHostName);
			String hostName = localHostName;
			render(metricMap, hostName, performaUsage, diskUsage);
		} catch (Exception e) {
			error(e);
		}
	}// end func.
	
    public static void whatsnew() {
    	String topnav = "new";
    	render(topnav);
    }

    public static void whatsold() {
    	String topnav = "new";
    	render(topnav);
    }
    
    public static void introVideo() {
    	String topnav = "introVideo";
    	render(topnav);
    }
    
    public static void jsonedit() {
    	String topnav = "new";
    	render(topnav);
    }
   

    

}