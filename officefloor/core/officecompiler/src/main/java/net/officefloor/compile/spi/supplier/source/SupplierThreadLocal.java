package net.officefloor.compile.spi.supplier.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Provides {@link ThreadLocal} access to the {@link ManagedObject} object
 * instances for the {@link SuppliedManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocal<T> {

	/**
	 * <p>
	 * Obtains the object for the respective {@link ManagedObject} this represents.
	 * <p>
	 * This is only to be used within the {@link SuppliedManagedObjectSource}
	 * {@link ManagedObject} implementations. Within this scope, the object will
	 * always be returned. Used outside this scope, the result is unpredictable.
	 * 
	 * @return Object from the {@link ManagedObject}.
	 */
	T get();

}