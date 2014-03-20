package resources;

import org.lightj.example.task.HttpTaskRequest;

public interface ICommand {
	
	public String getName();
	public void setName(String name);
	public HttpTaskRequest getHttpTaskRequest();

}
