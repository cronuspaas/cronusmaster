package com.stackscaling.agentmaster.resources.command;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lightj.example.task.HostTemplateValues;
import org.lightj.example.task.HttpTaskRequest;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.task.ExecuteOption;
import org.lightj.task.MonitorOption;
import org.lightj.util.JsonUtil;
import org.lightj.util.SpringContextUtil;
import org.lightj.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.UserDataProviderFactory;
import com.stackscaling.agentmaster.resources.cronuspkg.ICronusPkg;
import com.stackscaling.agentmaster.resources.utils.DataUtil;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * base class for command dao
 * @author binyu
 *
 */
public abstract class BaseCommandData implements ICommandData {

	@Autowired(required=true)
	protected IUserDataDao userDataDao;

	/** command cache */
	protected HashMap<String, ICommand> cmdCache = null;

	protected DataType dataType;
	
	public BaseCommandData(DataType dataType) {
		this.dataType = dataType;
	}

	@Override
	public Map<String, ICommand> getAllCommands() throws IOException {
		if (cmdCache == null) {
			load();
		}
		return cmdCache;
	}

	@Override
	public ICommand getCommandByName(String name) throws IOException {
		if (cmdCache == null) {
			load();
		}
		if (cmdCache.containsKey(name)) {
			return cmdCache.get(name);
		}
		throw new InvalidObjectException(String.format("Command of name %s does not exist", name));
	}

	@Override
	public void save(String cmdName, String content) throws IOException {
		CommandImpl commandImpl = JsonUtil.decode(content, CommandImpl.class);
		commandImpl.setName(cmdName);
		userDataDao.saveData(dataType, cmdName, JsonUtil.encodePretty(commandImpl));
		load();
	}

	@Override
	public void load() throws IOException {
		HashMap<String, ICommand> templates = new HashMap<String, ICommand>();
		List<UserDataMeta> cmdMetas = userDataDao.listNames(dataType);
		for (UserDataMeta cmdMeta : cmdMetas) {
			String content = userDataDao.readData(dataType, cmdMeta.getName());
			if (!StringUtil.isNullOrEmpty(content)) {
				CommandImpl cmd = JsonUtil.decode(content, CommandImpl.class);
				cmd.setUserDataMeta(cmdMeta);
				templates.put(cmdMeta.getName(), cmd);
			}
		}
		this.cmdCache = templates;
	}

