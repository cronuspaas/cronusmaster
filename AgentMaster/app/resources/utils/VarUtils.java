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
package resources.utils;

import org.lightj.util.JsonUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import play.Play;




/**
 * All static var, parameters...
 * 
 * @author ypei
 * 
 */
public class VarUtils {
	
	public static final String AGENT_CRT_LOCATION = "conf/park.der";
	
	public static final boolean LOG_PROGRESS_ENABLED = getVarBool("agentmaster.logProgressEnabled", true);
	
	public static final String ELASTICSEARCH_DATA = getVarStr("agentmaster.esData", "user_data/elasticsearch_data");
	public static final String ELASTICSEARCH_EP = getVarStr("agentmaster.esEp", "localhost");
	public static final boolean LOCAL_ES_ENABLED = getVarBool("agentmaster.localEsEnabled", true);
	
	public static final int BASELOG_CMDRES_LENGTH = getVarInt("agentmaster.baseLog.cmdResLength", 200);

	public static final String ESLOG_DATA_TTL = getVarStr("agentmaster.esLogDataTtl", "1d");
	
	public static final ObjectMapper ES_DATA_MAPPER = JsonUtil.customMapper("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
	
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
