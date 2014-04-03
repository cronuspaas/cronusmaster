package resources.command;

import java.util.List;

import org.lightj.example.task.HttpTaskRequest;

/**
 * command interface
 * @author binyu
 *
 */
public interface ICommand {
	
	public String getName();
	
	public void setName(String name);
	
	public HttpTaskRequest createCopy();
	
	public List<String> getAggRegexs();

	public void setAggRegexs(List<String> aggRegexs);
	
	public void setHttpTaskRequest(HttpTaskRequest httpTaskRequest);

}
