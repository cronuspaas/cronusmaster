package resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeGroupImpl implements INodeGroup {
	
	private String name;
	private String type;
	private List<String> nodeList = new ArrayList<String>();

	public NodeGroupImpl() {}
	public NodeGroupImpl(String name) {
		this.name = name;
	}
	
	public NodeGroupImpl addNodesToList(List<String> nodes) {
		nodeList.addAll(nodes);
		return this;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<String> getNodeList() {
		return nodeList;
	}
	@Override
	public String getType() {
		return type;
	}
	@Override
	public void setType(String ngType) {
		this.type = ngType;
	}

}
