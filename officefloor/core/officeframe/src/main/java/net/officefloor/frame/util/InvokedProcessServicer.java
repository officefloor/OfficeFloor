package net.officefloor.frame.util;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Services an invoked {@link ProcessState} from the
 * {@link ManagedObjectSourceStandAlone}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface InvokedProcessServicer {

	/**
	 * Services the invoked {@link ProcessState}.
	 * 
	 * @param processIndex  Index of the invoked {@link ProcessState}. Allows
	 *                      re-using the {@link InvokedProcessServicer} for multiple
	 *                      invocations.
	 * @param parameter     Parameter to the initial {@link ManagedFunction} within
	 *                      the {@link ProcessState}.
	 * @param managedObject {@link ManagedObject} provided for the invoked
	 *                      {@link ProcessState}.
	 * @throws Throwable If failure on servicing.
	 */
	void service(int processIndex, Object parameter, ManagedObject managedObject) throws Throwable;

}