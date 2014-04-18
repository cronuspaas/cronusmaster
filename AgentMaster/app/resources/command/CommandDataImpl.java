package resources.command;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lightj.example.task.HttpTaskRequest;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.IUserDataDao.DataType;

public class CommandDataImpl implements ICommandData {
	
	@Autowired(required=true)
	private IUserDataDao userDataDao;
	
	/** templates */
	private HashMap<String, ICommand> templates = null;
	
	public CommandDataImpl() {}
	
	@Override
	public Map<String, ICommand> getAllCommands() throws IOException {
		if (templates == null) {
			load();
		}
		return templates;
	}

	@Override
	public ICommand getCommandByName(String name) throws IOException {
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
		CommandImpl commandImpl = JsonUtil.decode(content, CommandImpl.class);
		commandImpl.setName(cmdName);
		userDataDao.saveData(DataType.COMMAND, cmdName, JsonUtil.encodePretty(commandImpl));
		load();
	}

	@Override
	public void load() throws IOException {
		HashMap<String, ICommand> templates = new HashMap<String, ICommand>();
		List<String> cmdNames = userDataDao.listNames(DataType.COMMAND);
		for (String cmdName : cmdNames) {
			String content = userDataDao.readData(DataType.COMMAND, cmdName);
			if (!StringUtil.isNullOrEmpty(content)) {
				CommandImpl cmd = JsonUtil.decode(content, CommandImpl.class);
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
