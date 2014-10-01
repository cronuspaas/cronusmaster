package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.lightj.util.StringUtil;

import play.mvc.Controller;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.cronuspkg.ICronusPkg;
import com.stackscaling.agentmaster.resources.log.BaseLog;
import com.stackscaling.agentmaster.resources.log.IJobLogger;

/**
 * housekeeping
 * 
 * @author binyu
 * 
 */
public class HouseKeeping extends Controller {

	/**
	 * cleanup cronus pkgs from retention policy (# of name or time to keep)
	 * 
	 * @param retainFactor: name or time
	 * @param numToKeep: # of files/name, or # of days
	 */
	public static void deleteCronusPkg(String retainFactor, int numToKeep) 
	{
		try {
			
			List<String> deletedFiles = deleteUserData(UserDataProviderFactory.getUserDataDao(), 
					DataType.CRONUSPKG, retainFactor, numToKeep, false);
			
			// reload cache after deletes
			UserDataProviderFactory.getCronusPkgData().load();
			
			renderJSON(deletedFiles);
			
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in delete cronus pkgs "
					+ StringUtil.getStackTrace(t));
		}

	}// end func

	/**
	 * archive application logs base on retain factor (name, or date) and
	 * num2keep (# of files per name, or # of days)
	 * 
	 * @param dataType
	 *            type of logs
	 * @param retainFactor
	 *            name, or time
	 * @param numToKeep
	 *            # of files/name, or # of days
	 */
	public static void deleteLogs(String dataType, String retainFactor,
			int numToKeep) 
	{
		try {
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			
			// we don't delete the files yet as corresponding es index needs to be deleted too by logdao
			List<String> deletedFiles = deleteUserData(UserDataProviderFactory.getUserDataDao(), 
					dType, retainFactor, numToKeep, true);
			
			// now the real deletes
			IJobLogger logger = UserDataProviderFactory.getJobLoggerOfType(dType);
			for (String deletedLog : deletedFiles) {
				logger.deleteLog(deletedLog);
			}
			
			renderJSON(deletedFiles);
			
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in delete logs "
					+ StringUtil.getStackTrace(t));
		}

	}// end func

	/**
	 * cleanup user data based on retention criteria
	 * 
	 * @param userDataDao
	 * @param dType
	 * @param retainFactor
	 *            : name or lastmodified time
	 * @param numToKeep
	 *            : retention threshold, for name, # of files with the same
	 *            name, for time, # of days
	 * @throws IOException
	 */
	public static List<String> deleteUserData(IUserDataDao userDataDao, DataType dType,
			String retainFactor, int numToKeep, boolean deleteLater) throws IOException {
		List<UserDataMeta> files = userDataDao.listNames(dType);
		ArrayList<String> deletedFiles = new ArrayList<String>();
		if (StringUtil.equalIgnoreCase("time", retainFactor)) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0 - numToKeep);
			long ts = cal.getTimeInMillis();
			for (UserDataMeta fileMeta : files) {
				long fileTs = fileMeta.getLastModified().getTime();
				if (fileTs < ts) {
					if (!deleteLater) {
						userDataDao.deleteData(dType, fileMeta.getName());
					}
					deletedFiles.add(fileMeta.getName());
				}
			}
		} else if (StringUtil.equalIgnoreCase("name", retainFactor)) {
			HashMap<String, AtomicInteger> nameCount = new HashMap<String, AtomicInteger>();
			for (UserDataMeta fileMeta : files) {
				String namePart = getNamePart(dType, fileMeta.getName());
				if (!nameCount.containsKey(namePart)) {
					nameCount.put(namePart, new AtomicInteger(0));
				}
				if (nameCount.get(namePart).incrementAndGet() > numToKeep) {
					if (!deleteLater) {
						userDataDao.deleteData(dType, fileMeta.getName());
					}
					deletedFiles.add(fileMeta.getName());
				}
			}
		}
		return deletedFiles;

	}// end func

	/**
	 * find the name to be grouped on
	 * 
	 * @param dType
	 * @param name
	 * @return
	 */
	private static String getNamePart(DataType dType, String name) {
		String namePart = name;
		switch (dType) {
		case CMDLOG:
		case JOBLOG:
		case FLOWLOG:
			namePart = BaseLog.getLogMetaFromName(name).get("lastToken");
			break;
		case CRONUSPKG:
			try {
				ICronusPkg pkg = UserDataProviderFactory.getCronusPkgData()
						.getPkgByName(name);
				if (pkg != null) {
					namePart = pkg.getAppName();
				}
			} catch (IOException e) {
				play.Logger.error(e, "Unable to find Cronus Pkg by name");
			}
			break;
		default:
			break;
		}
		return namePart;
	}
}
