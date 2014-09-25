package com.stackscaling.agentmaster.resources;

/**
 * base user data class
 * 
 * @author binyu
 *
 */
public abstract class BaseUserData implements IUserData {
	
	protected String name;
	protected UserDataMeta userDataMeta;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public UserDataMeta getUserDataMeta() {
		return userDataMeta;
	}
	public void setUserDataMeta(UserDataMeta userDataMeta) {
		this.userDataMeta = userDataMeta;
	}

}
