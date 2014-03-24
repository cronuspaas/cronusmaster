package resources.command;

import org.lightj.example.task.HttpTaskRequest;

public class CommandImpl implements ICommand {
	
	private String name;
	private HttpTaskRequest httpTaskRequest;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public HttpTaskRequest getHttpTaskRequest() {
		return httpTaskRequest;
	}
	
	public void setHttpTaskRequest(HttpTaskRequest httpTaskRequest) {
		this.httpTaskRequest = httpTaskRequest;
	}

}
