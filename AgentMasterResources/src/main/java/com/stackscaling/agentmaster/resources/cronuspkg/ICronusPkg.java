package com.stackscaling.agentmaster.resources.cronuspkg;

import java.util.Date;

import com.stackscaling.agentmaster.resources.IUserData;


/**
 * represent a saved cronus package
 * package have naming convention of appname-version.platform.cronus, e.g. myapp-1.0.0.all.cronus
 * 
 * @author binyu
 *
 */
public interface ICronusPkg extends IUserData {

	/** what app this package is for */
	public String getAppName();
	public void setAppName(String appName);
	
	public String getVersion();
	public void setVersion(String version);
	
	public String getPlatform();
	public void setPlatform(String platform);
	
	public long getSize();
	
	public Date getLastModified();
	
	public String getExternalLink();
	public String getInternalLink();
	
}
