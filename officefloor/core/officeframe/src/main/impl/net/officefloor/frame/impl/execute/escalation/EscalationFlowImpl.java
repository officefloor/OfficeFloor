package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

/**
 * Implementation of {@link EscalationFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationFlowImpl implements EscalationFlow {

	/**
	 * Type of cause handled by this {@link EscalationFlow}.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link ManagedFunctionMetaData} determine the actions for this
	 * {@link EscalationFlow}.
	 */
	private final ManagedFunctionMetaData<?, ?> taskMetaData;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData} determine the actions for this
	 *            {@link EscalationFlow}.
	 */
	public EscalationFlowImpl(Class<? extends Throwable> typeOfCause, ManagedFunctionMetaData<?, ?> taskMetaData) {
		this.typeOfCause = typeOfCause;
		this.taskMetaData = taskMetaData;
	}

	/*
	 * ======================== Escalation ====================================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData() {
		return this.taskMetaData;
	}

}