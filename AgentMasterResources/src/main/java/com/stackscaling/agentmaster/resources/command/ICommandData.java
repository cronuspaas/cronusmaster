package com.stackscaling.agentmaster.resources.command;

import java.io.IOException;
import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserData;

/**
 * command configs
 * @author binyu
 *
 */
public interface ICommandData extends IUserData {

	/**
	 * get all commands back
	 * @return
	 */
	public Map<String, ICommand> getAllCommands() throws IOException;

	/**
	 * find template by name
	 * @param name
	 * @return
	 */
	public ICommand getCommandByName(String name) throws IOException;

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
