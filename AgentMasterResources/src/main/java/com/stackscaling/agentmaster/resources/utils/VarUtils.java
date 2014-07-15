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
package com.stackscaling.agentmaster.resources.utils;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * All static var, parameters...
 *
 */
public abstract class VarUtils {
	
	// virtual file util
	public static IVirtualFileUtils vf;

	// agent private key
	public static String AGENT_CRT_LOCATION = "conf/park.der";
	
	// user data provider
	public static String userDataDaoType; // .getProperty("agentmaster.userDataDao").toString();
	
	// s3
	public static String myAccessKeyID;
	public static String mySecretKey;
	// s3 buckets
	public static Map<String, String> s3Buckets = new HashMap<String, String>();
	
	// swift
	public static String tenantId; //	.getProperty("agentmaster.userDataDao.swift.tenantId");
	public static String tenantName; //	.getProperty("agentmaster.userDataDao.swift.tenantName");
	public static String username; //	.getProperty("agentmaster.userDataDao.swift.username");
	public static String password; //	.getProperty("agentmaster.userDataDao.swift.password");
	public static String authenticationUrl; //	.getProperty("agentmaster.userDataDao.swift.authenticationUrl");
	// swift buckets
	public static Map<String, String> swiftBuckets = new HashMap<String, String>();
	
	// date util
	public static String LOG_TIME_ZONE; // .getProperty("LOG_TIME_ZONE", "America/Los_Angeles");    

	// log progress
	public static boolean LOG_PROGRESS_ENABLED;

	// log and elastic search
	public static String ELASTICSEARCH_DATA;
	public static String ELASTICSEARCH_EP;
	public static boolean LOCAL_ES_ENABLED;
	public static int BASELOG_CMDRES_LENGTH;
	public static String ESLOG_DATA_TTL;
	public static ObjectMapper ES_DATA_MAPPER;
	
	public VarUtils() {}

	public final void initS3Uuid(String dataType) {
		String key = String.format("agentmaster.userDataDao.s3.%s.uuid", dataType);
		s3Buckets.put(dataType, getVarStr(key, null));
	}

	public final void initSwiftUuid(String dataType) {
		String key = String.format("agentmaster.userDataDao.swift.%s.uuid", dataType);
		swiftBuckets.put(dataType, getVarStr(key, null));
	}
	
	public static final IVirtualFileUtils getVf() {
		return vf;
	}

	public abstract void init();
	
	public abstract String getVarStr(String propName, String defVal);

	public abstract int getVarInt(String propName, int defVal);

	public abstract boolean getVarBool(String propName, boolean defVal);
	
}
