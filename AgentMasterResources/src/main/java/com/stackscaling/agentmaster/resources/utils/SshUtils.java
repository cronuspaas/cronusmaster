package com.stackscaling.agentmaster.resources.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * ssh util
 *
 * @author binyu
 *
 */
public class SshUtils {

	/**
	 * key based identity interface
	 * @author binyu
	 *
	 */
	public interface IdentityKey {
		/** private key file */
		public String privateKeyFile();
		/** optional passphrase */
		public String passphrase();
	}

	/**
	 * cmd output
	 * @author binyu
	 *
	 */
	public static class SshCmdOutput {
		public byte[] stdout;
		public byte[] stderr;
		public int exitCode;
	}

	/**
	 * connect ssh via key
	 * @param user
	 * @param host
	 * @param port
	 * @param identityKey
	 * @return
	 * @throws JSchException
	 */
	public static Session connectViaKey(String user, String host, int port, IdentityKey identityKey)
			throws JSchException {
		JSch jsch = new JSch();

		if (identityKey.passphrase() != null) {
			jsch.addIdentity(identityKey.privateKeyFile(), identityKey.passphrase());
		}
		else {
			jsch.addIdentity(identityKey.privateKeyFile());
		}

		System.out.println("identity added");

		Session session = jsch.getSession(user, host, port);
		System.out.println("session created.");

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();
		System.out.println("session connected.....");
		return session;
	}

	/**
	 * disconnect
	 * @param session
	 */
	public static void disconnect(Session session) {
		if (session != null) {
			session.disconnect();
		}
	}

	/**
	 * execute cmd via ssh
	 *
	 * @param session
	 * @param command
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	public static int execCmd(Session session, String command) throws JSchException, IOException
	{
		Channel channel = session.openChannel("exec");

		try {
			((ChannelExec) channel).setCommand(command);

			// channel.setInputStream(System.in);
			channel.setInputStream(null);

			// channel.setOutputStream(System.out);

			// FileOutputStream fos=new FileOutputStream("/tmp/stderr");
			// ((ChannelExec)channel).setErrStream(fos);
			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			return channel.getExitStatus();
		} finally {
			channel.disconnect();
		}
	}

	/**
	 * scp a file to remote host
	 *
	 * @param lfile
	 * @param user
	 * @param host
	 * @param rfile
	 */
	public static void scpTo(Session session, String lfile, String rfile) {

		FileInputStream fis = null;
		try {
			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.exit(0);
			}

			File _lfile = new File(lfile);

			if (ptimestamp) {
				command = "T " + (_lfile.lastModified() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
				out.write(command.getBytes());
				out.flush();
				if (checkAck(in) != 0) {
					System.exit(0);
				}
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}
			out.close();

			channel.disconnect();

		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

}
