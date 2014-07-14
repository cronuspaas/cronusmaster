package resources.log;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import resources.IUserDataDao.DataType;

@Component("flowLogger")
@Scope("singleton")
public class FlowLogger extends LoggerImpl<FlowLog> {

	public FlowLogger() {
		super();
		this.dataType = DataType.FLOWLOG;
		this.logDoKlass = FlowLog.class;
	}
}
