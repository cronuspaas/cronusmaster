package com.stackscaling.agentmaster.resources.cronuspkg;

import java.util.Date;


/**
 * represent a saved cronus package
 * package have naming convention of appname-version.platform.cronus, e.g. myapp-1.0.0.all.cronus
 * 
 * @author binyu
 *
 */
public interface ICronusPkg {

	/** package name */
	public String getName();
	public void setName(String name);

	/** what app this package is for */
	public String getAppName();
	public void setAppName(String appName);
	
	public String getVersion();
	public void setVersion(String version);
	
	public String getPlatform();
	public void setPlatform(String platform);
	
	public int getSizeKByte();
	public void setSizeKByte(int sizeKByte);
	
	public Date getCreateDate();
	public void setCreateDate(Date createDate);
	
	public String getDownloadLink();
	
}
