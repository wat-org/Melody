package com.wat.melody.plugin.libvirt.protectedarea;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.cloud.firewall.xml.FireWallRulesLoader;
import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateProtectedArea.UPDATE_PROTECTED_AREA)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class UpdateProtectedArea extends AbstractProtectedAreaOperation {

	/**
	 * Task's name
	 */
	public static final String UPDATE_PROTECTED_AREA = "update-protected-area";

	private FireWallRulesPerDevice _rules = null;

	public UpdateProtectedArea() {
		super();
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found.
		// The 'Per Device' aspect of this FwRules is not used.
		try {
			setFwRules(new FireWallRulesLoader().load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getProtectedAreaController().ensureProtectedAreaContentIsUpToDate(
					getFwRules());
		} catch (ProtectedAreaException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Messages.PAContentEx_GENERIC_FAIL, Ex));

		}
	}

	protected FireWallRulesPerDevice getFwRules() {
		return _rules;
	}

	protected FireWallRulesPerDevice setFwRules(FireWallRulesPerDevice fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ FireWallRulesPerDevice.class.getCanonicalName() + ".");
		}
		FireWallRulesPerDevice previous = getFwRules();
		_rules = fwrs;
		return previous;
	}

}