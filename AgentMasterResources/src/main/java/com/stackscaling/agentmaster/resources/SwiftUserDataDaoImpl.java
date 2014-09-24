package com.stackscaling.agentmaster.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stackscaling.agentmaster.resources.swift.SwiftResourceProvider;

/**
 * use Swift as user data store
 *
 * @author binyu
 *
 */
public class SwiftUserDataDaoImpl implements IUserDataDao {

	static Logger logger = LoggerFactory.getLogger(SwiftUserDataDaoImpl.class);

	@Override
	public String readData(DataType type, String name) throws IOException
	{
		try {

			Account account = SwiftResourceProvider.getSwiftClient();
			StoredObject swiftObject = account.getContainer(type.getUuid()).getObject(name);
			return readInputStream(swiftObject.downloadObjectAsInputStream());

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new IOException(e);
		}

	}

	@Override
	public void saveData(DataType type, String name, String content)
			throws IOException
	{
		try {

			Account account = SwiftResourceProvider.getSwiftClient();
			StoredObject swiftObject = account.getContainer(type.getUuid()).getObject(name);
			byte[] contentBytes = content.getBytes();
			swiftObject.setContentLength(contentBytes.length);
			swiftObject.uploadObject(contentBytes);
			logger.info("uploaded to swift " + name);

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public void deleteData(DataType type, String name) throws IOException
	{
        try {

			Account account = SwiftResourceProvider.getSwiftClient();
			StoredObject swiftObject = account.getContainer(type.getUuid()).getObject(name);
			swiftObject.delete();
        	logger.info("deleted from swift " + name);

        } catch (Exception e) {
			logger.error(e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public List<String> listNames(DataType type)
	{

		ArrayList<String> result = new ArrayList<String>();

		Account account = SwiftResourceProvider.getSwiftClient();
		Container container = account.getContainer(type.getUuid());
	    Collection<StoredObject> objects = container.list();
	    for (StoredObject currentObject : objects) {
	        result.add(currentObject.getName());
	    }
		return result;

	}

	/**
	 * read string presentation of an inputstream
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static String readInputStream(InputStream input)
			throws IOException
	{
		StringBuffer buf = new StringBuffer();
		// Read one text line at a time and display.
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			buf.append(line).append("\n");
		}
		return buf.toString();
	}

	@Override
	public InputStream readStream(DataType type, String name)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveStream(DataType type, String name, InputStream dataStream)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

}
