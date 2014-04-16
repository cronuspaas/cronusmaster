package resources.nodegroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import models.utils.DateUtils;

import org.lightj.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import resources.IUserDataDao;
import resources.IUserDataDao.DataType;

public class AdhocNodeGroupDataImpl implements INodeGroupData {
	
	@Autowired(required=true)
	private IUserDataDao userConfigs;

	private NodeGroupImpl nodeGroup;

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
		if (nodeGroup != null) {
			HashMap<String, INodeGroup> ngs = new HashMap<String, INodeGroup>();
			ngs.put(nodeGroup.getName(), nodeGroup);
			return ngs;
		}
		return Collections.emptyMap();
	}

	@Override
	public INodeGroup getNodeGroupByName(String name) throws IOException {
		if (nodeGroup == null) {
			nodeGroup = new NodeGroupImpl();
			nodeGroup.setType(DataType.ADHOCNODEGROUP.name());
			nodeGroup.setName(String.format("NG-%s", DateUtils.getNowDateTimeStrSdsm()));
			nodeGroup.addNodesToList(Arrays.asList(name.split("\n")));
			save(nodeGroup.getName(), JsonUtil.encode(nodeGroup));
		}
		return nodeGroup;
	}

	@Override
	public void save(String ngName, String configFileContent) throws IOException {
		if (nodeGroup != null) {
			userConfigs.saveData(DataType.ADHOCNODEGROUP, ngName, configFileContent);
		}
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
