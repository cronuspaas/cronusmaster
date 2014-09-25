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
import resources.utils.JsonResponse;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
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
			String jsonResult = UserDataProviderFactory.getUserDataDao().readData(type, configKey);;
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
			UserDataProviderFactory.reloadAllConfigs();
			String alert = "Successful reload config with type " + dataType;
			redirect("Config.showConfigs", dataType, alert, nav);
			
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}

	/**
	 * reload all config
	 * @param dataType
	 */
	public static void reloadConfigsJson() {

		try {
			UserDataProviderFactory.reloadAllConfigs();
			renderJSON(JsonResponse.successResponse(null));
			
		} catch (Exception e) {
			renderJSON(JsonResponse.failedResponse(StringUtil.getStackTrace(e, 1000)));
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
			List<UserDataMeta> cfgs = UserDataProviderFactory.getUserDataDao().listNames(dType);
			List<String> cfgNames = new ArrayList<String>();
			for (UserDataMeta cfg : cfgs) {
				cfgNames.add(cfg.getName());
			}
			String lastRefreshed = DateUtils.getNowDateTimeDotStr();

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
	public static void editConfig(String dataType, String action, String configName, String topage, String nav) {

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
					IUserDataDao userDataDao = UserDataProviderFactory.getUserDataDao();
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
						HashMap<String, Object> cmdMap = new LinkedHashMap<String, Object>();
						cmdMap.put("httpTaskRequest", command.createCopy());
						cmdMap.put("userData", command.getUserData());
						
						content = JsonUtil.encodePretty(cmdMap);
					}
					else if (dType == DataType.NODEGROUP) {
						content = "line separated hosts";
					}
				}
			}
			else {
				IUserDataDao userDataDao = UserDataProviderFactory.getUserDataDao();
				content = userDataDao.readData(DataType.valueOf(dataType.toUpperCase()), configName);
			}
			
			String alert = null;

			render(page, topnav, dataType, configName, content, topage, alert);
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
	public static void editConfigUpdate(String dataType, String configName, String configNameNew, String content, String page, String nav) {

		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;
		String topage = StringUtil.isNullOrEmpty(page) ? "Config.showConfigs" : page;

		try {
			if (dataType == null) {
				throw new IllegalArgumentException("configFile is NULL. Error occured in editConfig");
			}
			
			if (StringUtil.equalIgnoreCase(NEW_CONFIG_NAME, configName)) {
				// new config
				if (StringUtil.isNullOrEmptyAfterTrim(configNameNew)) {
					throw new IllegalArgumentException("Invalid configuration name, cannot be empty");
				}
				configName = configNameNew;
			}

			DataType dType = DataType.valueOf(dataType.toUpperCase());
			switch(dType) {
			case COMMAND:
				UserDataProviderFactory.getCommandConfigs().save(configName, content);
				break;
			case CMD_ONECLICK:
				UserDataProviderFactory.getOneClickCommandConfigs().save(configName, content);
				break;
			case NODEGROUP:
				UserDataProviderFactory.getNodeGroupOfType(dType).save(configName, content);
				break;
			case SCRIPT:
				UserDataProviderFactory.getScriptOfType(dType).save(configName, content);
				break;
			default:
				throw new RuntimeException("Invalid datatype " + dataType);
			}
			
			String alert = String.format("%s %s was successfully updated at %s", dType.getLabel(), configName, DateUtils.getNowDateTimeDotStr());

			// reload after
			UserDataProviderFactory.reloadAllConfigs();
			
			if ("Config.showConfigs".equalsIgnoreCase(topage)) {
				redirect("Config.showConfigs", dataType, alert, topnav);
			}
			else {
				redirect(topage, alert);
			}

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
	public static void deleteConfig(String dataType, String configName, String nav, String page) {

		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;
		String topage = StringUtil.isNullOrEmpty(page) ? "Config.showConfigs" : page;

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			UserDataProviderFactory.getUserDataDao().deleteData(dType, configName);
			
			String alert = String.format("%s was successfully deleted at %s ", configName, DateUtils.getNowDateTimeDotStr());

			// reload after
			UserDataProviderFactory.reloadAllConfigs();
			
			if ("Config.showConfigs".equals(topage)) {
				redirect(topage, dataType, alert, topnav);
			} else {
				redirect(topage, alert);
			}

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
