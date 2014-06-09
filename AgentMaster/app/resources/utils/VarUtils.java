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

import play.Play;




/**
 * All static var, parameters...
 * 
 * @author ypei
 * 
 */
public class VarUtils {
	
	public static final String AGENT_CRT_LOCATION = "conf/park.der";
	
	public static final boolean LOG_PROGRESS_ENABLED = Boolean.valueOf(Play.configuration.getProperty("agentmaster.logProgressEnabled", "true")).booleanValue();
	
	public static final String ELASTICSEARCH_DATA = Play.configuration.getProperty("agentmaster.esData", "user_data/elasticsearch_data");
	public static final String ELASTICSEARCH_EP = Play.configuration.getProperty("agentmaster.esEp", "localhost");
	public static final boolean LOCAL_ES_ENABLED = Boolean.valueOf(Play.configuration.getProperty("agentmaster.localEsEnabled", "true")).booleanValue();
	
}
