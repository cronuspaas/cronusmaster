/*  

Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package resources.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import play.vfs.VirtualFile;

import com.stackscaling.agentmaster.resources.utils.IVirtualFileUtils;

/**
 * 20130509 Auto generate TSDB URL.
 * 
 * 
 * @author ypei
 * 
 */
public class FileIoUtils implements IVirtualFileUtils {
	
	//System.getenv("PE_CONF_PWD")

	/**
	 * Output both This will display all files except for empty.txt refined
	 * 20130918
	 * 
	 * @param folderName
	 * @return
	 */
	public static void getFileAndDirNamesInFolder(String folderName,
			List<String> fileNames, List<String> dirNames) {

		if (fileNames == null) {
			fileNames = new ArrayList<String>();
		}

		if (dirNames == null) {
			dirNames = new ArrayList<String>();
		}

		try {

			VirtualFile virtualDir = VirtualFile.fromRelativePath(folderName);
			List<VirtualFile> virtualFileList = virtualDir.list();

			if (virtualFileList == null) {
				play.Logger.error("virtualFileList is NULL! in getFileNamesInFolder()");
			}

			play.Logger.info("Under folder: " + folderName + ",  File/dir count is " + virtualFileList.size());

			for (int i = 0; i < virtualFileList.size(); i++) {

				String fileOrDirName = virtualFileList.get(i).getName();
				if (virtualFileList.get(i).getRealFile().isFile()) {
					fileNames.add(fileOrDirName);
				} else if (virtualFileList.get(i).getRealFile().isDirectory()) {
					play.Logger.info("Directory " + fileOrDirName);
					dirNames.add(fileOrDirName);

				}
			}// end for

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}// end func.

	/**
	 * 20130927 Fixed Memory Leak. Dont use line by line, just use apache
	 * commons io!! so simple and easy!
	 * 
	 * @param filePath
	 * @return
	 */
	public String readFileToString(String filePath) {

		String fileContentString = null;

		try {

			VirtualFile vf = VirtualFile.fromRelativePath(filePath);
			File realFile = vf.getRealFile();
			fileContentString = FileUtils.readFileToString(realFile);

		} catch (java.io.FileNotFoundException e) {
			play.Logger.error("File Not Found exception.", e);
			fileContentString = "File Not Found exception. This file may have been removed. " + filePath;
		} catch (Throwable e) {
			play.Logger.error("Error in readConfigFile.", e.getLocalizedMessage());
			e.printStackTrace();
			fileContentString = "File Not Found exception. This file may have been removed. " + filePath;
		}
		return fileContentString.toString();

	} // end func.

	
	@Override
	public File getRealFileFromRelativePath(String relativePath) {
		VirtualFile vf = VirtualFile.fromRelativePath(relativePath);
		return vf.getRealFile(); 
	}

}// end class

