package com.stackscaling.agentmaster.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stackscaling.agentmaster.resources.UserDataMeta.UserDataMetaComparator;
import com.stackscaling.agentmaster.resources.utils.DateUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * file based {IUserConfigsDao}
 * @author binyu
 *
 */
public class FileUserDataDaoImpl implements IUserDataDao {
	
	static Logger logger = LoggerFactory.getLogger(FileUserDataDaoImpl.class);
	
	private UserDataMetaComparator udmComparator = new UserDataMetaComparator();
	
	public FileUserDataDaoImpl() {}

	/**
	 * list files
	 * @param dataType
	 * @return
	 * @throws IOException 
	 */
	public List<UserDataMeta> listNames(DataType dataType) throws IOException
	{

		File dir = VarUtils.vf.getRealFileFromRelativePath(dataType.getPath());

		Collection<File> files = FileUtils.listFiles(dir,
				new IOFileFilter() {

					public boolean accept(File arg0) {
						return !arg0.getName().startsWith(".");
					}

					public boolean accept(File arg0, String arg1) {
						return !arg1.startsWith(".");
					}

		}, null);
		
		List<UserDataMeta> fileNames = new ArrayList<UserDataMeta>();
		for (File file : files) {
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			fileNames.add(new UserDataMeta(
					file.getName(), 
					attr.size(), 
					new Date(attr.lastModifiedTime().toMillis())));
		}
		Collections.sort(fileNames, udmComparator);
		Collections.reverse(fileNames);
		return fileNames;

	}


	/**
	 * In the end: will always call
	 * NodeGroupDataMap object if needed in allAgentData
	 * @throws IOException
	 */
	public String readData(DataType type, String name) throws IOException {

		if (type == null) {
			return "ERROR reading config: data type is empty.";
		}

		// String nodeGroupConfFileLocation =
		// Play.configuration.getProperty("agentmaster.nodegroup.conf.file.location");

		StringBuilder sb = new StringBuilder();

		// in test
		String configFileLocation = String.format("%s/%s", type.getPath(), name);
		BufferedReader reader = null;
		try {

			reader = new BufferedReader(new FileReader(VarUtils.vf.getRealFileFromRelativePath(configFileLocation)));
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}

			logger.debug("Completed readConfigFile with size: "
					+ sb.toString().length() / 1024.0 + " KB at "
					+ DateUtils.getNowDateTimeStr());

		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		return sb.toString();

	} // end func.

	/**
	 * save config file
	 * @param dataType
	 * @param configFileContent
	 * @throws IOException
	 */
	@Override
	public void saveData(DataType dataType, String fileName, String configFileContent) throws IOException
	{

		String configFileLocation = getFilePath(dataType, fileName);
		FileWriter fw = null;
		try {

			fw = new FileWriter(VarUtils.vf.getRealFileFromRelativePath(configFileLocation), false);
			fw.write(configFileContent);

			fw.close();
			logger.debug("Completed saveConfigFile " 
					+ configFileLocation + " at "
					+ DateUtils.getNowDateTimeStr());

		}
		finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}

	} // end func.


	/**
	 * delete config file
	 */
	@Override
	public void deleteData(DataType type, String fileName)
			throws IOException {

		String configFileLocation = getFilePath(type, fileName);

		VarUtils.vf.getRealFileFromRelativePath(configFileLocation).delete();
		logger.info("Deleted file : " 
					+ configFileLocation + " at "
					+ DateUtils.getNowDateTimeStr());

	}


	@Override
	public InputStream readStream(DataType type, String name)
			throws IOException 
	{
		String configFileLocation = getFilePath(type, name);
		File configFile = VarUtils.vf.getRealFileFromRelativePath(configFileLocation);
		if (configFile.exists()) {
			return new FileInputStream(configFile);
		} else {
			throw new IllegalArgumentException(name + " not found");
		}
	}

	@Override
	public void saveStream(DataType type, String name, InputStream dataStream)
			throws IOException
	{

		String configFileLocation = getFilePath(type, name);
		FileOutputStream output = null;
		try {

			output = new FileOutputStream(VarUtils.vf.getRealFileFromRelativePath(configFileLocation));
			IOUtils.copy(dataStream, output);

			output.close();
			logger.debug("Completed saveConfigFile " 
					+ configFileLocation + " at "
					+ DateUtils.getNowDateTimeStr());

		}
		finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}

	}

	/**
	 * get file path
	 * @param cat
	 * @param fileName
	 * @return
	 */
	private String getFilePath(DataType cat, String fileName) {
		String filePath = null;
		filePath = String.format("%s/%s", cat.getPath(), fileName);
		return filePath;
	}

}
