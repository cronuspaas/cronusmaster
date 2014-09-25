package com.stackscaling.agentmaster.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * base user data class
 * 
 * @author binyu
 *
 */
public abstract class BaseUserData implements IUserData {
	
	protected String name;
	
	@JsonIgnore
	protected UserDataMeta userDataMeta;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@JsonIgnore
	public UserDataMeta getUserDataMeta() {
		return userDataMeta;
	}
	@JsonIgnore
	public void setUserDataMeta(UserDataMeta userDataMeta) {
		this.userDataMeta = userDataMeta;
	}

}
