package resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import models.data.LogFile;
import models.utils.DateUtils;
import models.utils.FileIoUtils;
import models.utils.VarUtils;
import play.Logger;
import play.vfs.VirtualFile;

/**
 * file based {IUserConfigsDao}
 * @author binyu
 *
 */
public class FileUserDataDaoImpl implements IUserDataDao {
	
	public FileUserDataDaoImpl() {}
	
	/**
	 * list files
	 * @param configFile
	 * @return
	 */
	public List<String> listFiles(DataType configFile) {

		List<String> fileNames = FileIoUtils.getFileNamesInFolder(configFile.getPath());
		Collections.sort(fileNames);
		return fileNames;

	}

	
	/**
	 * In the end: will always call
	 * NodeGroupDataMap object if needed in allAgentData
	 * @throws IOException 
	 */
	@Override
	public String readConfigFile(DataType type, String fileName) throws IOException {

		if (type == null) {
			return "ERROR reading config: data type is empty.";
		}

		// String nodeGroupConfFileLocation =
		// Play.configuration.getProperty("agentmaster.nodegroup.conf.file.location");

		StringBuilder sb = new StringBuilder();

		// in test
		String configFileLocation = type.isFile() ? String.format("%s/%s.conf", type.getPath(), type.toString().toLowerCase()) :
								String.format("%s/%s", type.getPath(), fileName);
		BufferedReader reader = null;
		try {

			VirtualFile vf = VirtualFile.fromRelativePath(configFileLocation);
			reader = new BufferedReader(new FileReader(vf.getRealFile()));
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}

			models.utils.LogUtils.printLogNormal("Completed readConfigFile with size: "
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
	 * @param configFile
	 * @param configFileContent
	 * @throws IOException 
	 */
	@Override
	public void saveConfigFile(DataType configFile, String fileName, String configFileContent) throws IOException 
	{

		if (configFile == null) {
			models.utils.LogUtils.printLogError("ERROR reading config: configFile is empty.");
		}

		// in test
		String configFileLocation = getFilePath(configFile, fileName);
		FileWriter fw = null;
		try {

			VirtualFile vf = VirtualFile.fromRelativePath(configFileLocation);

			boolean append = false;
			fw = new FileWriter(vf.getRealFile(), append);
			fw.write(configFileContent);

			fw.close();
			models.utils.LogUtils.printLogNormal("Completed saveConfigFile with size: "
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
	 * get file path
	 * @param cat
	 * @param fileName
	 * @return
	 */
	private String getFilePath(DataType cat, String fileName) {
		String filePath = null;
		switch(cat) {
		case NODEGROUP:
		case COMMAND:
			filePath = String.format("conf/%s.conf", cat.name().toLowerCase());
			break;
		case LOG:
			filePath = String.format("app_logs/%s.jsonlog.txt", fileName);
			break;
		default:
			throw new IllegalArgumentException(String.format("Invalid data category %s", cat));
		}
		return filePath;
	}
	
}
