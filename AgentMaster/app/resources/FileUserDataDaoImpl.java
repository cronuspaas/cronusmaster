package resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import play.Logger;
import play.vfs.VirtualFile;
import resources.utils.DateUtils;

/**
 * file based {IUserConfigsDao}
 * @author binyu
 *
 */
public class FileUserDataDaoImpl implements IUserDataDao {
	
	public FileUserDataDaoImpl() {}
	
	/**
	 * list files
	 * @param dataType
	 * @return
	 */
	@Override
	public List<String> listNames(DataType dataType) 
	{

		VirtualFile vf = VirtualFile.fromRelativePath(dataType.getPath());
		File dir = vf.getRealFile();

		Collection<File> files = FileUtils.listFiles(dir, 
				new IOFileFilter() {

					@Override
					public boolean accept(File arg0) {
						return !arg0.getName().startsWith(".");
					}

					@Override
					public boolean accept(File arg0, String arg1) {
						return !arg1.startsWith(".");
					}
			
		}, null);
		List<String> fileNames = new ArrayList<String>();
		for (File file : files) {
			fileNames.add(file.getName());
		}
		Collections.sort(fileNames);
		return fileNames;

	}

	
	/**
	 * In the end: will always call
	 * NodeGroupDataMap object if needed in allAgentData
	 * @throws IOException 
	 */
	@Override
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

			VirtualFile vf = VirtualFile.fromRelativePath(configFileLocation);
			reader = new BufferedReader(new FileReader(vf.getRealFile()));
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}

			play.Logger.info("Completed readConfigFile with size: "
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

		if (dataType == null) {
			play.Logger.error("ERROR reading config: configFile is empty.");
		}

		// in test
		String configFileLocation = getFilePath(dataType, fileName);
		FileWriter fw = null;
		try {

			VirtualFile vf = VirtualFile.fromRelativePath(configFileLocation);

			boolean append = false;
			fw = new FileWriter(vf.getRealFile(), append);
			fw.write(configFileContent);

			fw.close();
			play.Logger.info("Completed saveConfigFile with size: "
					+ configFileContent.length() + " at "
					+ DateUtils.getNowDateTimeStr());

		}
		finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					Logger.error(e, e.getMessage());
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

		// in test
		String configFileLocation = getFilePath(type, fileName);

		VirtualFile vf = VirtualFile.fromRelativePath(configFileLocation);

		vf.getRealFile().delete();
		play.Logger.info("Deleted file : "
					+ type + "/" + fileName + " at "
					+ DateUtils.getNowDateTimeStr());

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
