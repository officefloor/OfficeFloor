package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;

/**
 * Start from the {@link ManagedObjectExecuteManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteStart<F extends Enum<F>> {

	/**
	 * Obtains the {@link ManagedObjectStartupRunnable} instances to execute once
	 * ready to start processing.
	 * 
	 * @return {@link ManagedObjectStartupRunnable} instances to execute once ready
	 *         to start processing.
	 */
	ManagedObjectStartupRunnable[] getStartups();

	/**
	 * Obtains the {@link ManagedObjectServiceReady} instances.
	 * 
	 * @return {@link ManagedObjectServiceReady} instances.
	 */
	ManagedObjectServiceReady[] getServiceReadiness();

	/**
	 * Obtains the {@link ManagedObjectService} instances.
	 * 
	 * @return {@link ManagedObjectService} instances.
	 */
	ManagedObjectService<F>[] getServices();

	/**
	 * Obtains the {@link ManagedObjectServiceContext}.
	 * 
	 * @return {@link ManagedObjectServiceContext}.
	 */
	ManagedObjectServiceContext<F> getManagedObjectServiceContext();

}