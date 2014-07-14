package resources.log;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import resources.IUserDataDao.DataType;

@Component("cmdLogger")
@Scope("singleton")
public class CmdLogger extends LoggerImpl<CmdLog> {

	public CmdLogger() {
		super();
		this.dataType = DataType.CMDLOG;
		this.logDoKlass = CmdLog.class;
	}
}
