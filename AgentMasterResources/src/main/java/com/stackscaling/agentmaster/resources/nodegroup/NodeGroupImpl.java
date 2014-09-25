package com.stackscaling.agentmaster.resources.nodegroup;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stackscaling.agentmaster.resources.BaseUserData;

public class NodeGroupImpl extends BaseUserData implements INodeGroup {

	private String type;
	private List<String> nodeList = new ArrayList<String>();

	public NodeGroupImpl() {}
	public NodeGroupImpl(String name) {
		this.name = name;
	}
	public NodeGroupImpl(String name, String type, List<String> nodeList) {
		this.name = name;
		this.type = type;
		this.nodeList = nodeList;
	}

	public NodeGroupImpl addNodesToList(List<String> nodes) {
		nodeList.addAll(nodes);
		return this;
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
	@JsonIgnore
	@Override
	public String[] getHosts() {
		return (nodeList!=null && !nodeList.isEmpty()) ? nodeList.toArray(new String[0]) : null;
	}

}
