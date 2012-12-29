package com.wat.melody.plugin.ssh.common;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.LogThreshold;
import com.wat.melody.plugin.ssh.common.exception.SshException;

abstract public class AbstractSshOperation extends AbstractSshTask implements
		JSchConnectionDatas {

	/**
	 * The 'login' XML attribute
	 */
	public static final String LOGIN_ATTR = "login";

	/**
	 * The 'host' XML attribute
	 */
	public static final String HOST_ATTR = "host";

	/**
	 * The 'port' XML attribute
	 */
	public static final String PORT_ATTR = "port";

	/**
	 * The 'password' XML attribute
	 */
	public static final String PASSWORD_ATTR = "password";

	/**
	 * The 'trust' XML attribute
	 */
	public static final String TRUST_ATTR = "trust";

	private String msLogin;
	private Host moHost;
	private Port moPort;
	private String msPassword;
	private Boolean mbTrust;

	public AbstractSshOperation() {
		super();
		initLogin();
		initHost();
		initPort();
		initPassword();
		initTrust();
	}

	private void initLogin() {
		msLogin = null;
	}

	private void initHost() {
		moHost = null;
	}

	private void initPort() {
		try {
			moPort = new Port(Port.SSH);
		} catch (IllegalPortException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Port with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private void initPassword() {
		msPassword = null;
	}

	private void initTrust() {
		mbTrust = false;
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		// Validate task attribute
		if (getPassword() != null && getKeyPairName() != null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_BOTH_PASSWORD_OR_PK_ATTR, PASSWORD_ATTR,
					KEYPAIR_NAME_ATTR));
		} else if (getPassword() == null && getKeyPairName() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_PASSWORD_OR_PK_ATTR, PASSWORD_ATTR,
					KEYPAIR_NAME_ATTR));
		}
	}

	/**
	 * <p>
	 * Open a ssh session.
	 * </p>
	 * 
	 * @return the opened session.
	 * 
	 * @throws SshException
	 */
	public Session openSession() throws SshException {
		Session session = JSchHelper.openSession(this, getPluginConf());
		session.getConnectThread().setName(Thread.currentThread().getName());
		return session;
	}

	/**
	 * <p>
	 * Open a sftp channel.
	 * </p>
	 * 
	 * @return the opened sftp channel.
	 * 
	 * @throws SshException
	 */
	public ChannelSftp openSftpChannel(Session session) throws SshException {
		return JSchHelper.openSftpChannel(session, getPluginConf());
	}

	public int execSshCommand(Session session, String sCommand,
			String outputPrefix) throws SshException, InterruptedException {
		LoggerOutputStream out = new LoggerOutputStream(outputPrefix
				+ " [STDOUT]", LogThreshold.DEBUG);
		LoggerOutputStream err = new LoggerOutputStream(outputPrefix
				+ " [STDERR]", LogThreshold.ERROR);
		return JSchHelper.execSshCommand(session, sCommand, out, err);
	}

	public int execSshCommand(String sCommand, String outputPrefix)
			throws SshException, InterruptedException {
		Session session = openSession();
		try {
			return execSshCommand(session, sCommand, outputPrefix);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	public String getLogin() {
		return msLogin;
	}

	@Attribute(name = LOGIN_ATTR, mandatory = true)
	public String setLogin(String sLogin) throws SshException {
		if (sLogin == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (sLogin.trim().length() == 0) {
			throw new SshException(Messages.bind(
					Messages.SshEx_EMPTY_LOGIN_ATTR, sLogin));
		}
		String previous = getLogin();
		msLogin = sLogin;
		return previous;
	}

	public Host getHost() {
		return moHost;
	}

	@Attribute(name = HOST_ATTR, mandatory = true)
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Host previous = getHost();
		moHost = host;
		return previous;
	}

	public Port getPort() {
		return moPort;
	}

	@Attribute(name = PORT_ATTR)
	public Port setPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Port previous = getPort();
		moPort = port;
		return previous;
	}

	@Override
	public String getPassword() {
		return msPassword;
	}

	@Attribute(name = PASSWORD_ATTR)
	public String setPassword(String sPassword) {
		if (sPassword == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassword();
		msPassword = sPassword;
		return previous;
	}

	public boolean getTrust() {
		return mbTrust;
	}

	@Attribute(name = TRUST_ATTR)
	public boolean setTrust(boolean b) {
		boolean previous = getTrust();
		mbTrust = b;
		return previous;
	}

	/**
	 * @param message
	 *            ignored
	 * 
	 * @return true always
	 */
	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	/**
	 * @param passwordPrompt
	 *            ignored
	 * 
	 * @return true always
	 */
	@Override
	public boolean promptPassword(String passwordPrompt) {
		return true;
	}

	/**
	 * @param message
	 *            ignored
	 * 
	 * @return the value of trustAllCertificates
	 */
	@Override
	public boolean promptYesNo(String message) {
		return getTrust();
	}

	/**
	 * @param message
	 *            ignored
	 */
	@Override
	public void showMessage(String message) {
	}

	/**
	 * Implementation of {@link UIKeyboardInteractive#promptKeyboardInteractive}
	 * .
	 * 
	 * @param destination
	 *            not used.
	 * @param name
	 *            not used.
	 * @param instruction
	 *            not used.
	 * @param prompt
	 *            the method checks if this is one in length.
	 * @param echo
	 *            the method checks if the first element is false.
	 * @return the password in a size one array if there is a password and if
	 *         the prompt and echo checks pass.
	 */
	@Override
	public String[] promptKeyboardInteractive(String destination, String name,
			String instruction, String[] prompt, boolean[] echo) {
		if (prompt.length != 1 || echo[0] || getPassword() == null) {
			return null;
		}
		String[] response = new String[1];
		response[0] = getPassword();
		return response;
	}

}
