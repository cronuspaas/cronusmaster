package com.stackscaling.agentmaster.resources;

public interface IUserDataProvider {

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
