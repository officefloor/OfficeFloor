package net.officefloor.compile.spi.supplier.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Completion context for the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierCompletionContext extends SupplierCompileContext {

	/**
	 * <p>
	 * Obtains the {@link AvailableType} instances.
	 * <p>
	 * Note that {@link ManagedObject} instances provided by {@link SupplierSource}
	 * instances are not included. This is because {@link SupplierSource} instances
	 * are not completed at this time to list their available {@link ManagedObject}
	 * instances.
	 * 
	 * @return {@link AvailableType} instances.
	 */
	AvailableType[] getAvailableTypes();
}