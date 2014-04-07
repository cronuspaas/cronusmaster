package resources.ebay.flow;
import java.io.File;

import junit.framework.TestCase;

import org.lightj.RuntimeContext;
import org.lightj.initialization.BaseModule;
import org.lightj.initialization.InitializationException;
import org.lightj.initialization.InitializationProcessor;
import org.lightj.initialization.ShutdownException;
import org.lightj.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.vfs.VirtualFile;


public abstract class BaseTestCase extends TestCase {
	
	static Logger logger = LoggerFactory.getLogger(BaseTestCase.class);
	
	private InitializationProcessor initializer = null;

	public BaseTestCase() {}
	
	public String getConfigRoot() {
		return VirtualFile.fromRelativePath("conf").getRealFile().getPath();
	}
	
	/**
	 * dependent modules
	 * @return
	 */
	protected abstract BaseModule[] getDependentModules();
	
	/**
	 * Prepare to run the test suite.
	 * @throws InitializationException 
	 */
	protected void setUp() throws Exception {

		if (getDependentModules() != null) {
			logger.warn("Test case" + this.getName());
			initializer = new InitializationProcessor(getDependentModules());
			initializer.initialize();
		}

		afterInitialize(System.getProperty("user.dir"));
	}
	
	protected void tearDown() throws Exception {
		if (initializer != null) {
			initializer.shutdown();
		}
	}
	
	protected void afterInitialize(String home) throws InitializationException {
	}

	protected void afterShutdown() throws ShutdownException {
	}

}
