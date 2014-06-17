package resources.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScriptImpl implements IScript {
	
	private String name;
	private String type;
	private String content;

	public ScriptImpl() {}
	public ScriptImpl(String name) {
		this.name = name;
	}
	public ScriptImpl(String name, String type, String content) {
		this.name = name;
		this.type = type;
		this.content = content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
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
	public String getType() {
		return type;
	}

	@Override
	public void setType(String ngType) {
		this.type = ngType;
	}

}
