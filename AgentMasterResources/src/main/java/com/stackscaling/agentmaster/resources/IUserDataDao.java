package com.stackscaling.agentmaster.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * manage persistence and CRUD of user configs
 * actual implementation can be based of local storage, object store, or database
 *
 * @author binyu
 *
 */
public interface IUserDataDao {

	/**
	 * read config file of type
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public String readData(DataType type, String name) throws IOException;

	/**
	 * render input stream for user data
	 * @param type
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public InputStream readStream(DataType type, String name) throws IOException;
	
	/**
	 * save config file of type
	 * @param type
	 * @param content
	 * @throws IOException
	 */
	public void saveData(DataType type, String name, String content) throws IOException;

	/**
	 * save config file from input stream
	 * @param type
	 * @param name
	 * @param dataStream
	 * @throws IOException
	 */
	public void saveStream(DataType type, String name, InputStream dataStream) throws IOException;

	/**
	 * delete config file
	 * @param type
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public void deleteData(DataType type, String name) throws IOException;

	/**
	 * list config files
	 * @param type
	 * @return
	 */
	public List<UserDataMeta> listNames(DataType type) throws IOException;

}
