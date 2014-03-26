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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import models.utils.DateUtils;

import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.IUserDataDao;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;
import resources.command.ICommand;

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
	 * show all configs of a type
	 * @param dataType
	 */
	public static void showConfigs(String dataType) {
		
		String page = "showConfig";
		String topnav = "config";

		try {
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			List<String> cfgNames = UserDataProvider.getUserDataDao().listNames(dType);

			String alert = dataType + " Configs at " + DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, dataType, cfgNames, alert);

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
	public static void editConfig(String dataType, String configName) {

		String page = "editConfig";
		String topnav = "config";

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
			String content = userDataDao.readData(DataType.valueOf(dataType.toUpperCase()), configName);
			
			String configFileUpper = dataType.toUpperCase(Locale.ENGLISH);
			page = new String(page + dataType.toLowerCase(Locale.ENGLISH));

			String alert = null;

			render(page, topnav, configFileUpper, configName, content, alert);
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
	public static void editConfigUpdate(String dataType, String configName, String content) {

		String page = "editConfig";
		String topnav = "config";

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
			userDataDao.saveData(DataType.valueOf(dataType.toUpperCase()), configName, content);

			String configFileUpper = dataType.toUpperCase(Locale.ENGLISH);
			page = new String(page + dataType.toLowerCase(Locale.ENGLISH));
			String alert = "Config was successfully updated at "
					+ DateUtils.getNowDateTimeStrSdsm();

			// reload after
			UserDataProvider.reloadAllConfigs();

			renderTemplate("Config/editConfig.html", page, topnav, content, configFileUpper, configName, alert);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * show all configs
	 */
	public static void index() {

		String page = "index";
		String topnav = "config";

		try {
			Map<String, ICommand> cmds = UserDataProvider.getCommandConfigs().getAllCommands();
			List<Map<String, String>> commands = new ArrayList<Map<String,String>>();
			for (ICommand cmd : cmds.values()) {
				Map<String, String> values = new HashMap<String, String>();
				values.put("name", cmd.getName());
				UrlTemplate req = cmd.getHttpTaskRequest().getUrlTemplate();
				values.put("url", req.getUrl());
				values.put("httpMethod", req.getMethod().name());
				StringBuffer headers = new StringBuffer();
				for (Entry<String, String> header : req.getHeaders().entrySet()) {
					headers.append(String.format("%s=%s", header.getKey(), header.getValue())).append("\n");
				}
				values.put("headers", headers.toString());
				values.put("body", req.getBody());
				values.put("variables", StringUtil.join(req.getVariableNames(), ","));
				StringBuffer parameters = new StringBuffer();
				for (Entry<String, String> param : req.getParameters().entrySet()) {
					parameters.append(String.format("%s=%s | ", param.getKey(), param.getValue()));
				}
				values.put("parameters", parameters.toString());
				commands.add(values);
			}
			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, commands, lastRefreshed);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}

}