	public IUserDataDao getUserDataDao() {
		return userDataDao;
	}

	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userDataDao = userConfigs;
	}
	
	/**
	 * build http request from user request
	 * @param ng
	 * @param cmd
	 * @param options
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("rawtypes")
	public static HttpTaskRequest createTaskByRequest(
			String[] hosts, 
			ICommand cmd, 
			Map<String, String> options, 
			Map<String, ?> userData) throws IOException 
	{
		HttpTaskRequest reqTemplate = cmd.createCopy();
		
		// enhance the request by its category
		ICommandEnhancer cmdEnhancer = UserDataProviderFactory.getCommandEnhancer(cmd.getCategory());
		if (cmdEnhancer != null) {
			cmdEnhancer.enhanceRequest(reqTemplate);
		}

		// process user input for execution
		long exeInitDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "exe_initde", "0"));
		long exeTimoutSec = Long.parseLong(DataUtil.getOptionValue(options, "exe_to", "0"));
		int exeRetry = Integer.parseInt(DataUtil.getOptionValue(options, "exe_retry", "0"));
		long retryDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "exe_rede", "0"));
		ExecuteOption exeOption = new ExecuteOption(exeInitDelaySec, exeTimoutSec, exeRetry, retryDelaySec);
		reqTemplate.setExecutionOption(exeOption);
		
		if (StringUtil.equalIgnoreCase(HttpTaskRequest.TaskType.asyncpoll.name(), reqTemplate.getTaskType())) {
			long monIntervalSec = Integer.parseInt(DataUtil.getOptionValue(options, "mon_int", "1"));
			long monInitDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "mon_initde", "0"));
			long monTimoutSec = Long.parseLong(DataUtil.getOptionValue(options, "mon_to", "0"));
			int monRetry = Integer.parseInt(DataUtil.getOptionValue(options, "mon_retry", "0"));
			long monRetryDelaySec = Long.parseLong(DataUtil.getOptionValue(options, "mon_rede", "0"));
			MonitorOption monOption = new MonitorOption(monInitDelaySec, monIntervalSec, monTimoutSec, monRetry, monRetryDelaySec);
			reqTemplate.setMonitorOption(monOption);
		}
		else {
			for (String option : new String[] {"mon_int", "mon_initde", "mon_to", "mon_retry", "mon_rede"}) {
				options.remove(option);
			}
		}
				
		Strategy strategy = Strategy.valueOf(DataUtil.getOptionValue(options, "thrStrategy", "UNLIMITED"));
		int maxRate = Integer.parseInt(DataUtil.getOptionValue(options, "thr_rate", "1000"));
		BatchOption batchOption = new BatchOption(maxRate, strategy);
		reqTemplate.setBatchOption(batchOption);
		
		// process user input for user data
		HashMap<String, String> values = new HashMap<String, String>();
		for (Entry<String, ?> entry : userData.entrySet()) {
			Object v = entry.getValue();
			if (v instanceof String) {
				values.put(entry.getKey(), (String) v);
			} 
			else if (v instanceof List) {
				List lvs = (List) v;
				for (int i = 0; i < lvs.size(); i++) {
					String lv = (String) lvs.get(i);
					String nv = checkLatestValue(lv);
					if (nv != null) {
						lvs.set(i, nv);
					}
				}
				values.put(entry.getKey(), JsonUtil.encode(v));
			}
			else {
				values.put(entry.getKey(), JsonUtil.encode(v));
			}
		}
		
		if (hosts != null) {
			reqTemplate.setHosts(hosts);
		}
		reqTemplate.setTemplateValuesForAllHosts(new HostTemplateValues().addNewTemplateValue(values));
		return reqTemplate;
	}
	
	/**
	 * convenient method of matching and replacing latest pkg
	 */
	static Pattern latestPattern = Pattern.compile("(.*)<(.*)\\.latest>$");
	static Pattern cmIpPatternE = Pattern.compile("(.*)<(cmExternalIp)>(.*)");
	static Pattern cmIpPatternI = Pattern.compile("(.*)<(cmInternalIp)>(.*)");
	private static String checkLatestValue(String v) throws IOException {
		Matcher matcher = latestPattern.matcher(v);
		boolean replaced = false;
		if (matcher.matches()) {
			List<ICronusPkg> pkgs = UserDataProviderFactory.getCronusPkgData()
					.getPkgsByFilter("name", matcher.group(2));
			if (!pkgs.isEmpty()) {
				v = matcher.replaceFirst(String.format("$1%s", pkgs.get(0).getName()));
				replaced = true;
			}
		}
		matcher = cmIpPatternE.matcher(v);
		if (matcher.matches()) {
			v = matcher.replaceFirst(String.format("$1%s$3", VarUtils.externalIp));
			replaced = true;
		}
		matcher = cmIpPatternI.matcher(v);
		if (matcher.matches()) {
			v = matcher.replaceFirst(String.format("$1%s$3", VarUtils.internalIp));
			replaced = true;
		}
		return replaced ? v : null;
	}
	
	public static void main(String[] args) {
			String pkg = "http://<cmExternalIp>:9000/agent/downloadPkg/<simple_pyserver-*.all.cronus.latest>";
			Matcher matcher = latestPattern.matcher(pkg);
			System.out.println(matcher.matches());
			System.out.println(matcher.group(2));
			pkg = matcher.replaceFirst("$1newvalue");
			System.out.println(pkg);
			matcher = cmIpPatternE.matcher(pkg);
			System.out.println(matcher.matches());
			System.out.println(matcher.group(1));
			pkg = matcher.replaceFirst("$1newvalue$3");
			System.out.println(pkg);
			
	}

}
