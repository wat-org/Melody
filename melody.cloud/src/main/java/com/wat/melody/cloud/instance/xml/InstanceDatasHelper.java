package com.wat.melody.cloud.instance.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.protectedarea.ProtectedAreaNames;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNamesException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.keypair.exception.IllegalKeyPairSizeException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryPathException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class InstanceDatasHelper {

	/*
	 * TODO : create method melody:instanceIdExists(//instancenodepath)
	 */

	public static List<String> findInstanceId(List<Element> instanceElmts)
			throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			String res = findInstanceId(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static String findInstanceId(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		// instance-id cannot be herited and cannot contains Melody XPath Expr
		String v = instanceElmt
				.getAttribute(InstanceDatasLoader.INSTANCE_ID_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	public static List<String> findInstanceRegion(List<Element> instanceElmts)
			throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			String res = findInstanceRegion(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static String findInstanceRegion(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.REGION_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<String> findInstanceSite(List<Element> instanceElmts)
			throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			String res = findInstanceSite(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static String findInstanceSite(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.SITE_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<String> findInstanceImageId(List<Element> instanceElmts)
			throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			String res = findInstanceImageId(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static String findInstanceImageId(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.IMAGEID_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<InstanceType> findInstanceType(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<InstanceType> list = new ArrayList<InstanceType>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			InstanceType res = findInstanceType(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static InstanceType findInstanceType(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.INSTANCETYPE_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return InstanceType.parseString(v);
			} catch (IllegalInstanceTypeException Ex) {
				Attr attr = DocHelper.getAttribute(instanceElmt, "./@"
						+ InstanceDatasLoader.INSTANCETYPE_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<KeyPairRepositoryPath> findInstanceKeyPairRepositoryPath(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<KeyPairRepositoryPath> list = new ArrayList<KeyPairRepositoryPath>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			KeyPairRepositoryPath res = findInstanceKeyPairRepositoryPath(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static KeyPairRepositoryPath findInstanceKeyPairRepositoryPath(
			Element instanceElmt) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.KEYPAIR_REPO_ATTR, null);
			if (instanceElmt == null) {
				throw new IllegalArgumentException("null: Not accepted. "
						+ "Must be valid an "
						+ Element.class.getCanonicalName() + ".");
			}
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return new KeyPairRepositoryPath(v);
			} catch (KeyPairRepositoryPathException Ex) {
				Attr attr = DocHelper.getAttribute(instanceElmt, "./@"
						+ InstanceDatasLoader.KEYPAIR_REPO_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<KeyPairName> findInstanceKeyPairName(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<KeyPairName> list = new ArrayList<KeyPairName>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			KeyPairName res = findInstanceKeyPairName(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static KeyPairName findInstanceKeyPairName(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.KEYPAIR_NAME_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return KeyPairName.parseString(v);
			} catch (IllegalKeyPairNameException Ex) {
				Attr attr = DocHelper.getAttribute(instanceElmt, "./@"
						+ InstanceDatasLoader.KEYPAIR_NAME_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<String> findInstanceKeyPairPassphrase(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			String res = findInstanceKeyPairPassphrase(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static String findInstanceKeyPairPassphrase(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.PASSPHRASE_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<KeyPairSize> findInstanceKeyPairSize(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<KeyPairSize> list = new ArrayList<KeyPairSize>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			KeyPairSize res = findInstanceKeyPairSize(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static KeyPairSize findInstanceKeyPairSize(Element instanceElmt)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.KEYPAIR_SIZE_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return KeyPairSize.parseString(v);
			} catch (IllegalKeyPairSizeException Ex) {
				Attr attr = DocHelper.getAttribute(instanceElmt, "./@"
						+ InstanceDatasLoader.KEYPAIR_SIZE_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public static List<ProtectedAreaNames> findInstanceProtectedAreaNames(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<ProtectedAreaNames> list = new ArrayList<ProtectedAreaNames>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			ProtectedAreaNames res = findInstanceProtectedAreaNames(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static ProtectedAreaNames findInstanceProtectedAreaNames(
			Element instanceElmt) throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			String v = DocHelper.getAttributeValue(instanceElmt, "./@"
					+ InstanceDatasLoader.PROTECTED_AREAS_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return ProtectedAreaNames.parseString(v);
			} catch (IllegalProtectedAreaNamesException Ex) {
				Attr attr = DocHelper.getAttribute(instanceElmt, "./@"
						+ InstanceDatasLoader.PROTECTED_AREAS_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

}