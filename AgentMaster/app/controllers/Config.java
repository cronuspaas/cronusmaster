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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;
import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.JsonUtil;
import org.lightj.util.MapListPrimitiveJsonParser;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.utils.JsonResponse;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.command.CommandImpl;
import com.stackscaling.agentmaster.resources.log.ILog;
import com.stackscaling.agentmaster.resources.oneclickcommand.IOneClickCommand;
import com.stackscaling.agentmaster.resources.oneclickcommand.IOneClickCommandData;
import com.stackscaling.agentmaster.resources.oneclickcommand.OneClickCommandImpl;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
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
	 * reload all configs, render html response
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
	 * reload all configs, render json response
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
		
		// name used to drive second nav bar
		String page = "showConfigs"+dataType.toLowerCase();
		// top nav bar, can be passed in for displaying the page in different context
		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;

		try {
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			List<UserDataMeta> cfgs = UserDataProviderFactory.getUserDataDao().listNames(dType);
			List<Map<String, String>> cfgNames = new ArrayList<Map<String, String>>();
    		DecimalFormat myFormatter = new DecimalFormat("###,###,###");
			for (UserDataMeta cfg : cfgs) {
				HashMap<String, String> prop = new HashMap<String, String>();
				prop.put("name", cfg.getName());
				prop.put("size", myFormatter.format(cfg.getSize()));
				prop.put("lastmodified", DateUtils.getDateTimeStr(cfg.getLastModified()));
				cfgNames.add(prop);
			}
			String lastRefreshed = DateUtils.getNowDateTimeDotStr();

			render(page, topnav, dataType, cfgNames, lastRefreshed, alert);

		}
		catch (Exception e) {
			e.printStackTrace();
			error(e);
		}
	}

	/** name token indicate new config */
	static final String NEW_CONFIG_NAME = "new";

	/**
	 * create or update config
	 * @param dataType
	 */
	public static void editConfig(String dataType,
			String action, String configName, String topage, String nav) 
	{

		// redirect page
		String page = "editConfig";
		// top nav bar
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
						
						content = JsonUtil.encodePretty(command);
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
	 * save config
	 * @param dataType
	 * @param content
	 */
	public static void editConfigUpdate(String dataType, 
			String configName, String configNameNew, String content, String page, String nav) 
	{

		// top nav bar
		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;
		// page to redirect to after save, allow config update from different page context
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
			case SYSCMD:
				UserDataProviderFactory.getSysCommandConfigs().save(configName, content);
				break;
			default:
				throw new RuntimeException("Invalid datatype " + dataType);
			}
			
			String alert = String.format("%s %s was successfully updated at %s", 
					dType.getLabel(), configName, DateUtils.getNowDateTimeDotStr());

			// reload all configs after save
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
	public static void deleteConfig(String dataType, String configName, String nav, String page) 
	{
		
		// top nav bar
		String topnav = StringUtil.isNullOrEmpty(nav) ? "config" : nav;
		// redirect page, allow delete config from different page context
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
	 * config index page, default show nodegroup configs
	 */
	public static void index() {
		
		redirect("Config.showConfigs", "nodegroup");

	}
	
	
	/**
	 * edit and save a new oneclick command
	 * @param logType
	 * @param logId
	 */
	public static void oneclickSave(String logType, String logId) {

		// redirect page
		String page = "Config/editConfig.html";
		// top nav bar
		String topnav = "commands";
		// redirect page after save
		String topage = "Commands.oneclick";
		// data type
		String dataType = DataType.CMD_ONECLICK.name();
		String configName = "new";
		String alert = null;

		try {
		
			DataType lType = DataType.valueOf(logType.toUpperCase());
			ILog log = UserDataProviderFactory.getJobLoggerOfType(lType).readLog(logId);
			
			IOneClickCommand oneClickCmd = new OneClickCommandImpl();
			oneClickCmd.setCommandKey(log.getCommandKey());
			oneClickCmd.setNodeGroupKey(log.getNodeGroup().getName());
			Map<String, String> options = log.getUserData();
			String varValues = DataUtil.getOptionValue(options, "var_values", "{}").trim();
			Map<String, Object> userData = (Map<String, Object>) MapListPrimitiveJsonParser.parseJson(varValues);
			String rebuildVarValues = MapListPrimitiveJsonParser.buildJson(userData);
			options.put("var_values", rebuildVarValues);
			oneClickCmd.setUserData(options);
			String cmdName = DateUtils.getNowDateTimeStrConcise();
			oneClickCmd.setName(cmdName);
			String content = JsonUtil.encodePretty(oneClickCmd); 

			renderTemplate(page, topnav, dataType, configName, content, topage, alert);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}
	


}
