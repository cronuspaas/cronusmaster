package com.stackscaling.agentmaster.resources.script;

import com.stackscaling.agentmaster.resources.IUserData;


/**
 * group of nodes
 * @author binyu
 *
 */
public interface IScript extends IUserData 
{

	/**
	 * type of script
	 * @return
	 */
	public String getType();

	/**
	 * set type of script
	 * @param type
	 */
	public void setType(String type);

	/**
	 * script content
	 * @param content
	 */
	public void setContent(String content);

	/**
	 * script content
	 * @return
	 */
	public String getContent();
}
