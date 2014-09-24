package com.stackscaling.agentmaster.resources.cronuspkg;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
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

		List<String> pkgNames = userConfigs.listNames(DataType.CRONUSPKG);
		for (String pkgName : pkgNames) {
			CronusPkgImpl pkg = new CronusPkgImpl();
			pkg.setName(pkgName);
			this.parseFromUserDataName(pkg);
			pkgs.put(pkgName, pkg);
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

	@Override
	public Map<String, ICronusPkg> getAllPkgs() throws IOException {
		if (pkgs == null) {
			load();
		}
		return pkgs;
	}

	@Override
	public List<ICronusPkg> getPkgsByFilters(Map<String, String> filters)
			throws IOException 
	{
		List<ICronusPkg> rst = new ArrayList<ICronusPkg>();
		for (ICronusPkg pkg : pkgs.values()) {
			boolean match = true;
			for (Entry<String, String> filter : filters.entrySet()) {
				String fn = filter.getKey();
				String fv = filter.getValue();
				if (StringUtil.equalIgnoreCase("version", fn)) {
					match &= StringUtil.equalIgnoreCase(pkg.getVersion(), fv);
				} else if (StringUtil.equalIgnoreCase("platform", fn)) {
					match &= StringUtil.equalIgnoreCase(pkg.getPlatform(), fv);
				} else if (StringUtil.equalIgnoreCase("appname", fn)) {
					match &= StringUtil.equalIgnoreCase(pkg.getAppName(), fv);
				} else if (StringUtil.equalIgnoreCase("name", fn)) {
					match &= StringUtil.equalIgnoreCase(pkg.getName(), fv);
				}
			}
			if (match) {
				rst.add(pkg);
			}
		}
		return rst;
	}

	@Override
	public void save(String pkgName, InputStream dataInputStream) throws IOException {
		userConfigs.saveStream(DataType.CRONUSPKG, pkgName, dataInputStream);
	}

	@Override
	public InputStream getDownloadStream(String pkgName) throws IOException {
		return userConfigs.readStream(DataType.CRONUSPKG, pkgName);
	}
}
