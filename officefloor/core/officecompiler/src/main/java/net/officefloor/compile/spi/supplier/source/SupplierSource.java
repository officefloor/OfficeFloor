package net.officefloor.compile.spi.supplier.source;

import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Supplies {@link ManagedObjectSource} instances.
 * <p>
 * This allows for plugging in object libraries.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSource {

	/**
	 * <p>
	 * Obtains the {@link SupplierSourceSpecification} for this
	 * {@link SupplierSource}.
	 * <p>
	 * This enables the {@link SupplierSourceContext} to be populated with the
	 * necessary details as per this {@link SupplierSourceSpecification} in loading
	 * the {@link SupplierType}.
	 * 
	 * @return {@link SupplierSourceSpecification}.
	 */
	SupplierSourceSpecification getSpecification();

	/**
	 * Supplies the necessary {@link ManagedObjectSource} instances.
	 * 
	 * @param context {@link SupplierSourceContext}.
	 * @throws Exception If fails to provide supply of {@link ManagedObjectSource}
	 *                   instances.
	 */
	void supply(SupplierSourceContext context) throws Exception;

	/**
	 * <p>
	 * Terminates the supply contract.
	 * <p>
	 * This should release all resources required by the supplier.
	 */
	void terminate();

}