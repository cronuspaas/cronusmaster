package resources;

import java.io.IOException;
import java.util.List;

import org.lightj.util.StringUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import play.Play;

@Component
@Scope("singleton")
public class DelegateUserDataDaoImpl implements IUserDataDao {

	/** actual data dao used */
	private IUserDataDao userDataDao;
	
	public DelegateUserDataDaoImpl() {
		String userDataDaoType = Play.configuration.getProperty("agentmaster.userDataDao").toString();
		if (StringUtil.equalIgnoreCase("file", userDataDaoType)) {
			userDataDao = new FileUserDataDaoImpl();
		} 
		else if (StringUtil.equalIgnoreCase("aws_s3", userDataDaoType)) {
			userDataDao = new S3UserDataDaoImpl();
		} 
		else if (StringUtil.equalIgnoreCase("openstack_swift", userDataDaoType)) {
			userDataDao = new SwiftUserDataDaoImpl();
		}
		else {
			throw new RuntimeException("undefined user data dao type " + userDataDaoType);
		}
	}

	@Override
	public String readData(DataType type, String name) throws IOException {
		return userDataDao.readData(type, name);
	}

	@Override
	public void saveData(DataType type, String name, String content)
			throws IOException {
		userDataDao.saveData(type, name, content);
	}

	@Override
	public void deleteData(DataType type, String name) throws IOException {
		userDataDao.deleteData(type, name);
	}

	@Override
	public List<String> listNames(DataType type) {
		return userDataDao.listNames(type);
	}

}
