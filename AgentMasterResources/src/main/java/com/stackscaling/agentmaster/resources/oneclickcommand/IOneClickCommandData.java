package com.stackscaling.agentmaster.resources.oneclickcommand;

import java.io.IOException;
import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserData;

/**
 * command configs
 * @author binyu
 *
 */
public interface IOneClickCommandData extends IUserData {

	/**
	 * get all commands back
	 * @return
	 */
	public Map<String, IOneClickCommand> getAllCommands() throws IOException;

	/**
	 * find template by name
	 * @param name
	 * @return
	 */
	public IOneClickCommand getCommandByName(String name) throws IOException;

	/**
	 * save command configs
	 * @param content
	 * @throws IOException
	 */
	public void save(String cmdName, String content) throws IOException;

	/**
	 * load all commands from backing storage
	 */
	public void load() throws IOException;

}
