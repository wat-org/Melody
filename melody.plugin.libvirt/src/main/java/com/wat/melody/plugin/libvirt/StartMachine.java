package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = StartMachine.START_MACHINE)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class StartMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String START_MACHINE = "start-machine";

	public StartMachine() {
		super();
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstanceController().ensureInstanceIsStarted(
					getInstanceDatas().getStartTimeout().getTimeoutInMillis());
		} catch (OperationException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Messages.StartEx_GENERIC_FAIL, Ex));
		}
	}

}