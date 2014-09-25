package com.stackscaling.agentmaster.resources.cronuspkg;

import java.util.Date;

import com.stackscaling.agentmaster.resources.BaseUserData;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * Cronus package
 * @author binyu
 *
 */
public class CronusPkgImpl extends BaseUserData implements ICronusPkg {
	
	private String appName;
	private String version;
	private String platform;
	private int sizeKByte;
	private Date createDate;
	
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
	public String getExternalLink() {
		return String.format("http://%s/agent/downloadPkg/%s", VarUtils.externalIp, name);
	}
	@Override
	public String getInternalLink() {
		return String.format("http://%s/agent/downloadPkg/%s", VarUtils.internalIp, name);
	}

}
