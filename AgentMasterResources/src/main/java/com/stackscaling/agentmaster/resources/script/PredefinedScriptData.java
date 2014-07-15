package com.stackscaling.agentmaster.resources.script;

import com.stackscaling.agentmaster.resources.IUserDataDao.DataType;

public class PredefinedScriptData extends ScriptDataImpl {

	public PredefinedScriptData() {
		super();
		this.dataType = DataType.SCRIPT;
	}

}
