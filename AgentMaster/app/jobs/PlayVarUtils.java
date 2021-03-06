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
package jobs;

import org.lightj.util.JsonUtil;

import play.Play;
import resources.utils.CronusFileIoUtils;

import com.ning.http.util.Base64;
import com.stackscaling.agentmaster.resources.utils.VarUtils;




/**
 * All static var, parameters...
 * 
 * @author ypei
 * 
 */
public class PlayVarUtils extends VarUtils {
	
	public static int listLogSize = getVarInt("agentmaster.ui.listlogsize", 50);
	
	PlayVarUtils() {
		init();
	}
	
	public void init() {
		
		// user data provider
		userDataDaoType = getVarStr("agentmaster.userDataDao", "file");
		
		// file
		appHome = getVarStr("agentmaster.userDataDao.file.root", null);
		userDataDir = getVarStr("agentmaster.userDataDao.file.dir", null); 
		
		// s3
		myAccessKeyID = getVarStr("agentmaster.userDataDao.s3.myAccessKeyID", null);
		mySecretKey = getVarStr("agentmaster.userDataDao.s3.mySecretKey", null);
		
		// swift
		tenantId = getVarStr("agentmaster.userDataDao.swift.tenantId", null);
		tenantName = getVarStr("agentmaster.userDataDao.swift.tenantName", null);
		username = getVarStr("agentmaster.userDataDao.swift.username", null);
		password = getVarStr("agentmaster.userDataDao.swift.password", null);
		authenticationUrl = getVarStr("agentmaster.userDataDao.swift.authenticationUrl", null);
		
		// date util
		logTimeZone = getVarStr("LOG_TIME_ZONE", "America/Los_Angeles");    
		
		// enable log progress
		isLogProgEnabled = getVarBool("agentmaster.logProgressEnabled", true);

		// elastic search
		esDataPath = getVarStr("agentmaster.esData", "user_data/elasticsearch_data/data");
		esEp = getVarStr("agentmaster.esEp", "localhost");
		isLocalEsEnabled = getVarBool("agentmaster.localEsEnabled", true);
		cmdResLength = getVarInt("agentmaster.baseLog.cmdResLength", 200);
		esDataMapper = JsonUtil.customMapper("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
		
		// agent related
		agentPassword = getVarStr("agentmaster.cronusagent.password", null);
		if (agentPassword != null) {
			agentPasswordBase64 = Base64.encode(agentPassword.getBytes());
		}
		agentPkiCert = getVarStr("agentmaster.cronusagent.pkicert", null);
		
		// ips
		externalIp = getVarStr("agentmaster.externalIp", "127.0.0.1");
		internalIp = getVarStr("agentmaster.internalIp", "127.0.0.1");

		// in the end, init file util
		vf = new CronusFileIoUtils();
		
	}
	
	public static final String getVarStr(String propName, String defVal) {
		return Play.configuration.getProperty(propName, defVal);
	}
	
	public static final int getVarInt(String propName, int defVal) {
		return Integer.parseInt(Play.configuration.getProperty(propName, Integer.toString(defVal)));
	}
	
	public static final boolean getVarBool(String propName, boolean defVal) {
		return Boolean.valueOf(Play.configuration.getProperty(propName, Boolean.toString(defVal))).booleanValue();
	}
	
}
