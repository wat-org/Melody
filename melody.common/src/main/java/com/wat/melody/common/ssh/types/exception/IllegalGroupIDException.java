package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalGroupIDException extends MelodyException {

	private static final long serialVersionUID = -8676564432134334572L;

	public IllegalGroupIDException() {
		super();
	}

	public IllegalGroupIDException(String msg) {
		super(msg);
	}

	public IllegalGroupIDException(String msg, Throwable cause) {
		super(msg, cause);
	}

}