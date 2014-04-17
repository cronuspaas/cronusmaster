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
import org.lightj.util.StringUtil;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import resources.IUserDataDao;
import resources.IUserDataDao.DataType;
import resources.command.CommandImpl;
import resources.utils.DateUtils;
import resources.UserDataProvider;

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
	public static void reloadConfig(String dataType) {

		try {
			UserDataProvider.reloadAllConfigs();
			String alert = "Successful reload config with type " + dataType;
			redirect("Config.showConfigs", dataType, alert);
			
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}
	
	/**
	 * show all configs of a type
	 * @param dataType
	 */
	public static void showConfigs(String dataType, String alert) {
		
		String page = "showConfigs"+dataType.toLowerCase();
		String topnav = "config";

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
	static final String NEW_CONFIG_NAME = "newConfig";
	public static void editConfig(String dataType, String configName) {

		String page = "editConfig";
		String topnav = "config";

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			String content = null;
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			if (StringUtil.equalIgnoreCase(NEW_CONFIG_NAME, configName)) {
				if (dType == DataType.COMMAND) {
					// this is for new configuration
					final ObjectMapper mapper = new ObjectMapper();
					mapper.setSerializationInclusion(Include.NON_NULL);
					
					CommandImpl command = new CommandImpl();
					
					HttpTaskRequest sampleReq = new HttpTaskRequest();
					UrlTemplate temp = new UrlTemplate(UrlTemplate.encodeAllVariables("http://host:port/uri", "host"), HttpMethod.POST, "body");
					sampleReq.setUrlTemplate(temp);
					sampleReq.setPollTemplate(temp);
					sampleReq.setTaskType("asyncpoll");
					sampleReq.setHttpClientType("httpClient");
					sampleReq.setExecutionOption(new ExecuteOption(0,0,0,0));
					sampleReq.setMonitorOption(new MonitorOption(10, 0));
					command.setHttpTaskRequest(sampleReq);
					
					command.addUserData("some variable", "value");
					command.setAggRegexs(Arrays.asList(new String[] {"some regex"}));
					
					HashMap<String, Object> cmdMap = new LinkedHashMap<String, Object>();
					cmdMap.put("userInputs", command.getUserData());
					cmdMap.put("httpTaskRequest", command.createCopy());
					cmdMap.put("aggRegexs", command.getAggRegexs());
					
					content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cmdMap);
				}
				else if (dType == DataType.NODEGROUP) {
					content = "line separated hosts";
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
	public static void editConfigUpdate(String dataType, String configName, String configNameNew, String content) {

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			
			if (StringUtil.equalIgnoreCase(NEW_CONFIG_NAME, configName)) {
				// new config
				configName = configNameNew;
			}

			IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
			userDataDao.saveData(DataType.valueOf(dataType.toUpperCase()), configName, content);

			String alert = "Config was successfully updated at " + DateUtils.getNowDateTimeStrSdsm();

			// reload after
			UserDataProvider.reloadAllConfigs();
			
			redirect("Config.showConfigs", dataType, alert);

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
	public static void deleteConfig(String dataType, String configName) {

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			UserDataProvider.getUserDataDao().deleteData(dType, configName);
			
			String alert = String.format("%s was successfully deleted at %s ", configName, DateUtils.getNowDateTimeStrSdsm());

			// reload after
			UserDataProvider.reloadAllConfigs();
			
			redirect("Config.showConfigs", dataType, alert);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * show all configs
	 */
	public static void index() {
		
		redirect("Config.showConfigs", "command");

	}

}
