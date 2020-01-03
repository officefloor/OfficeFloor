package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * External service input to {@link OfficeFloor}.
 * 
 * @param <O> Type of object returned from the {@link ManagedObject}.
 * @param <M> Type of {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExternalServiceInput<O, M extends ManagedObject> {

	/**
	 * Invoked by an external service to use {@link OfficeFloor}.
	 * 
	 * @param managedObject {@link ManagedObject} for dependency injection into
	 *                      {@link ManagedFunction} instances.
	 * @param callback      {@link FlowCallback} to indicate servicing complete.
	 * @return {@link ProcessManager} for the invoked {@link ProcessState}.
	 */
	ProcessManager service(M managedObject, FlowCallback callback);

}