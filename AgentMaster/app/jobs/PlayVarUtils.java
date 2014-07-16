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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

import play.Play;
import resources.utils.FileIoUtils;




/**
 * All static var, parameters...
 * 
 * @author ypei
 * 
 */
public class PlayVarUtils extends VarUtils {
	
	PlayVarUtils() {
		init();
	}
	
	public void init() {
		
		vf = new FileIoUtils();
		
		// user data provider
		userDataDaoType = getVarStr("agentmaster.userDataDao", "file");
		
		// s3
		myAccessKeyID = getVarStr("agentmaster.userDataDao.s3.myAccessKeyID", null);
		mySecretKey = getVarStr("agentmaster.userDataDao.s3.mySecretKey", null);
		
		// swift
		tenantId = getVarStr("agentmaster.userDataDao.swift.tenantId", null);
		tenantName = getVarStr("agentmaster.userDataDao.swift.tenantName", null);
		username = getVarStr("agentmaster.userDataDao.swift.username", null);
		password = getVarStr("agentmaster.userDataDao.swift.password", null);
		authenticationUrl = getVarStr("agentmaster.userDataDao.swift.authenticationUrl", null);
		
		// s3 and swift buckets
		for (DataType dt : DataType.values()) {
			initS3Uuid(dt.name());
			initSwiftUuid(dt.name());
		}
		
		// date util
		LOG_TIME_ZONE = getVarStr("LOG_TIME_ZONE", "America/Los_Angeles");    
		
		// enable log progress
		LOG_PROGRESS_ENABLED = getVarBool("agentmaster.logProgressEnabled", true);

		// elastic search
		ELASTICSEARCH_DATA = getVarStr("agentmaster.esData", "user_data/elasticsearch_data/data");
		ELASTICSEARCH_EP = getVarStr("agentmaster.esEp", "localhost");
		LOCAL_ES_ENABLED = getVarBool("agentmaster.localEsEnabled", true);
		BASELOG_CMDRES_LENGTH = getVarInt("agentmaster.baseLog.cmdResLength", 200);
		ESLOG_DATA_TTL = getVarStr("agentmaster.esLogDataTtl", "1d");
		ES_DATA_MAPPER = JsonUtil.customMapper("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
		
	}
	
	public String getVarStr(String propName, String defVal) {
		return Play.configuration.getProperty(propName, defVal);
	}
	
	public int getVarInt(String propName, int defVal) {
		return Integer.parseInt(Play.configuration.getProperty(propName, Integer.toString(defVal)));
	}
	
	public boolean getVarBool(String propName, boolean defVal) {
		return Boolean.valueOf(Play.configuration.getProperty(propName, Boolean.toString(defVal))).booleanValue();
	}
	
}
