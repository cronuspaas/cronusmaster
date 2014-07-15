package com.stackscaling.agentmaster.resources.script;


/**
 * group of nodes
 * @author binyu
 *
 */
public interface IScript {

	/** script name */
	public String getName();

	/**
	 * set script name
	 * @param name
	 */
	public void setName(String name);

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
