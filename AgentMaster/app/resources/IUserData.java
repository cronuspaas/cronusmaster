package resources;

public interface IUserData {
	
	/**
	 * user configs handle persistence
	 * @return
	 */
	public IUserDataDao getUserConfigs();

	/**
	 * user configs handle persistence
	 * @param userConfigs
	 */
	public void setUserConfigs(IUserDataDao userConfigs);

}
