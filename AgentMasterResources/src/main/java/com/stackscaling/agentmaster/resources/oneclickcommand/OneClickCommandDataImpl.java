package com.stackscaling.agentmaster.resources.oneclickcommand;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;

public class OneClickCommandDataImpl implements IOneClickCommandData {

	@Autowired(required=true)
	private IUserDataDao userDataDao;

	/** templates */
	private HashMap<String, IOneClickCommand> templates = null;

	/** constructor */
	public OneClickCommandDataImpl() {
	}

	@Override
	public Map<String, IOneClickCommand> getAllCommands() throws IOException {
		if (templates == null) {
			load();
		}
		return templates;
	}

	@Override
	public IOneClickCommand getCommandByName(String name) throws IOException {
		if (templates == null) {
			load();
		}
		if (templates.containsKey(name)) {
			return templates.get(name);
		}
		throw new InvalidObjectException(String.format("Command of name %s does not exist", name));
	}

	@Override
	public void save(String cmdName, String content) throws IOException {
		OneClickCommandImpl commandImpl = JsonUtil.decode(content, OneClickCommandImpl.class);
		commandImpl.setName(cmdName);
		userDataDao.saveData(DataType.CMD_ONECLICK, cmdName, JsonUtil.encodePretty(commandImpl));
		load();
	}

	@Override
	public void load() throws IOException {
		HashMap<String, IOneClickCommand> templates = new HashMap<String, IOneClickCommand>();
		List<String> cmdNames = userDataDao.listNames(DataType.CMD_ONECLICK);
		for (String cmdName : cmdNames) {
			String content = userDataDao.readData(DataType.CMD_ONECLICK, cmdName);
			if (!StringUtil.isNullOrEmpty(content)) {
				OneClickCommandImpl cmd = JsonUtil.decode(content, OneClickCommandImpl.class);
				templates.put(cmdName, cmd);
			}
		}
		this.templates = templates;
	}

	public IUserDataDao getUserDataDao() {
		return userDataDao;
	}

	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userDataDao = userConfigs;
	}
	
}
