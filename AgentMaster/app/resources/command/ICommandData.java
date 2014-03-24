package resources.command;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

import resources.IUserData;

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
	 * @param configFileContent
	 * @throws IOException
	 */
	public void save(String configFileContent) throws IOException;
	
	/**
	 * load all commands from backing storage
	 */
	public void load() throws IOException;
	
	/**
	 * validate config file content
	 * @param configFileContent
	 * @throws InvalidObjectException
	 */
	public void validate(String configFileContent) throws IOException;

}
