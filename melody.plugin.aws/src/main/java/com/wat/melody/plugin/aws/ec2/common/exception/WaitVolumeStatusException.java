package com.wat.melody.plugin.aws.ec2.common.exception;

import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.plugin.aws.ec2.common.VolumeState;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WaitVolumeStatusException extends AwsException {

	private static final long serialVersionUID = 4897604537494850805L;

	private final DiskDevice moDisk;
	private final String msVolumeId;
	private final VolumeState moExpectedState;
	private final long mlTimeout;

	public WaitVolumeStatusException(DiskDevice disk, String sVolumeId,
			VolumeState expectedState, long timeout) {
		if (disk == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Disk.");
		}
		if (sVolumeId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws Volume Id).");
		}
		if (expectedState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid VolumeState.");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a positive Integer (a timeout).");
		}
		moDisk = disk;
		msVolumeId = sVolumeId;
		moExpectedState = expectedState;
		mlTimeout = timeout;
	}

	public DiskDevice getDisk() {
		return moDisk;
	}

	public String getVolumeId() {
		return msVolumeId;
	}

	public VolumeState getExpectedState() {
		return moExpectedState;
	}

	public long getTimeout() {
		return mlTimeout;
	}
}
