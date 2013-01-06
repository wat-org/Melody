package com.wat.melody.plugin.ssh.common.jsch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.ssh.common.jsch.messages";

	public static String SshEx_FAILED_TO_CONNECT;
	public static String SshEx_EXEC_INTERRUPTED;
	public static String SshMsg_GRACEFULL_SHUTDOWN;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
