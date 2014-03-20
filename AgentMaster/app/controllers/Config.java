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
import java.util.Locale;

import models.asynchttp.actors.ActorConfig;
import models.data.providers.AgentConfigProvider;
import models.data.providers.AgentDataProvider;
import models.utils.ConfUtils;
import models.utils.DateUtils;
import models.utils.VarUtils.CONFIG_FILE_TYPE;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.lightj.util.JsonUtil;
import org.lightj.util.SpringContextUtil;

import play.mvc.Controller;
import resources.FileUserDataDaoImpl;
import resources.IUserDataDao;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;

/**
 * 
 * @author ypei
 *
 */
public class Config extends Controller {

	/**
	 * gc
	 */
	public static void runGC() {

		try {
			ActorConfig.runGCWhenNoJobRunning();
			renderJSON( "Success in RunGC at " + DateUtils.getNowDateTimeStrSdsm());
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in RunGC");
		}

	}
	
	public static void viewConfigItem(String configType, String configKey) {
		try {
			DataType type = DataType.valueOf(configType.toUpperCase());
			String jsonResult = null;
			switch (type) {
			case COMMAND:
				jsonResult = JsonUtil.encode(UserDataProvider.getCommandConfigs().getCommandByName(configKey));
				break;
			case NODEGROUP:
			case ADHOCNODEGROUP:
				jsonResult = JsonUtil.encode(UserDataProvider.getNodeGroupOfType(type).getNodeGroupByName(configKey));
				break;
			default:
				break;
			}
			renderJSON(jsonResult);
		} catch (IOException e) {
			error(e);
		}
	}

	/**
	 * reload all config
	 * @param type
	 */
	public static void reloadConfig(String type) {

		try {
			UserDataProvider.reloadAllConfigs();
			renderJSON("Successful reload config with type " + type);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in reloadConfig with type" + type);
		}

	}

	/**
	 * edit page
	 * @param configFile
	 */
	public static void editConfig(String configFile) {

		String page = "editConfig";
		String topnav = "config";

		try {
			if (configFile == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
			String configFileContent = userDataDao.readConfigFile(DataType.valueOf(configFile.toUpperCase()), null);
			
			String configFileUpper = configFile.toUpperCase(Locale.ENGLISH);

			page = new String(page + configFile.toLowerCase(Locale.ENGLISH));

			String alert = null;

			render(page, topnav, configFileUpper, configFileContent, alert);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in editConfig");
		}

	}// end func

	/**
	 * save after edit
	 * @param configFile
	 * @param configFileContent
	 */
	public static void editConfigUpdate(String configFile, String configFileContent) {

		String page = "editConfig";
		String topnav = "config";

		try {
			if (configFile == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
			userDataDao.saveConfigFile(DataType.valueOf(configFile.toUpperCase()), null, configFileContent);

			String configFileUpper = configFile.toUpperCase(Locale.ENGLISH);
			page = new String(page + configFile.toLowerCase(Locale.ENGLISH));
			String alert = "Config was successfully updated at "
					+ DateUtils.getNowDateTimeStrSdsm();

			// reload after
			UserDataProvider.reloadAllConfigs();

			renderTemplate("Config/editConfig.html", page, topnav,
					configFileContent, configFileUpper, alert);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in editConfigUpdate");
		}

	}// end func

	/**
	 * show all configs
	 */
	public static void index() {

		String page = "index";
		String topnav = "config";

		try {
			IUserDataDao configsDao = SpringContextUtil.getBean("resources", IUserDataDao.class);

			String configFileNodeGroupTitle = DataType.NODEGROUP.toString();
			String configFileAggregationTitle = DataType.AGGREGATION.toString();
			String configFileCommandTitle = DataType.COMMAND.toString();
			
			String configFileCommands = configsDao.readConfigFile(DataType.COMMAND, null);
			String configFileContentNodeGroup = configsDao.readConfigFile(DataType.NODEGROUP, null);
			String configFileContentAggregation= configsDao.readConfigFile(DataType.AGGREGATION, null);

			render(page, topnav, 
					configFileNodeGroupTitle, configFileAggregationTitle,
					configFileContentNodeGroup, configFileContentAggregation,
					configFileCommandTitle, configFileCommands
					);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in editConfig");
		}

	}

	/**
	 * 20130718 add
	 * 
	 * @param runCronJob
	 */
	public static void setRunCronJob(boolean runCronJob) {

		ConfUtils.setRunCronJob(runCronJob);
		renderText("Set runCronJob as " + runCronJob + " at time: "
				+ DateUtils.getNowDateTimeStrSdsm());
	}
	
}
