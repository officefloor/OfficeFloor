package net.officefloor.compile.supplier;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * <code>Type definition</code> of a Supplier.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierType {

	/**
	 * Obtains the required {@link SupplierThreadLocalType} instances required by
	 * the {@link SupplierSource}.
	 * 
	 * @return Required {@link SupplierThreadLocalType} instances required by the
	 *         {@link SupplierSource}.
	 */
	SupplierThreadLocalType[] getSupplierThreadLocalTypes();

	/**
	 * Obtains the {@link ThreadSynchroniserFactory} instances required by the
	 * {@link SupplierSource}.
	 * 
	 * @return Required {@link ThreadSynchroniserFactory} instances required by the
	 *         {@link SupplierSource}.
	 */
	ThreadSynchroniserFactory[] getThreadSynchronisers();

	/**
	 * Obtains the possible {@link SuppliedManagedObjectSourceType} instances from
	 * the {@link SupplierSource}.
	 * 
	 * @return Possible {@link SuppliedManagedObjectSourceType} instances from the
	 *         {@link SupplierSource}.
	 */
	SuppliedManagedObjectSourceType[] getSuppliedManagedObjectTypes();

}