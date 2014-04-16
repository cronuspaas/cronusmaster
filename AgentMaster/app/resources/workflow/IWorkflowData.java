package resources.workflow;

import java.io.IOException;
import java.util.Map;

public interface IWorkflowData {

	/**
	 * get all commands back
	 * @return
	 */
	public Map<String, IWorkflowMeta> getAllFlows();
	
	/**
	 * find template by name
	 * @param name
	 * @return
	 */
	public IWorkflowMeta getFlowByName(String name);


	/**
	 * save command configs
	 * @param content
	 * @throws IOException
	 */
	public void save(String name, String content) throws IOException;
	

	/**
	 * save command configs
	 * @param content
	 * @throws IOException
	 */
	public void delete(String name) throws IOException;
	
	/**
	 * load data
	 * @throws IOException
	 */
	public void load() throws IOException;
	
}
