package com.wat.melody.cloud.firewall.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.ComplexFireWallRule;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractTcpUdpFireWallRulesLoader extends
		AbstractFireWallRulesLoader {

	/**
	 * XML attribute of a FireWall Rule Element, which define its source ports.
	 */
	public static final String FROM_PORTS_ATTR = "from-ports";

	/**
	 * XML attribute of a FireWall Rule Element, which define its destination
	 * ports.
	 */
	public static final String TO_PORTS_ATTR = "to-ports";

	protected PortRanges loadFromPorts(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + FROM_PORTS_ATTR,
					null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return PortRanges.parseString(v);
			} catch (IllegalPortRangesException Ex) {
				Attr attr = DocHelper.getAttribute(e, "./@" + FROM_PORTS_ATTR,
						null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	protected PortRanges loadToPorts(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + TO_PORTS_ATTR,
					null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return PortRanges.parseString(v);
			} catch (IllegalPortRangesException Ex) {
				Attr attr = DocHelper.getAttribute(e, "./@" + TO_PORTS_ATTR,
						null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * <p>
	 * Find the TCP/UDP FireWall Rule {@link Element}s of the given given
	 * Instance {@link Element} or a Protected Area {@link Element} and convert
	 * it into a {@link FireWallRulesPerDevice}.
	 * </p>
	 * 
	 * <p>
	 * A TCP/UDP FireWall Rule {@link Element} may have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRefs} ;</li>
	 * <li>from-ips : which should contains {@link Addresses} ;</li>
	 * <li>from-ports : which should contains {@link PortRanges} ;</li>
	 * <li>to-ips : which should contains {@link Addresses} :</li>
	 * <li>to-ports : which should contains {@link PortRanges} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>access : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return a {@link FireWallRulesPerDevice} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a FireWall Rule
	 *             {@link Element}'s attribute is not valid).
	 */
	public FireWallRulesPerDevice load(Element e) throws NodeRelatedException {
		List<Element> fireWallRuleElmts = findFwRuleNodes(e);

		FireWallRulesPerDevice fwrs = new FireWallRulesPerDevice();
		for (Element fireWallRuleElmt : fireWallRuleElmts) {
			Directions dirs = loadDirection(fireWallRuleElmt);
			Addresses fromIps = loadFromIps(fireWallRuleElmt, e);
			Addresses toIps = loadToIps(fireWallRuleElmt, e);
			if (fromIps == null && dirs.contains(Direction.IN)) {
				if (dirs.contains(Direction.OUT)) {
					dirs.remove(Direction.IN);
				} else {
					continue;
				}
			}
			if (toIps == null && dirs.contains(Direction.OUT)) {
				if (dirs.contains(Direction.IN)) {
					dirs.remove(Direction.OUT);
				} else {
					continue;
				}
			}
			PortRanges fromPorts = loadFromPorts(fireWallRuleElmt);
			PortRanges toPorts = loadToPorts(fireWallRuleElmt);
			NetworkDeviceNameRefs refs = loadNetworkDeviceNameRefs(fireWallRuleElmt);
			Access access = loadAccess(fireWallRuleElmt);
			fwrs.merge(refs,
					newFwRule(fromIps, fromPorts, toIps, toPorts, dirs, access));
		}
		return fwrs;
	}

	public abstract List<Element> findFwRuleNodes(Element instanceElmt)
			throws NodeRelatedException;

	public abstract ComplexFireWallRule newFwRule(Addresses fromAddresses,
			PortRanges fromPortRanges, Addresses toAddresses,
			PortRanges toPortRanges, Directions directions, Access access);

}