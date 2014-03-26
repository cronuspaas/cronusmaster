package resources.nodegroup;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.utils.DateUtils;
import models.utils.LogUtils;

import org.lightj.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.IUserDataDao.DataType;

/**
 * node group configs impl
 * 
 * @author binyu
 * 
 */
public class NodeGroupDataImpl implements INodeGroupData {

	@Autowired(required = true)
	private IUserDataDao userConfigs;

	/** loaded node groups */
	private HashMap<String, INodeGroup> nodeGroups = null;

	private DataType dataType;

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
			nodeGroups.put(ngName, nodeGroupImpl);
		}

		LogUtils.printLogNormal("Completed NodeGroup loading with node group count: "
				+ nodeGroups.size() + " at " + DateUtils.getNowDateTimeStr());

		this.nodeGroups = nodeGroups;
	}

	@Override
	public IUserDataDao getUserDataDao() {
		return userConfigs;
	}

	@Override
	public void setUserDataDao(IUserDataDao userConfigs) {
		this.userConfigs = userConfigs;
	}

}
