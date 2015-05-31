package com.wat.melody.plugin.libvirt;

import java.io.IOException;

import com.wat.cloud.libvirt.LibVirtInstanceController;
import com.wat.cloud.libvirt.LibVirtKeyPairRepository;
import com.wat.cloud.libvirt.exception.LibVirtKeyPairRepositoryException;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.ex.HiddenException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = NewMachine.NEW_MACHINE)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class NewMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String NEW_MACHINE = "new-machine";

	public NewMachine() {
		super();
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstanceController().ensureInstanceIsCreated(
					getInstanceDatas().getInstanceType(),
					getInstanceDatas().getSite(),
					getInstanceDatas().getImageId(),
					getInstanceDatas().getKeyPairName(),
					getInstanceDatas().getProtectedAreaIds(),
					getInstanceDatas().getCreateTimeout().getTimeoutInMillis());
		} catch (OperationException Ex) {
			throw new LibVirtException(
					new NodeRelatedException(getTargetElement(),
							Msg.bind(Messages.CreateEx_GENERIC_FAIL,
									getInstanceDatas()), Ex));
		}
	}

	/**
	 * @return an {@link LibVirtInstanceController} which provides additional
	 *         KeyPair Management features.
	 */
	@Override
	protected InstanceController newLibVirtInstanceController() {
		// create LibVirtInstanceControllerWithKeyPairManagement class ?
		return new LibVirtInstanceController(getCloudConnection(),
				getInstanceId()) {

			@Override
			public String createInstance(InstanceType type, String site,
					String imageId, KeyPairName keyPairName,
					ProtectedAreaIds protectedAreaIds, long createTimeout)
					throws OperationException, InterruptedException {
				try {
					LibVirtKeyPairRepository kpr = LibVirtKeyPairRepository
							.getLibVirtKeyPairRepository(getConnection(),
									getInstanceDatas()
											.getKeyPairRepositoryPath());
					kpr.createKeyPair(keyPairName, getInstanceDatas()
							.getKeyPairSize(), getInstanceDatas()
							.getPassphrase());
				} catch (IllegalPassphraseException Ex) {
					if (getInstanceDatas().getPassphrase() == null) {
						throw new OperationException(Msg.bind(
								Messages.CreateEx_MISSING_PASSPHRASE_ATTR,
								keyPairName,
								InstanceDatasLoader.PASSPHRASE_ATTR),
								new HiddenException(Ex));
					} else {
						throw new OperationException(Msg.bind(
								Messages.CreateEx_INVALID_PASSPHRASE_ATTR,
								keyPairName,
								InstanceDatasLoader.PASSPHRASE_ATTR),
								new HiddenException(Ex));
					}
				} catch (IOException | LibVirtKeyPairRepositoryException Ex) {
					throw new OperationException(Ex);
				}

				return super.createInstance(type, site, imageId, keyPairName,
						protectedAreaIds, createTimeout);
			}

		};
	}

}