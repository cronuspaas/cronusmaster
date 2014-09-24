package com.stackscaling.agentmaster.resources.cronuspkg;

import java.util.Date;

/**
 * Cronus package
 * @author binyu
 *
 */
public class CronusPkgImpl implements ICronusPkg {
	
	private String name;
	private String appName;
	private String version;
	private String platform;
	private int sizeKByte;
	private Date createDate;
	private String downloadLink;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public int getSizeKByte() {
		return sizeKByte;
	}
	public void setSizeKByte(int sizeKByte) {
		this.sizeKByte = sizeKByte;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	@Override
	public String getDownloadLink() {
		return downloadLink;
	}

}
