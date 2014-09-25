package com.stackscaling.agentmaster.resources.script;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * node group configs impl
 *
 * @author binyu
 *
 */
public abstract class ScriptDataImpl implements IScriptData {

	static Logger LOG = LoggerFactory.getLogger(ScriptDataImpl.class);

	private int scriptCount;

	@Autowired(required=true)
	private IUserDataDao userConfigs;

	/** loaded node groups */
	private HashMap<String, IScript> scripts = null;

	protected DataType dataType;

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	@Override
	public Map<String, IScript> getAllScripts() throws IOException {
		if (scripts == null) {
			load();
		}
		return scripts;
	}

	@Override
	public IScript getScriptByName(String name) throws IOException {
		if (scripts == null) {
			load();
		}
		if (scripts.containsKey(name)) {
			return scripts.get(name);
		}
		throw new InvalidObjectException(String.format(
				"Script of name %s does not exist", name));
	}

	@Override
	public void save(String ngName, String configFileContent) throws IOException {
		userConfigs.saveData(dataType, ngName, configFileContent);
		load();
	}

	@Override
	public void load() throws IOException {

		HashMap<String, IScript> scripts = new HashMap<String, IScript>();

		List<UserDataMeta> scriptMetas = userConfigs.listNames(dataType);
		for (UserDataMeta scriptMeta : scriptMetas) {
			String content = userConfigs.readData(DataType.SCRIPT, scriptMeta.getName());
			ScriptImpl scriptImpl = new ScriptImpl(scriptMeta.getName(), DataType.SCRIPT.name(), content);
			scriptImpl.setUserDataMeta(scriptMeta);
			scripts.put(scriptMeta.getName(), scriptImpl);
		}

		LOG.info("Completed scripts loading script count: "
				+ scripts.size() + " at " + DateUtils.getNowDateTimeStr());

		this.scripts = scripts;
		this.scriptCount = scriptMetas.size();
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
	public int getScriptCount() {
		return scriptCount;
	}

}
