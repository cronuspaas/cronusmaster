package resources.command;

import org.lightj.example.task.HttpTaskRequest;

/**
 * command interface
 * @author binyu
 *
 */
public interface ICommand {
	
	/**
	 * command name
	 * @return
	 */
	public String getName();
	
	/**
	 * command name
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * command template
	 * @return
	 */
	public HttpTaskRequest getHttpTaskRequest();

}
