package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * Implementation of the {@link EscalationConfiguration}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationConfigurationImpl implements EscalationConfiguration {

	/**
	 * Type of cause.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link ManagedFunctionReference}.
	 */
	private final ManagedFunctionReference taskNodeReference;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause.
	 * @param taskNodeReference
	 *            {@link ManagedFunctionReference}.
	 */
	public EscalationConfigurationImpl(Class<? extends Throwable> typeOfCause,
			ManagedFunctionReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
		this.taskNodeReference = taskNodeReference;
	}

	/*
	 * ================= EscalationConfiguration ========================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public ManagedFunctionReference getManagedFunctionReference() {
		return this.taskNodeReference;
	}

}