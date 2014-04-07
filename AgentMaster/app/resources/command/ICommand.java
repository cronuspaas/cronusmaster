package resources.command;

import java.util.List;
import java.util.Map;

import org.lightj.example.task.HttpTaskRequest;

/**
 * command interface
 * @author binyu
 *
 */
public interface ICommand {
	
	/** name of the command */
	public String getName();
	
	/** name of the command */
	public void setName(String name);
	
	/** http request */
	public HttpTaskRequest createCopy();
	
	/** aggregation regex */
	public List<String> getAggRegexs();

	/** aggregation regex */
	public void setAggRegexs(List<String> aggRegexs);
	
	/** set http request */
	public void setHttpTaskRequest(HttpTaskRequest httpTaskRequest);
	
}
