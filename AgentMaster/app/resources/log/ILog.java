package resources.log;

import java.util.Map;

import resources.IUserDataDao.DataType;
import resources.nodegroup.INodeGroup;

/**
 * log interface (cmdlog, joblog, flowlog)
 * @author biyu
 *
 */
public interface ILog {
	
	/** log uuid, combination of some metadata */
	public String uuid();
	
	public String getTimestamp();
	
	public Map<String, String> getUserData();
	
	public void setUserData(Map<String, String> userData);
	
	public INodeGroup getNodeGroup();
	
	public void setNodeGroup(INodeGroup nodeGroup);	
	
	public String getCommandKey();
	
	public DataType getCommandType();

	public String getStatus();
	
	public String getDisplayProgress();
	
	public String getStatusDetail();
	
}
