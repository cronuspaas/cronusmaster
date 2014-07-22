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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.vfs.VirtualFile;

import com.stackscaling.agentmaster.resources.IUserData;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;
import com.stackscaling.agentmaster.resources.utils.DateUtils;
import com.stackscaling.agentmaster.resources.utils.IVirtualFileUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 */
public class CronusFileIoUtils implements IVirtualFileUtils {
	
	static Logger LOG = LoggerFactory.getLogger(CronusFileIoUtils.class); 

	static File userDataHome;

	static {
		try {
			if (VarUtils.appHome == null) {
				throw new RuntimeException("not in production");
			}
			LOG.info("user data root dir " + VarUtils.appHome);
			userDataHome = new File(new File(VarUtils.appHome), VarUtils.userDataDir);
			LOG.info("user data dir " + userDataHome.getAbsolutePath());
			assert userDataHome.exists() && userDataHome.isDirectory() && userDataHome.canRead() && userDataHome.canWrite();
			for (DataType dt : DataType.values()) {
				File dtDir = new File(userDataHome, dt.getPath());
				if (!dtDir.exists()) {
					dtDir.mkdir();
				}
			}
		} catch (Throwable t) {
			// anything wrong, fall back to use play root
			LOG.error("fail to get user data root %s, fallback to use play path", t.getMessage());
			userDataHome = Play.applicationPath;
		}
	}
	
	public CronusFileIoUtils() {
	}
	
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

			File realFile = this.getRealFileFromRelativePath(filePath);
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
		File realFile = new File(userDataHome, relativePath);
		return realFile; 
	}
	
}// end class

