package com.stackscaling.agentmaster.resources.command;

import org.lightj.example.task.HttpTaskRequest;

/**
 * enhance a command by change http request with common headers, parameters ect. 
 * 
 * @author binyu
 *
 */
public interface ICommandEnhancer {
	
	
	/** enhance the request after request is completely built
	 *  good place to insert common headers, parameters etc. 
	 *  that should be invisible to end user
	 */
	public void enhanceRequest(HttpTaskRequest request);

}
