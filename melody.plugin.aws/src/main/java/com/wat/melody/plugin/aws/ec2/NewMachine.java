package com.wat.melody.plugin.aws.ec2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.cloud.InstanceType;
import com.wat.melody.cloud.exception.IllegalInstanceTypeException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.plugin.aws.ec2.common.AbstractMachineOperation;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpathextensions.GetHeritedAttribute;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NewMachine extends AbstractMachineOperation {

	private static Log log = LogFactory.getLog(NewMachine.class);

	/**
	 * The 'NewMachine' XML element
	 */
	public static final String NEW_MACHINE = "NewMachine";

	/**
	 * The 'instanceType' XML attribute
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	/**
	 * The 'imageId' XML attribute
	 */
	public static final String IMAGEID_ATTR = "imageId";

	/**
	 * The 'availabilityZone' XML attribute
	 */
	public static final String AVAILABILITYZONE_ATTR = "availabilityZone";

	/**
	 * The 'keyPairName' XML attribute
	 */
	public static final String KEYPAIR_NAME_ATTR = "keyPairName";

	/**
	 * The 'passphrase' XML attribute
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * The 'keyRepository' XML attribute
	 */
	public static final String KEYPAIR_REPO_ATTR = "keyPairRepository";

	private InstanceType msInstanceType;
	private String msImageId;
	private String msKeyPairName;
	private String msPassphrase;
	private File moKeyPairRepository;
	private String msSecurityGroupName;
	private String msSecurityGroupDescription;
	private String msAvailabilityZone;

	public NewMachine() {
		super();
		initAvailabilityZone();
		initSecurityGroupDescription();
		initSecurityGroupName();
		initKeyPairRepository();
		initPassphrase();
		initKeyPairName();
		initImageId();
		initInstanceType();
	}

	private void initAvailabilityZone() {
		msAvailabilityZone = null;
	}

	private void initSecurityGroupDescription() {
		msSecurityGroupDescription = "Melody security group";
	}

	private void initSecurityGroupName() {
		msSecurityGroupName = "MelodySg" + "_" + System.currentTimeMillis();
	}

	private void initKeyPairRepository() {
		moKeyPairRepository = null;
	}

	private void initPassphrase() {
		msPassphrase = null;
	}

	private void initKeyPairName() {
		msKeyPairName = null;
	}

	private void initImageId() {
		msImageId = null;
	}

	private void initInstanceType() {
		msInstanceType = null;
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		try {
			Node n = getTargetNode();
			String v = null;

			v = GetHeritedAttribute.getHeritedAttributeValue(n,
					Common.INSTANCETYPE_ATTR);
			try {
				try {
					if (v != null) {
						setInstanceType(InstanceType.parseString(v));
					}
				} catch (IllegalInstanceTypeException Ex) {
					throw new AwsException(Messages.bind(
							Messages.NewEx_INVALID_INSTANCETYPE_ATTR, v));
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_INSTANCETYPE_ERROR,
						Common.INSTANCETYPE_ATTR, getTargetNodeLocation()), Ex);
			}

			v = GetHeritedAttribute.getHeritedAttributeValue(n,
					Common.IMAGEID_ATTR);
			try {
				if (v != null) {
					setImageId(v);
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_IMAGEID_ERROR, Common.IMAGEID_ATTR,
						getTargetNodeLocation()), Ex);
			}

			v = GetHeritedAttribute.getHeritedAttributeValue(n,
					Common.AVAILABILITYZONE_ATTR);
			try {
				if (v != null) {
					setAvailabilityZone(v);
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_AVAILABILITYZONE_ERROR,
						Common.AVAILABILITYZONE_ATTR, getTargetNodeLocation()),
						Ex);
			}

			v = GetHeritedAttribute.getHeritedAttributeValue(n,
					Common.KEYPAIR_NAME_ATTR);
			try {
				if (v != null) {
					setKeyPairName(v);
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_KEYPAIR_NAME_ERROR,
						Common.KEYPAIR_NAME_ATTR, getTargetNodeLocation()), Ex);
			}

			v = GetHeritedAttribute.getHeritedAttributeValue(n,
					Common.PASSPHRASE_ATTR);
			if (v != null) {
				setPassphrase(v);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}

		// Get the default KeyPair Repository, if not provided.
		if (getKeyPairRepository() == null) {
			setKeyPairRepository(getSshPluginConf().getKeyPairRepo());
		}
		// Validate everything is provided.
		if (getInstanceType() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_INSTANCETYPE_ATTR, new Object[] {
							NewMachine.INSTANCETYPE_ATTR,
							NewMachine.NEW_MACHINE, Common.INSTANCETYPE_ATTR,
							getTargetNodeLocation() }));
		}

		if (getImageId() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_IMAGEID_ATTR, new Object[] {
							NewMachine.IMAGEID_ATTR, NewMachine.NEW_MACHINE,
							Common.IMAGEID_ATTR, getTargetNodeLocation() }));
		}

		if (getKeyPairName() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_KEYPAIR_NAME_ATTR, new Object[] {
							NewMachine.KEYPAIR_NAME_ATTR,
							NewMachine.NEW_MACHINE, Common.KEYPAIR_NAME_ATTR,
							getTargetNodeLocation() }));
		}

		// Validate task's attributes
		// AZ must be validated AFTER the Aws Region is known
		// AZ can be null
		if (getAvailabilityZoneFullName() != null
				&& !Common.availabilityZoneExists(getEc2(),
						getAvailabilityZoneFullName())) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_INVALID_AVAILABILITYZONE_ATTR,
					getAvailabilityZone(), getRegion()));
		}
		// imageId must be validated AFTER the Aws Region is known
		if (!Common.imageIdExists(getEc2(), getImageId())) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_INVALID_IMAGEID_ATTR, getImageId(),
					getRegion()));
		}
		// Enable the given KeyPair.
		try {
			enableKeyPair(getKeyPairRepository(), getKeyPairName(),
					getSshPluginConf().getKeyPairSize(), getPassphrase());
		} catch (IOException Ex) {
			throw new AwsException(Ex);
		}

	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		if (instanceLives()) {
			log.warn(Messages.bind(Messages.NewMsg_LIVES, new Object[] {
					getAwsInstanceID(), "LIVE", getTargetNodeLocation() }));
			setInstanceRelatedInfosToED(getInstance());
			if (instanceRuns()) {
				enableManagement();
			}
		} else {
			newInstance(getInstanceType(), getImageId(),
					getSecurityGroupName(), getSecurityGroupDescription(),
					getAvailabilityZoneFullName(), getKeyPairName());
			setInstanceRelatedInfosToED(getInstance());
			enableManagement();
		}
	}

	public InstanceType getInstanceType() {
		return msInstanceType;
	}

	@Attribute(name = INSTANCETYPE_ATTR)
	public InstanceType setInstanceType(InstanceType instanceType) {
		if (instanceType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid InstanceType (Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		InstanceType previous = getInstanceType();
		msInstanceType = instanceType;
		return previous;
	}

	public String getImageId() {
		return msImageId;
	}

	@Attribute(name = IMAGEID_ATTR)
	public String setImageId(String imageId) throws AwsException {
		if (imageId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS AMI Id).");
		}
		String previous = getImageId();
		msImageId = imageId;
		return previous;
	}

	public String getAvailabilityZone() {
		return msAvailabilityZone;
	}

	@Attribute(name = AVAILABILITYZONE_ATTR)
	public String setAvailabilityZone(String sAZ) throws AwsException {
		if (sAZ == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS Availability Zone).");
		}
		String previous = getAvailabilityZone();
		msAvailabilityZone = sAZ;
		return previous;
	}

	public String getAvailabilityZoneFullName() {
		if (getAvailabilityZone() == null) {
			return null;
		}
		return getRegion() + getAvailabilityZone();
	}

	public String getKeyPairName() {
		return msKeyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public String setKeyPairName(String keyPairName) throws AwsException {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS KeyPair Name).");
		}
		if (keyPairName.trim().length() == 0) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_EMPTY_KEYPAIR_NAME_ATTR, keyPairName));
		} else if (!keyPairName
				.matches("^" + Common.KEYPAIR_NAME_PATTERN + "$")) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_INVALID_KEYPAIR_NAME_ATTR, keyPairName,
					Common.KEYPAIR_NAME_PATTERN));
		}
		String previous = getKeyPairName();
		msKeyPairName = keyPairName;
		return previous;
	}

	public String getPassphrase() {
		return msPassphrase;
	}

	@Attribute(name = PASSPHRASE_ATTR)
	public String setPassphrase(String sPassphrase) {
		if (sPassphrase == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassphrase();
		msPassphrase = sPassphrase;
		return previous;
	}

	public File getKeyPairRepository() {
		return moKeyPairRepository;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public File setKeyPairRepository(File keyPairRepository)
			throws AwsException {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		try {
			Tools.validateDirExists(keyPairRepository.getAbsolutePath());
		} catch (IllegalDirectoryException Ex) {
			throw new AwsException(
					Messages.bind(Messages.NewEx_INVALID_KEYPAIR_REPO_ATTR,
							keyPairRepository), Ex);
		}
		File previous = getKeyPairRepository();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

	public String getSecurityGroupName() {
		return msSecurityGroupName;
	}

	public String getSecurityGroupDescription() {
		return msSecurityGroupDescription;
	}

}
