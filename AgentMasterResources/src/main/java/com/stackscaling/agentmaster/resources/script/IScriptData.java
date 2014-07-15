package com.stackscaling.agentmaster.resources.script;

import java.io.IOException;
import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserData;

/**
 * command configs
 * @author binyu
 *
 */
public interface IScriptData extends IUserData {

	/**
	 * get all commands back
	 * @return
	 */
	public Map<String, IScript> getAllScripts() throws IOException;

	/**
	 * find template by name
	 * @param name
	 * @return
	 */
	public IScript getScriptByName(String name) throws IOException;

	/**
	 * save command configs
	 * @param configFileContent
	 * @throws IOException
	 */
	public void save(String scriptName, String configFileContent) throws IOException;

	/**
	 * load all commands from backing storage
	 */
	public void load() throws IOException;

	/**
	 * total number of scripts
	 * @return
	 */
	public int getScriptCount();

}
