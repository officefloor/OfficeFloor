package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;

/**
 * {@link BoundInputManagedObjectConfiguration} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class BoundInputManagedObjectConfigurationImpl implements
		BoundInputManagedObjectConfiguration {

	/**
	 * Input {@link ManagedObject} name.
	 */
	private final String inputManagedObjectName;

	/**
	 * Name of the {@link ManagedObjectSource} to bind to the input
	 * {@link ManagedObject}.
	 */
	private final String boundManagedObjectSourceName;

	/**
	 * Initiate.
	 *
	 * @param inputManagedObjectName
	 *            Input {@link ManagedObject} name.
	 * @param boundManagedObjectSourceName
	 *            Name of the {@link ManagedObjectSource} to bind to the input
	 *            {@link ManagedObject}.
	 */
	public BoundInputManagedObjectConfigurationImpl(
			String inputManagedObjectName, String boundManagedObjectSourceName) {
		this.inputManagedObjectName = inputManagedObjectName;
		this.boundManagedObjectSourceName = boundManagedObjectSourceName;
	}

	/*
	 * =============== BoundInputManagedObjectConfiguration ====================
	 */

	@Override
	public String getInputManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public String getBoundManagedObjectSourceName() {
		return this.boundManagedObjectSourceName;
	}

}