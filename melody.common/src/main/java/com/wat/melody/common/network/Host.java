package com.wat.melody.common.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.wat.melody.common.network.exception.IllegalHostException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Host {

	public static Host parseString(String sHost) throws IllegalHostException {
		return new Host(sHost);
	}

	private InetAddress moInetAddress;

	public Host(String sHost) throws IllegalHostException {
		setValue(sHost);
	}

	@Override
	public String toString() {
		return getValue().getHostAddress();
	}

	public InetAddress getValue() {
		return moInetAddress;
	}

	private InetAddress setValue(String sHost) throws IllegalHostException {
		if (sHost == null) {
			throw new IllegalArgumentException();
		}
		if (sHost.trim().length() == 0) {
			throw new IllegalHostException(Messages.bind(Messages.HostEx_EMPTY,
					sHost));
		}
		InetAddress previous = getValue();
		try {
			moInetAddress = InetAddress.getByName(sHost);
		} catch (UnknownHostException Ex) {
			throw new IllegalHostException(Messages.bind(
					Messages.HostEx_INVALID, sHost), Ex);
		}
		return previous;
	}

}
