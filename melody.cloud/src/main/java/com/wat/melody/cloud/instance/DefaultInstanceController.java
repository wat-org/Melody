package com.wat.melody.cloud.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.exception.DiskDeviceException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.instance.exception.OperationTimeoutException;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DefaultInstanceController extends BaseInstanceController {

	private static Logger log = LoggerFactory
			.getLogger(DefaultInstanceController.class);

	private String _instanceId;

	public DefaultInstanceController() {
		super();
	}

	/**
	 * @return the instance id. Can be <tt>null</tt>, when the instance is not
	 *         created.
	 */
	@Override
	public String getInstanceId() {
		return _instanceId;
	}

	/**
	 * @param instanceId
	 *            is the instance id to assign. Can be <tt>null</tt>, when the
	 *            instance is not created.
	 * 
	 * @return the previous value.
	 */
	protected String setInstanceId(String instanceId) {
		// can be null, but cannot be an empty String
		if (instanceId != null && instanceId.trim().length() == 0) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must be a valid String (an Instance Identifier).");
		}
		String previous = getInstanceId();
		_instanceId = instanceId;
		return previous;
	}

	@Override
	public boolean isInstanceDefined() {
		return getInstanceId() != null;
	}

	@Override
	public void ensureInstanceIsCreated(InstanceType type, String site,
			String imageId, KeyPairName keyPairName,
			ProtectedAreaIds protectedAreaIds, long createTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		if (instanceLives()) {
			log.warn(Msg
					.bind(Messages.CreateMsg_LIVES, getInstanceId(), "LIVE"));
		} else {
			try {
				String instanceId = createInstance(type, site, imageId,
						keyPairName, protectedAreaIds, createTimeout);
				setInstanceId(instanceId);
			} catch (OperationTimeoutException Ex) {
				/*
				 * When the create operation times out, the instance should have
				 * been created and its identifier must be stored !
				 */
				if (getInstanceId() != null) {
					fireInstanceCreated(); // will store the instance id
					throw Ex;
				}
			}
		}
		fireInstanceCreated();
		if (instanceRuns()) {
			// will apply network updates performed in listeners
			fireInstanceStarted();
		}
	}

	public abstract String createInstance(InstanceType type, String site,
			String imageId, KeyPairName keyPairName,
			ProtectedAreaIds protectedAreaIds, long createTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException;

	@Override
	public void ensureInstanceIsDestroyed(long destroyTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		if (!isInstanceDefined()) {
			fireInstanceStopped();
			log.warn(Messages.DestroyMsg_NO_INSTANCE);
		} else if (!instanceLives()) {
			/*
			 * When an instance fail to destroy in the given time, its self
			 * protected area is not deleted. When re-running
			 * ensureInstanceIsDestroyed, if the instance is in the dead state,
			 * its self protected area must be deleted.
			 */
			destroyInstance(destroyTimeout);
			fireInstanceStopped();
			fireInstanceDestroyed();
			String sInstanceId = setInstanceId(null);
			log.warn(Msg.bind(Messages.DestroyMsg_TERMINATED, sInstanceId,
					"DEAD"));
		} else {
			destroyInstance(destroyTimeout);
			fireInstanceStopped();
			fireInstanceDestroyed();
			setInstanceId(null);
		}
	}

	/**
	 * <P>
	 * Should destroy the instance. If the instance is destroying or already
	 * destroyed, will NOT raise any error.
	 * 
	 */
	public abstract void destroyInstance(long destroyTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException;

	@Override
	public void ensureInstanceIsStarted(long startTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		InstanceState is = getInstanceState();
		if (!isInstanceDefined()) {
			throw new OperationException(Messages.StartEx_NO_INSTANCE);
		} else if (is == InstanceState.PENDING) {
			log.warn(Msg.bind(Messages.StartMsg_PENDING, getInstanceId(),
					InstanceState.PENDING, InstanceState.RUNNING));
			if (!waitUntilInstanceStatusBecomes(InstanceState.RUNNING,
					startTimeout)) {
				throw new OperationException(Msg.bind(
						Messages.StartEx_WAIT_TO_START_TIMEOUT,
						getInstanceId(), startTimeout));
			}
			fireInstanceStarted();
		} else if (is == InstanceState.RUNNING) {
			fireInstanceStarted();
			log.info(Msg.bind(Messages.StartMsg_RUNNING, getInstanceId(),
					InstanceState.RUNNING));
		} else if (is == InstanceState.STOPPING) {
			log.warn(Msg.bind(Messages.StartMsg_STOPPING, getInstanceId(),
					InstanceState.STOPPING, InstanceState.STOPPED));
			if (!waitUntilInstanceStatusBecomes(InstanceState.STOPPED,
					startTimeout)) {
				throw new OperationException(Msg.bind(
						Messages.StartEx_WAIT_TO_RESTART_TIMEOUT,
						getInstanceId(), startTimeout));
			}
			fireInstanceStopped();
			startInstance(startTimeout);
			fireInstanceStarted();
		} else if (is == InstanceState.SHUTTING_DOWN) {
			fireInstanceStopped();
			throw new OperationException(Msg.bind(
					Messages.StartEx_SHUTTING_DOWN, getInstanceId(),
					InstanceState.SHUTTING_DOWN));
		} else if (is == InstanceState.TERMINATED) {
			fireInstanceStopped();
			fireInstanceDestroyed();
			throw new OperationException(Msg.bind(Messages.StartEx_TERMINATED,
					getInstanceId(), InstanceState.TERMINATED));
		} else if (is == null) { // == !instanceExists()
			throw new OperationException(Msg.bind(
					Messages.StartEx_INVALID_INSTANCE_ID, getInstanceId()));
		} else {
			startInstance(startTimeout);
			fireInstanceStarted();
		}
	}

	public abstract void startInstance(long startTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException;

	@Override
	public void ensureInstanceIsStoped(long stopTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		if (!isInstanceDefined()) {
			fireInstanceStopped();
			log.warn(Messages.StopMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			fireInstanceStopped();
			throw new OperationException(Msg.bind(
					Messages.StopEx_INVALID_INSTANCE_ID, getInstanceId()));
		} else if (!instanceRuns()) {
			fireInstanceStopped();
			log.warn(Msg.bind(Messages.StopMsg_ALREADY_STOPPED,
					getInstanceId(), InstanceState.STOPPED));
		} else {
			stopInstance(stopTimeout);
			fireInstanceStopped();
		}
	}

	public abstract void stopInstance(long stopTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException;

	@Override
	public void ensureInstanceSizing(InstanceType targetType)
			throws OperationException, InterruptedException {
		InstanceState is = getInstanceState();
		if (!isInstanceDefined()) {
			log.warn(Messages.ResizeMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			throw new OperationException(Msg.bind(
					Messages.ResizeEx_INVALID_INSTANCE_ID, getInstanceId()));
		} else if (getInstanceType() == targetType) {
			log.info(Msg.bind(Messages.ResizeMsg_NO_NEED, getInstanceId(),
					targetType));
		} else if (is != InstanceState.STOPPED) {
			throw new OperationException(Msg.bind(
					Messages.ResizeEx_NOT_STOPPED, getInstanceId(),
					InstanceState.STOPPED, is));
		} else {
			resizeInstance(targetType);
		}
	}

	public abstract void resizeInstance(InstanceType targetType)
			throws OperationException, InterruptedException;

	public boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout) throws InterruptedException {
		return waitUntilInstanceStatusBecomes(state, timeout, 0);
	}

	public abstract boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException;

	@Override
	public void ensureInstanceDiskDevicesAreUpToDate(DiskDeviceList list)
			throws OperationException, InterruptedException {
		if (!isInstanceDefined()) {
			log.warn(Messages.UpdateDiskDevMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			throw new OperationException(Msg.bind(
					Messages.UpdateDiskDevEx_INVALID_INSTANCE_ID,
					getInstanceId()));
		} else {
			updateInstanceDiskDevices(list);
		}
	}

	public void updateInstanceDiskDevices(DiskDeviceList target)
			throws OperationException, InterruptedException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (target.size() == 0) {
			throw new RuntimeException("Cannot update Virtual Machine '"
					+ getInstanceId() + "' Disk Device List. "
					+ "The expected Disk Device List is empty! "
					+ "There should be at least one disk device in it.");
		}
		DiskDeviceList current = getInstanceDiskDevices();
		try {
			current.isCompatible(target);
		} catch (DiskDeviceException Ex) {
			throw new OperationException(Ex);
		}

		DiskDeviceList disksToAdd = null;
		DiskDeviceList disksToRemove = null;
		disksToAdd = current.delta(target);
		disksToRemove = target.delta(current);

		log.info(Msg.bind(Messages.UpdateDiskDevMsg_DISK_DEVICES_RESUME,
				getInstanceId(), current, target, disksToAdd, disksToRemove));

		detachAndDeleteInstanceDiskDevices(disksToRemove);
		createAndAttachDiskInstanceDevices(disksToAdd);

		updateInstanceDiskDevicesDeleteOnTerminationFlag(target);
	}

	public abstract void detachAndDeleteInstanceDiskDevices(
			DiskDeviceList disksToRemove) throws OperationException,
			InterruptedException;

	public abstract void createAndAttachDiskInstanceDevices(
			DiskDeviceList disksToAdd) throws OperationException,
			InterruptedException;

	public abstract void updateInstanceDiskDevicesDeleteOnTerminationFlag(
			DiskDeviceList diskList);

	@Override
	public void ensureInstanceNetworkDevicesAreUpToDate(NetworkDeviceList list)
			throws OperationException, InterruptedException {
		if (!isInstanceDefined()) {
			log.warn(Messages.UpdateNetDevMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			throw new OperationException(Msg.bind(
					Messages.UpdateNetDevEx_INVALID_INSTANCE_ID,
					getInstanceId()));
		} else {
			updateInstanceNetworkDevices(list);
			if (instanceRuns()) {
				fireInstanceStarted();
			}
		}
	}

	public void updateInstanceNetworkDevices(NetworkDeviceList target)
			throws OperationException, InterruptedException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceList.class.getCanonicalName() + ".");
		}
		if (target.size() == 0) {
			throw new RuntimeException("Cannot update Virtual Machine '"
					+ getInstanceId() + "' Network Device List. "
					+ "The expected Network Device List is empty! "
					+ "There should be at least one network device called "
					+ "'eth0' in it.");
		}
		NetworkDeviceList current = getInstanceNetworkDevices();
		NetworkDeviceList toAdd = null;
		NetworkDeviceList toRemove = null;
		toAdd = current.delta(target);
		toRemove = target.delta(current);

		log.info(Msg.bind(Messages.UpdateNetDevMsg_NETWORK_DEVICES_RESUME,
				getInstanceId(), current, target, toAdd, toRemove));

		detachInstanceNetworkDevices(toRemove);
		attachInstanceNetworkDevices(toAdd);
	}

	public abstract void detachInstanceNetworkDevices(
			NetworkDeviceList netDevicesToRemove) throws OperationException,
			InterruptedException;

	public abstract void attachInstanceNetworkDevices(
			NetworkDeviceList netDevicesToAdd) throws OperationException,
			InterruptedException;

	@Override
	public void ensureInstanceFireWallRulesAreUpToDate(
			FireWallRulesPerDevice list) throws OperationException,
			InterruptedException {
		if (!isInstanceDefined()) {
			log.warn(Messages.UpdateFireWallMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			throw new OperationException(Msg.bind(
					Messages.UpdateFireWallEx_INVALID_INSTANCE_ID,
					getInstanceId()));
		} else {
			updateInstanceFireWallRules(list);
		}
	}

	public void updateInstanceFireWallRules(FireWallRulesPerDevice target)
			throws OperationException, InterruptedException {
		NetworkDeviceList netdevs = getInstanceNetworkDevices();
		for (NetworkDevice netdev : netdevs) {
			NetworkDeviceName devname = netdev.getNetworkDeviceName();
			FireWallRules current = getInstanceFireWallRules(devname);
			FireWallRules expected = target.getFireWallRules(devname);
			FireWallRules toAdd = current.delta(expected);
			FireWallRules toRemove = expected.delta(current);

			log.info(Msg.bind(Messages.UpdateFireWallMsg_FWRULES_RESUME,
					getInstanceId(), devname, current, expected, toAdd,
					toRemove));

			revokeInstanceFireWallRules(devname, toRemove);
			authorizeInstanceFireWallRules(devname, toAdd);
		}
	}

}