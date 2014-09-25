package com.stackscaling.agentmaster.resources.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.session.FlowContext;
import org.lightj.session.FlowModule;
import org.lightj.session.FlowSession;
import org.lightj.session.FlowSessionFactory;
import org.lightj.session.FlowType;
import org.lightj.task.BatchOption;
import org.lightj.task.BatchOption.Strategy;
import org.lightj.util.JsonUtil;
import org.lightj.util.MapListPrimitiveJsonParser;
import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.UserDataMeta;
import com.stackscaling.agentmaster.resources.nodegroup.INodeGroup;
import com.stackscaling.agentmaster.resources.utils.DataUtil;

public class WorkflowDataImpl implements IWorkflowData {

	static Logger LOG = LoggerFactory.getLogger(WorkflowDataImpl.class);

	@Autowired(required=true)
	private IUserDataDao userConfigs;

	private HashMap<String, IWorkflowMeta> flows = new HashMap<String, IWorkflowMeta>();

	@Override
	public Map<String, IWorkflowMeta> getAllFlows() {
		return Collections.unmodifiableMap(flows);
	}

	@Override
	public IWorkflowMeta getFlowByName(String name) {
		return flows.get(name);
	}

	@Override
	public void load() throws IOException {
		HashMap<String, IWorkflowMeta> flows = new HashMap<String, IWorkflowMeta>();

		// load from dir
		List<UserDataMeta> flowMetas = userConfigs.listNames(DataType.WORKFLOW);
		for (UserDataMeta flowMeta : flowMetas) {
			String content = userConfigs.readData(DataType.WORKFLOW, flowMeta.getName());
			if (!StringUtil.isNullOrEmpty(content)) {
				WorkflowMetaImpl flow = JsonUtil.decode(content, WorkflowMetaImpl.class);
				flows.put(flowMeta.getName(), flow);
			}
		}

		// load predefined
		for (FlowType type : FlowModule.getRegisteredFlowTypes()) {
			WorkflowMetaImpl flow = new WorkflowMetaImpl();
			flow.setFlowName(type.value());
			flow.setFlowDescription(type.desc());
			flow.setFlowType(IWorkflowMeta.FlowType.Predefined.name());
			flow.setFlowClass(type.getFlowKlass().getSimpleName());
			try {
				FlowContext ctx = type.getCtxKlass().newInstance();
				flow.setUserData(ctx.getUserDataInfo());
			} catch (Throwable t) {
				LOG.error("%s, %s", t.getMessage(), "error get flow userdata info");
			}
			flows.put(flow.getFlowName(), flow);
			userConfigs.saveData(DataType.WORKFLOW, flow.getFlowName(), JsonUtil.encode(flow));
		}

		this.flows = flows;
	}

	@Override
	public void save(String name, String content) throws IOException
	{
		WorkflowMetaImpl wfMeta = JsonUtil.decode(content, WorkflowMetaImpl.class);
		if (StringUtil.equalIgnoreCase(IWorkflowMeta.FlowType.Predefined.name(), wfMeta.getFlowType())) {
			throw new IOException("Cannot save a predefined flow " + name);
		}
		userConfigs.saveData(DataType.WORKFLOW, name, content);
	}

	@Override
	public void delete(String name) throws IOException {
		if (StringUtil.equalIgnoreCase(IWorkflowMeta.FlowType.Predefined.name(), flows.get(name).getFlowType())) {
			throw new IOException("Cannot delete a predefined flow " + name);
		}
		userConfigs.deleteData(DataType.WORKFLOW, name);
	}

	
	/**
	 * create workflow session from request (user or internal)
	 * @param ng
	 * @param workflow
	 * @param options
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static FlowSession createFlowByRequest(INodeGroup ng, IWorkflowMeta workflow, Map<String, String> options) throws IOException 
	{
		String varValues = DataUtil.getOptionValue(options, "var_values", "{}").trim();
		Map<String, Object> values = (Map<String, Object>) MapListPrimitiveJsonParser.parseJson(varValues);
		
		Strategy strategy = Strategy.valueOf(DataUtil.getOptionValue(options, "thrStrategy", "UNLIMITED"));
		int maxRate = Integer.parseInt(DataUtil.getOptionValue(options, "thr_rate", "1000"));
		values.put("batchOption", new BatchOption(maxRate, strategy));
		
		String[] hosts = ng.getHosts();
		values.put("hosts", Arrays.asList(hosts));
		
		FlowSession flow = FlowSessionFactory.getInstance().createSession(workflow.getFlowName());
		flow.getSessionContext().addUserData(values);
		return flow;
	}

}
