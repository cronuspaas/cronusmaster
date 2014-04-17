package resources.workflow;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.session.FlowContext;
import org.lightj.session.FlowModule;
import org.lightj.session.FlowType;
import org.lightj.util.JsonUtil;
import org.lightj.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.IUserDataDao.DataType;

public class WorkflowDataImpl implements IWorkflowData {
	
	@Autowired
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
		List<String> flowNames = userConfigs.listNames(DataType.WORKFLOW);
		for (String flowName : flowNames) {
			String content = userConfigs.readData(DataType.WORKFLOW, flowName);
			if (!StringUtil.isNullOrEmpty(content)) {
				WorkflowMetaImpl flow = JsonUtil.decode(content, WorkflowMetaImpl.class);
				flows.put(flowName, flow);
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
				play.Logger.error(t, "error get flow userdata info");
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

}
