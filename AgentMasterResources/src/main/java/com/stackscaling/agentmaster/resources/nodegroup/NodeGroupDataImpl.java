package com.stackscaling.agentmaster.resources.nodegroup;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stackscaling.agentmaster.resources.DataType;
import com.stackscaling.agentmaster.resources.IUserDataDao;
import com.stackscaling.agentmaster.resources.utils.DateUtils;

/**
 * node group configs impl
 *
 * @author binyu
 *
 */
public class NodeGroupDataImpl implements INodeGroupData {

	static Logger LOG = LoggerFactory.getLogger(NodeGroupDataImpl.class);

	private int nodeCount;

	@Autowired(required=true)
	private IUserDataDao userConfigs;

	/** loaded node groups */
	private HashMap<String, INodeGroup> nodeGroups = null;

	private DataType dataType = DataType.NODEGROUP;

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	@Override
	public Map<String, INodeGroup> getAllNodeGroups() throws IOException {
		if (nodeGroups == null) {
			load();
		}
		return nodeGroups;
	}

	@Override
	public INodeGroup getNodeGroupByName(String name) throws IOException {
		if (nodeGroups == null) {
			load();
		}
		if (nodeGroups.containsKey(name)) {
			return nodeGroups.get(name);
		}
		throw new InvalidObjectException(String.format(
				"Nodegroup of name %s does not exist", name));
	}

	@Override
	public void save(String ngName, String configFileContent) throws IOException {
		userConfigs.saveData(dataType, ngName, configFileContent);
		load();
	}

	@Override
	public void load() throws IOException {

		HashMap<String, INodeGroup> nodeGroups = new HashMap<String, INodeGroup>();
		int nodeCount = 0;

		List<String> ngNames = userConfigs.listNames(dataType);
		for (String ngName : ngNames) {
			String content = userConfigs.readData(DataType.NODEGROUP, ngName);

			String[] lines = content.split("\n");
			List<String> tmpNodeList = new ArrayList<String>();
			for (String line : lines) {
				// trim the comments or empty lines
				if (StringUtil.isNullOrEmptyAfterTrim(line)) {
					continue;
				}
				tmpNodeList.add(line);
			}
			NodeGroupImpl nodeGroupImpl = new NodeGroupImpl(ngName);
			nodeGroupImpl.setType(DataType.NODEGROUP.name());
			nodeGroupImpl.addNodesToList(tmpNodeList);
			nodeCount += tmpNodeList.size();
			nodeGroups.put(ngName, nodeGroupImpl);
		}

		LOG.info("Completed NodeGroup loading with node group count: "
				+ nodeGroups.size() + " at " + DateUtils.getNowDateTimeStr());

		this.nodeGroups = nodeGroups;
		this.nodeCount = nodeCount;
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
	public int getNodeCount() {
		return nodeCount;
	}

}
