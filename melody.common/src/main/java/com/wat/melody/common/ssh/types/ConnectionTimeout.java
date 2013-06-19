package com.wat.melody.common.ssh.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConnectionTimeout implements Timeout {

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link ConnectionTimeout}, which is equal to the given
	 *         <tt>long</tt>.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>long</tt> is < 0.
	 */
	public static ConnectionTimeout parseLong(long timeout)
			throws IllegalTimeoutException {
		return new ConnectionTimeout(timeout);
	}

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link ConnectionTimeout}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTimeoutException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>Long</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public static ConnectionTimeout parseString(String timeout)
			throws IllegalTimeoutException {
		return new ConnectionTimeout(timeout);
	}

	private GenericTimeout _timeout;

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>long</tt> is < 0.
	 */
	public ConnectionTimeout(long timeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.CnxTimeoutEx_INVALID, timeout), Ex);
		}
	}

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTimeoutException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>Long</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public ConnectionTimeout(String timeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.CnxTimeoutEx_INVALID, timeout), Ex);
		}
	}

	@Override
	public int hashCode() {
		return _timeout.hashCode();
	}

	@Override
	public String toString() {
		return _timeout.toString();
	}

	@Override
	public boolean equals(Object anObject) {
		return _timeout.equals(anObject);
	}

	/**
	 * @return the timeout, in seconds.
	 */
	public long getTimeout() {
		return _timeout.getTimeout();
	}

	/**
	 * @return the timeout, in milliseconds.
	 */
	public long getTimeoutInMillis() {
		return _timeout.getTimeoutInMillis();
	}

}