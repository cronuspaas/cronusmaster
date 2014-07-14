package resources.script;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import resources.IUserDataDao.DataType;

@Component("predefinedScripts")
@Scope("singleton")
public class PredefinedScriptData extends ScriptDataImpl {
	
	public PredefinedScriptData() {
		super();
		this.dataType = DataType.SCRIPT;
	}

}
