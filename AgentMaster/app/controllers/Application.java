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

import java.util.HashMap;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.UserDataProvider;

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
			metricMap.put("totalOneclickCount", 
					Integer.toString(UserDataProvider.getOneClickCommandConfigs().getAllCommands().size()));
			metricMap.put("totalWfCount",
					Integer.toString(UserDataProvider.getWorkflowConfigs().getAllFlows().size()));
			metricMap.put("totalJobCount",
					Integer.toString(UserDataProvider.getIntervalJobOfType(DataType.CMDJOB).getAllJobs().size()
					+ UserDataProvider.getIntervalJobOfType(DataType.FLOWJOB).getAllJobs().size()));
			metricMap.put("totalScriptCount", 
					Integer.toString(UserDataProvider.getScriptOfType(DataType.SCRIPT).getScriptCount()));

			render(metricMap);
			
		} catch (Exception e) {
			error(e);
		}
	}// end func.
	
    public static void whatsnew() {
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