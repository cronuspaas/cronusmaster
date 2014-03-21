package resources;

import java.io.IOException;

import models.utils.VarUtils.CONFIG_FILE_TYPE;

import org.lightj.util.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import resources.INodeGroupData.NodeGroupType;
import resources.IUserDataDao.DataType;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfigBean;

@Configuration
public class UserDataProvider {
	
	/**
	 * IUserConfigs impl
	 * @return
	 */
	public @Bean(name="userConfigs") @Scope("singleton") IUserDataDao userConfigs() {
		return new FileUserDataDaoImpl();
	}
	
	/**
	 * IUserConfigsDao
	 * @return
	 * @throws IOException
	 */
	public @Bean(name="commandConfigs") @Scope("singleton") ICommandData commandConfigs() throws IOException {
		CommandDataImpl dao = new CommandDataImpl();
		return dao;
	}
	
	/**
	 * predefined node group
	 * @return
	 */
	public @Bean @Scope("singleton") INodeGroupData predefinedNodeGroup() {
		return new NodeGroupDataImpl(DataType.NODEGROUP.name());
	}

	/**
	 * adhoc node group
	 * @return
	 */
	public @Bean @Scope("prototype") INodeGroupData adhocNodeGroup() {
		return new AdhocNodeGroupDataImpl();
	}
	
	/**
	 * job logger
	 * @return
	 */
	public @Bean @Scope("singleton") IJobLogger jobLogger() {
		return new JobLoggerImpl();
	}
	
	/**
	 * return current IUserConfigsDao
	 * @return
	 */
	public static ICommandData getCommandConfigs() {
		return SpringContextUtil.getBean("resources", "commandConfigs", ICommandData.class);
	}
	
	/**
	 * get node group configs of a type
	 * @param type
	 * @return
	 */
	public static INodeGroupData getNodeGroupOfType(DataType type) {
		INodeGroupData ng = null;
		switch(type) {
		case NODEGROUP:
			ng = SpringContextUtil.getBean("resources", "predefinedNodeGroup", INodeGroupData.class);
			break;
		case ADHOCNODEGROUP:
			ng = SpringContextUtil.getBean("resources", "adhocNodeGroup", INodeGroupData.class);
			break;
		default:
			throw new IllegalArgumentException(String.format("Invalid nodegroup type %s", type));
		}
		return ng;
	}
	
	/**
	 * get job logger
	 * @return
	 */
	public static IJobLogger getJobLogger() {
		IJobLogger logger = SpringContextUtil.getBean("resources", "jobLogger", IJobLogger.class);
		return logger;
	}
	
	/**
	 * get user data dao
	 * @return
	 */
	public static IUserDataDao getUserDataDao() {
		return SpringContextUtil.getBean("resources", IUserDataDao.class);
	}
	
	/**
	 * reload all configs
	 * @throws IOException
	 */
	public static void reloadAllConfigs() throws IOException {
		getNodeGroupOfType(DataType.NODEGROUP).load(DataType.NODEGROUP.name());
		getCommandConfigs().load();
	}

}
