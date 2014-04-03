package resources.command;

import java.util.List;

import org.lightj.example.task.HttpTaskRequest;

/**
 * command impl
 * @author biyu
 *
 */
public class CommandImpl implements ICommand {
	
	/** name of the command */
	private String name;
	
	/** http request template */
	private HttpTaskRequest httpTaskRequest;

	/** list of aggregation regex */
	private List<String> aggRegexs;
	
	public List<String> getAggRegexs() {
		return aggRegexs;
	}

	public void setAggRegexs(List<String> aggRegexs) {
		this.aggRegexs = aggRegexs;
	}

	/**
	 * do not use externally, use createCopy because ICommand is cached and shared (copy on write)
	 * @return
	 */
	HttpTaskRequest getHttpTaskRequest() {
		return httpTaskRequest;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public HttpTaskRequest createCopy() {
		return httpTaskRequest.createNew();
	}
	
	public void setHttpTaskRequest(HttpTaskRequest httpTaskRequest) {
		this.httpTaskRequest = httpTaskRequest;
	}

}
