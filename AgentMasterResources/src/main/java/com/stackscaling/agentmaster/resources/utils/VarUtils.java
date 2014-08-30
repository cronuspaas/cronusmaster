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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * All static var, parameters...
 *
 */
public abstract class VarUtils {
	
	// virtual file util
	public static IVirtualFileUtils vf;

	// user data provider
	public static String userDataDaoType; // .getProperty("agentmaster.userDataDao").toString();
	
	// file
	public static String appHome;
	public static String userDataDir;
	
	// s3
	public static String myAccessKeyID;
	public static String mySecretKey;
	
	// swift
	public static String tenantId; //	.getProperty("agentmaster.userDataDao.swift.tenantId");
	public static String tenantName; //	.getProperty("agentmaster.userDataDao.swift.tenantName");
	public static String username; //	.getProperty("agentmaster.userDataDao.swift.username");
	public static String password; //	.getProperty("agentmaster.userDataDao.swift.password");
	public static String authenticationUrl; //	.getProperty("agentmaster.userDataDao.swift.authenticationUrl");
	
	// date util
	public static String logTimeZone; // .getProperty("LOG_TIME_ZONE", "America/Los_Angeles");    

	// log progress
	public static boolean isLogProgEnabled;

	// log and elastic search
	public static String esDataPath;
	public static String esEp;
	public static boolean isLocalEsEnabled;
	public static int cmdResLength;
	public static ObjectMapper esDataMapper;
	
	// agent
	public static String agentPassword;
	public static String agentPasswordBase64;
	// agent private key
	public static String agentPkiCert;
	
	
	public VarUtils() {}

	public static final IVirtualFileUtils getVf() {
		return vf;
	}

	public abstract void init();
	
	public abstract String getVarStr(String propName, String defVal);

	public abstract int getVarInt(String propName, int defVal);

	public abstract boolean getVarBool(String propName, boolean defVal);
	
}
