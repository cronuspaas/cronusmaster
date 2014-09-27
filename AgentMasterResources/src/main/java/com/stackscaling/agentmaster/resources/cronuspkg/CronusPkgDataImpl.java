package com.stackscaling.agentmaster.resources.cronuspkg;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * cronus package impl
 *
 * @author binyu
 *
 */
public class CronusPkgDataImpl implements ICronusPkgData {

	static Logger LOG = LoggerFactory.getLogger(CronusPkgDataImpl.class);

	/** number of pkgs on hand */
	private int pkgCount;

	@Autowired(required=true)
	private IUserDataDao userConfigs;

	/** loaded package metadata */
	private HashMap<String, ICronusPkg> pkgs = null;

	@Override
	public ICronusPkg getPkgByName(String name) throws IOException {
		if (pkgs == null) {
			load();
		}
		if (pkgs.containsKey(name)) {
			return pkgs.get(name);
		}
		throw new InvalidObjectException(String.format(
				"Pacakge of name %s does not exist", name));
	}

	@Override
	public void load() throws IOException {

		HashMap<String, ICronusPkg> pkgs = new HashMap<String, ICronusPkg>();

		List<UserDataMeta> pkgMetas = userConfigs.listNames(DataType.CRONUSPKG);
		for (UserDataMeta pkgMeta : pkgMetas) {
			CronusPkgImpl pkg = new CronusPkgImpl();
			pkg.setName(pkgMeta.getName());
			pkg.setUserDataMeta(pkgMeta);
			this.parseFromUserDataName(pkg);
			pkgs.put(pkgMeta.getName(), pkg);
		}

		LOG.info("Completed cronuspkgs loading pkgs count: "
				+ pkgs.size() + " at " + DateUtils.getNowDateTimeStr());

		this.pkgs = pkgs;
		this.pkgCount = pkgs.size();
	}

	@Override
	public IUserDataDao getUserDataDao() {
		return userConfigs;
	}

	@Override
	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userConfigs = userConfigs;
	}

	@Override
	public int getPkgCount() {
		return pkgCount;
	}
	
	static final String CRONUSPKG_NAME_REGEX = "(.*)-(.*)\\.(.*)\\.cronus";
	static Pattern CRONUSPKG_NAME_PATTERN = Pattern.compile(CRONUSPKG_NAME_REGEX);
	
	/**
	 * parse persisted user data name and populate fields
	 * @param userDataName
	 */
	private void parseFromUserDataName(CronusPkgImpl pkg) {
		Matcher matcher = CRONUSPKG_NAME_PATTERN.matcher(pkg.getName());
		if (matcher.matches()) {
			pkg.setAppName(matcher.group(1));
			pkg.setVersion(matcher.group(2));
			pkg.setPlatform(matcher.group(3));
		}
	}
	
	/**
	 * validate the name is a valid cronus pkg name
	 * @param pkgName
	 * @return
	 */
	private void validateName(String pkgName) {
		Matcher matcher = CRONUSPKG_NAME_PATTERN.matcher(pkgName);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid cronus package name " + pkgName);
		}
	}

	@Override
	public Map<String, ICronusPkg> getAllPkgs() throws IOException {
		if (pkgs == null) {
			load();
		}
		return pkgs;
	}

	@Override
	public List<ICronusPkg> getPkgsByFilter(final String fn, String fvReg)
			throws IOException 
	{
		Pattern pattern = Pattern.compile(fvReg);
		Matcher matcher = null;
		List<ICronusPkg> rst = new ArrayList<ICronusPkg>();
		for (ICronusPkg pkg : pkgs.values()) {
			String mv = null;
			if (StringUtil.equalIgnoreCase("version", fn)) {
				mv = pkg.getVersion();
			} else if (StringUtil.equalIgnoreCase("platform", fn)) {
				mv = pkg.getPlatform();
			} else if (StringUtil.equalIgnoreCase("appname", fn)) {
				mv = pkg.getAppName();
			} else if (StringUtil.equalIgnoreCase("name", fn)) {
				mv = pkg.getName();
			}
			matcher = pattern.matcher(mv);
			if (matcher.matches()) {
				rst.add(pkg);
			}
		}
		
		// always sort desc before return
		Collections.sort(rst, Collections.reverseOrder(new Comparator<ICronusPkg>() {

			@Override
			public int compare(ICronusPkg o1, ICronusPkg o2) {
				if (StringUtil.equalIgnoreCase("version", fn)) {
					return o1.getVersion().compareTo(o2.getVersion());
				} else if (StringUtil.equalIgnoreCase("platform", fn)) {
					return o1.getPlatform().compareTo(o2.getPlatform());
				} else if (StringUtil.equalIgnoreCase("appname", fn)) {
					return o1.getAppName().compareTo(o2.getAppName());
				} else if (StringUtil.equalIgnoreCase("name", fn)) {
					return o1.getName().compareTo(o2.getName());
				}
				return o1.getName().compareTo(o2.getName());
			}
		}));
		
		return rst;
	}
	

	@Override
	public void save(String pkgName, InputStream dataInputStream) throws IOException 
	{
		validateName(pkgName);
		userConfigs.saveStream(DataType.CRONUSPKG, pkgName, dataInputStream);
	}

	@Override
	public InputStream getDownloadStream(String pkgName) throws IOException {
		return userConfigs.readStream(DataType.CRONUSPKG, pkgName);
	}
}
