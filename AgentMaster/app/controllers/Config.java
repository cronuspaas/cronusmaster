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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.UserDataProvider;
import com.stackscaling.agentmaster.resources.command.CommandImpl;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * 
 * @author ypei
 *
 */
public class Config extends Controller {

	/**
	 * view configs
	 * @param configType
	 * @param configKey
	 */
	public static void viewConfigItem(String configType, String configKey) {
		try {
			DataType type = DataType.valueOf(configType.toUpperCase());
			String jsonResult = UserDataProvider.getUserDataDao().readData(type, configKey);;
			renderJSON(jsonResult);
		} catch (IOException e) {
			e.printStackTrace();
			renderJSON(e);
		}
	}

	/**
	 * reload all config
	 * @param dataType
	 */
	public static void reloadConfig(String dataType, String nav) {

		try {
			UserDataProvider.reloadAllConfigs();
			String alert = "Successful reload config with type " + dataType;
			redirect("Config.showConfigs", dataType, alert, nav);
			
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}
	
	/**
	 * show all configs of a type
	 * @param dataType
	 */
	public static void showConfigs(String dataType, String alert, String nav) {
		
		String page = "showConfigs"+dataType.toLowerCase();
		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;

		try {
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			List<String> cfgNames = UserDataProvider.getUserDataDao().listNames(dType);

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, dataType, cfgNames, lastRefreshed, alert);

		}
		catch (Exception e) {
			e.printStackTrace();
			error(e);
		}
	}

	/**
	 * edit page
	 * @param dataType
	 */
	static final String NEW_CONFIG_NAME = "new";
	public static void editConfig(String dataType, String action, String configName, String nav) {

		String page = "editConfig";
		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			String content = null;
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			if (StringUtil.equalIgnoreCase("create", action)) {
				if (!StringUtil.equalIgnoreCase("new", configName)) {
					IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
					content = userDataDao.readData(DataType.valueOf(dataType.toUpperCase()), configName);
					configName = "new";
				}
				else {
					if (dType == DataType.COMMAND) {
						// this is for new configuration
						CommandImpl command = new CommandImpl();
						
						HttpTaskRequest sampleReq = new HttpTaskRequest();
						UrlTemplate temp = new UrlTemplate(UrlTemplate.encodeAllVariables("http://host:port/uri", "host"), HttpMethod.POST);
						sampleReq.setUrlTemplate(temp);
						sampleReq.setPollTemplate(temp);
						sampleReq.setTaskType("asyncpoll");
						sampleReq.setHttpClientType("httpClient");
						sampleReq.setExecutionOption(new ExecuteOption(0,0,0,0));
						sampleReq.setMonitorOption(new MonitorOption(10, 0));
						sampleReq.setGlobalContext("globalContextLookup");
						sampleReq.setResProcessorName("responseProcessor");
						command.setHttpTaskRequest(sampleReq);
						
						command.addUserData("variableInHttpTemplate", "sample value");
						command.setAggRegexs(Arrays.asList(new String[] {"regex for response aggregation"}));
						
						HashMap<String, Object> cmdMap = new LinkedHashMap<String, Object>();
						cmdMap.put("httpTaskRequest", command.createCopy());
						cmdMap.put("userData", command.getUserData());
						cmdMap.put("aggRegexs", command.getAggRegexs());
						
						content = JsonUtil.encodePretty(cmdMap);
					}
					else if (dType == DataType.NODEGROUP) {
						content = "line separated hosts";
					}
				}
			}
			else {
				IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
				content = userDataDao.readData(DataType.valueOf(dataType.toUpperCase()), configName);
			}
			
			String alert = null;

			render(page, topnav, dataType, configName, content, alert);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * save after edit
	 * @param dataType
	 * @param content
	 */
	public static void editConfigUpdate(String dataType, String configName, String configNameNew, String content, String nav) {

		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			if (StringUtil.equalIgnoreCase(NEW_CONFIG_NAME, configName)) {
				// new config
				configName = configNameNew;
			}

			DataType dType = DataType.valueOf(dataType.toUpperCase());
			switch(dType) {
			case COMMAND:
				UserDataProvider.getCommandConfigs().save(configName, content);
				break;
			case NODEGROUP:
				UserDataProvider.getNodeGroupOfType(dType).save(configName, content);
				break;
			case SCRIPT:
				UserDataProvider.getScriptOfType(dType).save(configName, content);
				break;
			default:
				throw new RuntimeException("Invalid datatype " + dataType);
			}
			
			String alert = "Config was successfully updated at " + DateUtils.getNowDateTimeStrSdsm();

			// reload after
			UserDataProvider.reloadAllConfigs();
			
			redirect("Config.showConfigs", dataType, alert, topnav);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * delete a config
	 * @param dataType
	 * @param configName
	 */
	public static void deleteConfig(String dataType, String configName, String nav) {

		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			UserDataProvider.getUserDataDao().deleteData(dType, configName);
			
			String alert = String.format("%s was successfully deleted at %s ", configName, DateUtils.getNowDateTimeStrSdsm());

			// reload after
			UserDataProvider.reloadAllConfigs();
			
			redirect("Config.showConfigs", dataType, alert, topnav);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * show all configs
	 */
	public static void index() {
		
		redirect("Config.showConfigs", "nodegroup");

	}

}
