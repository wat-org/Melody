package com.wat.melody.plugin.libvirt.protectedarea;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = CreateProtectedArea.NEW_PROTECTED_AREA)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class CreateProtectedArea extends AbstractProtectedAreaOperation {

	/**
	 * Task's name
	 */
	public static final String NEW_PROTECTED_AREA = "new-protected-area";

	public CreateProtectedArea() {
		super();
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getProtectedAreaController().ensureProtectedAreaIsCreated(
					getProtectedAreaDatas().getName(),
					getProtectedAreaDatas().getDescription());
		} catch (ProtectedAreaException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Msg.bind(
							Messages.PACreateEx_GENERIC_FAIL,
							getProtectedAreaDatas()), Ex));
		}
	}

}