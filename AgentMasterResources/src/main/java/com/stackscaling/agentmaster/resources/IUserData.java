package com.stackscaling.agentmaster.resources;

/**
 * base interface of all user data
 * 
 * @author binyu
 *
 */
public interface IUserData {
	
	public String getName();
	
	public void setName(String name);

	public UserDataMeta getUserDataMeta();
	
	public void setUserDataMeta(UserDataMeta meta);
	
}
