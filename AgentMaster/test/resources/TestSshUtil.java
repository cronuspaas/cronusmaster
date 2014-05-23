package resources;

import junit.framework.Assert;

import org.junit.Test;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import resources.utils.SshUtils;
import resources.utils.SshUtils.IdentityKey;

public class TestSshUtil {
	
	private static Session connect() throws JSchException {
		
		Session session = SshUtils.connectViaKey("binyu", "192.168.1.126", 22, new IdentityKey() {
			
			@Override
			public String privateKeyFile() {
				return "c:/Users/binyu/.ssh/id_rsa";
			}
			
			@Override
			public String passphrase() {
				return "il0v3j3nny";
			}
		});
		
		return session;
	}
	
	@Test
	public void testKeyBasedConnect() throws Exception {
		Session session = connect();
		Assert.assertTrue(session != null);
		session.disconnect();
	}
	
	@Test
	public void testExecCmd() throws Exception {
		Session session = connect();
		int exitCode = SshUtils.execCmd(session, "echo hello world");
		Assert.assertEquals(0, exitCode);
		session.disconnect();
	}
}
