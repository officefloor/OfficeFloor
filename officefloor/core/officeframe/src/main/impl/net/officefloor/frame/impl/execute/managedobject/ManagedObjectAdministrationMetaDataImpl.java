package net.officefloor.frame.impl.execute.managedobject;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * {@link ManagedObjectAdministrationMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectAdministrationMetaDataImpl<E, F extends Enum<F>, G extends Enum<G>>
		implements ManagedObjectAdministrationMetaData<E, F, G> {

	/**
	 * {@link ManagedObjectIndex} instances for the required {@link ManagedObject}.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<E, F, G> administrationMetaData;

	/**
	 * {@link Logger} for the {@link AdministrationContext}.
	 */
	private final Logger logger;

	/**
	 * Instantiate.
	 * 
	 * @param requiredManagedObjects {@link ManagedObjectIndex} instances for the
	 *                               required {@link ManagedObject}.
	 * @param administrationMetaData {@link AdministrationMetaData}.
	 * @param logger                 {@link Logger} for the
	 *                               {@link AdministrationContext}.
	 */
	public ManagedObjectAdministrationMetaDataImpl(ManagedObjectIndex[] requiredManagedObjects,
			AdministrationMetaData<E, F, G> administrationMetaData, Logger logger) {
		this.requiredManagedObjects = requiredManagedObjects;
		this.administrationMetaData = administrationMetaData;
		this.logger = logger;
	}

	/*
	 * ================= ManagedObjectAdministrationMetaData =================
	 */

	@Override
	public ManagedObjectIndex[] getRequiredManagedObjects() {
		return this.requiredManagedObjects;
	}

	@Override
	public AdministrationMetaData<E, F, G> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

}