package com.stackscaling.agentmaster.resources.nodegroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

public class AdhocNodeGroupDataImpl implements INodeGroupData {

	@Autowired(required=true)
	private IUserDataDao userConfigs;

	@Override
	public IUserDataDao getUserDataDao() {
		return userConfigs;
	}

	@Override
	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userConfigs = userConfigs;
	}

	@Override
	public Map<String, INodeGroup> getAllNodeGroups() throws IOException {
		return Collections.emptyMap();
	}

	@Override
	public INodeGroup getNodeGroupByName(String name) throws IOException {
		NodeGroupImpl nodeGroup = new NodeGroupImpl();
		nodeGroup.setType(DataType.ADHOCNODEGROUP.name());
		nodeGroup.setName(String.format("NG-%s", DateUtils.getNowDateTimeStrConcise()));
		nodeGroup.addNodesToList(Arrays.asList(name.split("\n")));
		save(nodeGroup.getName(), JsonUtil.encode(nodeGroup));
		return nodeGroup;
	}

	@Override
	public void save(String ngName, String configFileContent) throws IOException {
		userConfigs.saveData(DataType.ADHOCNODEGROUP, ngName, configFileContent);
	}

	@Override
	public void load() throws IOException {
		// noop
	}

	@Override
	public int getNodeCount() {
		return 0;
	}

}
