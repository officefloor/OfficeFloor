package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.source.ManagedObjectService;

/**
 * Indicates if ready to start {@link ManagedObjectService} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectServiceReady {

	/**
	 * Indicates if ready to start servicing.
	 * 
	 * @return <code>true</code> if ready to start servicing.
	 * @throws Exception If fails to be ready.
	 */
	boolean isServiceReady() throws Exception;

}