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
 * @author binyu
 *
 */
public class NodeGroupDataImpl implements INodeGroupData {
	
	@Autowired(required=true)
	private IUserDataDao userConfigs;
	
	private boolean isLoaded = false;
	
	/** loaded node groups */
	private HashMap<String, INodeGroup> nodeGroups = new HashMap<String, INodeGroup>();
	
	/** source */
	private final String source;
	
	public NodeGroupDataImpl(String source) {
		this.source = source;
	}

	@Override
	public Map<String, INodeGroup> getAllNodeGroups() throws IOException {
		load(DataType.NODEGROUP.name());
		return nodeGroups;
	}

	@Override
	public INodeGroup getNodeGroupByName(String name) throws IOException 
	{
		load(DataType.NODEGROUP.name());
		if (nodeGroups.containsKey(name)) {
			return nodeGroups.get(name);
		}
		throw new InvalidObjectException(String.format("Nodegroup of name %s does not exist", name));
	}

	@Override
	public void save(String configFileContent) throws IOException 
	{
		validate(configFileContent);
		userConfigs.saveConfigFile(DataType.valueOf(source), null, configFileContent);
		isLoaded = false;
	}

	@Override
	public void load(String configFileName) throws IOException {
		
		if (!isLoaded) {
			HashMap<String, INodeGroup> nodeGroups = new HashMap<String, INodeGroup>();
			String content = userConfigs.readConfigFile(DataType.NODEGROUP, configFileName);

			String[] lines = content.split("\n");
			NodeGroupImpl nodeGroupImpl = null;
			List<String> tmpNodeList = new ArrayList<String>();
			boolean isNode = false;
			for (String line : lines) {
				// trim the comments or empty lines
				if (StringUtil.isNullOrEmptyAfterTrim(line)) {
					continue;
				}

				if (line.startsWith(NODEGROUP_NAME_TAG)) {
					String tokens[] = line.split("=");
					nodeGroupImpl = new NodeGroupImpl(tokens[1]);
					nodeGroupImpl.setType(DataType.NODEGROUP.name());
				}
				else if (line.startsWith(NODEGROUP_LIST_START_TAG)) {
					tmpNodeList.clear();
					isNode = true;
				}
				else if (line.startsWith(NODEGROUP_LIST_END_TAG)) {
					nodeGroupImpl.addNodesToList(tmpNodeList);
					if (nodeGroupImpl != null) {
						nodeGroups.put(nodeGroupImpl.getName(), nodeGroupImpl);
					}
					isNode = false;
				}
				else if (isNode) {
					tmpNodeList.add(line);
				}

			}

			LogUtils.printLogNormal
			 			("Completed NodeGroup loading with node group count: "
			 					+ nodeGroups.size() + " at "
			 					+ DateUtils.getNowDateTimeStr());
			
			this.nodeGroups = nodeGroups;
			isLoaded = true;
		}
	}

	@Override
	public void validate(String configFileContent) throws IOException {
		String[] lines = configFileContent.split("\n");
		int nameCnt=0, startCnt=0, endCnt = 0;
		for (String line : lines) {
			// trim the comments or empty lines
			if (StringUtil.isNullOrEmptyAfterTrim(line)) {
				continue;
			}

			if (line.startsWith(NODEGROUP_NAME_TAG)) {
				nameCnt++;
				assert nameCnt == (startCnt+1);
				assert nameCnt == (endCnt+1);
			}
			else if (line.startsWith(NODEGROUP_LIST_START_TAG)) {
				startCnt++;
				assert nameCnt == startCnt;
				assert nameCnt == (endCnt+1);
			}
			else if (line.startsWith(NODEGROUP_LIST_END_TAG)) {
				endCnt++;
				assert nameCnt == startCnt;
				assert nameCnt == endCnt;
			}
		}
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
