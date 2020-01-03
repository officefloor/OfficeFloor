package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;

/**
 * Handles {@link CleanupEscalation} instances from other {@link ManagedObject}
 * instances used to service the input.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExternalServiceCleanupEscalationHandler<M extends ManagedObject> {

	/**
	 * Invoked to handle the {@link CleanupEscalation} instances.
	 * 
	 * @param inputManagedObject
	 *            Input {@link ManagedObject}.
	 * @param cleanupEscalations
	 *            {@link CleanupEscalation} instances.
	 * @throws Throwable
	 *             If fails to handle {@link CleanupEscalation} instances.
	 */
	void handleCleanupEscalations(M inputManagedObject, CleanupEscalation[] cleanupEscalations) throws Throwable;

}