package com.wat.melody.cloud.network;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDevicesLoader {

	/**
	 * The 'device' XML attribute of a Network Device Node
	 */
	public static final String DEVICE_ATTR = "device";

	private ITaskContext moTC;

	public NetworkDevicesLoader(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moTC = tc;
	}

	protected ITaskContext getTC() {
		return moTC;
	}

	private NetworkDeviceName loadDeviceName(Node n)
			throws ResourcesDescriptorException {
		Node attr = n.getAttributes().getNamedItem(DEVICE_ATTR);
		if (attr == null) {
			throw new ResourcesDescriptorException(n, Messages.bind(
					Messages.NetworkLoadEx_MISSING_ATTR, DEVICE_ATTR));
		}
		String v = attr.getNodeValue();
		try {
			return NetworkDeviceName.parseString(v);
		} catch (IllegalNetworkDeviceException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Converts the given Network Device {@link Node}s into a
	 * {@link NetworkDeviceNameList}.
	 * </p>
	 * 
	 * <p>
	 * A Network Device {@link Node} must have the attributes : <BR/>
	 * <ul>
	 * <li>device : which should contains a LINUX DEVICE NAME (ex : eth0, eth4)
	 * ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param nl
	 *            a list of Network Device {@link Node}s.
	 * 
	 * @return a {@link NetworkDeviceNameList} object, which is a collection of
	 *         {@link NetworkDeviceName} .
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a Network
	 *             Device Node is not valid, multiple device declare with the
	 *             same name).
	 */
	public NetworkDeviceNameList load(NodeList nl)
			throws ResourcesDescriptorException {
		NetworkDeviceNameList dl = new NetworkDeviceNameList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			NetworkDeviceName netDevName = loadDeviceName(n);
			try {
				dl.addNetworkDevice(netDevName);
			} catch (IllegalNetworkDeviceListException Ex) {
				throw new ResourcesDescriptorException(n, "This Network "
						+ "Device Node description is not valid. Read message "
						+ "bellow to get more details about this issue.", Ex);
			}
		}
		return dl;
	}

}