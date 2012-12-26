package com.wat.melody.cloud.management;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.management.messages";

	public static String MgmtMsg_INTRO;
	public static String MgmtMsg_RESUME;
	public static String MgmtMsg_FAILED;

	public static String MgmtEx_SSH_MGMT_ENABLE_TIMEOUT;

	public static String MgmtEx_WINRM_MGMT_NOT_SUPPORTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
