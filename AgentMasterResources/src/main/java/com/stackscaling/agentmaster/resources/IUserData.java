package com.stackscaling.agentmaster.resources;

public interface IUserData {

	/**
	 * user configs handle persistence
	 * @return
	 */
	public IUserDataDao getUserDataDao();

	/**
	 * user configs handle persistence
	 * @param userConfigs
	 */
	public void setUserDataDao(IUserDataDao userConfigs);

}
