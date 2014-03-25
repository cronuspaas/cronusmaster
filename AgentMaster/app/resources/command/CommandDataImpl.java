package resources.command;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.type.TypeReference;
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
	public void save(String configFileContent) throws IOException {
		validate(configFileContent);
		userDataDao.saveConfigFile(DataType.COMMAND, null, configFileContent);
		load();
	}

	@Override
	public void load() throws IOException {
		HashMap<String, ICommand> templates = new HashMap<String, ICommand>();
		String content = userDataDao.readConfigFile(DataType.COMMAND, null);
		if (!StringUtil.isNullOrEmpty(content)) {
			HashMap<String, HttpTaskRequest> dataLoaded = JsonUtil.decode(content, new HttpTemplateTypeReference());
			for (Entry<String, HttpTaskRequest> data : dataLoaded.entrySet()) {
				String key = data.getKey();
				HttpTaskRequest req = data.getValue();
				CommandImpl cmd = new CommandImpl();
				cmd.setName(key);
				cmd.setHttpTaskRequest(req);
				templates.put(key, cmd);
			}
		}
		this.templates = templates;
	}

	@Override
	public void validate(String configFileContent) throws IOException {
		if (!StringUtil.isNullOrEmpty(configFileContent)) {
			JsonUtil.decode(configFileContent, new HttpTemplateTypeReference());
		}
		else {
			throw new InvalidObjectException("Command content cannot be empty");
		}
	}

	public IUserDataDao getUserDataDao() {
		return userDataDao;
	}

	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userDataDao = userConfigs;
	}

	static final class HttpTemplateTypeReference extends TypeReference<HashMap<String, HttpTaskRequest>> {
	}
}