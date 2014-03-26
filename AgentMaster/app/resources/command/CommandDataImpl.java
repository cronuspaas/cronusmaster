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
		JsonUtil.decode(content, HttpTaskRequest.class);
		userDataDao.saveData(DataType.COMMAND, cmdName, content);
		load();
	}

	@Override
	public void load() throws IOException {
		HashMap<String, ICommand> templates = new HashMap<String, ICommand>();
		List<String> cmdNames = userDataDao.listNames(DataType.COMMAND);
		for (String cmdName : cmdNames) {
			String content = userDataDao.readData(DataType.COMMAND, cmdName);
			if (!StringUtil.isNullOrEmpty(content)) {
				HttpTaskRequest req = JsonUtil.decode(content, HttpTaskRequest.class);
				CommandImpl cmd = new CommandImpl();
				cmd.setName(cmdName);
				cmd.setHttpTaskRequest(req);
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
