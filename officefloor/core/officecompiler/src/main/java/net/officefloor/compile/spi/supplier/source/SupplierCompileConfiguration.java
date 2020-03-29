package net.officefloor.compile.spi.supplier.source;

import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * Configuration from the {@link SupplierCompileContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierCompileConfiguration extends SupplierCompileContext {

	/**
	 * Obtains the {@link SupplierThreadLocalType} instances.
	 * 
	 * @return {@link SupplierThreadLocalType} instances.
	 */
	SupplierThreadLocalType[] getSupplierThreadLocalTypes();

	/**
	 * Obtains the {@link ThreadSynchroniserFactory} instances.
	 * 
	 * @return {@link ThreadSynchroniserFactory} instances.
	 */
	ThreadSynchroniserFactory[] getThreadSynchronisers();

	/**
	 * Obtains the {@link SupplierCompileCompletion} instances.
	 * 
	 * @return {@link SupplierCompileCompletion} instances.
	 */
	SupplierCompileCompletion[] getCompileCompletions();

	/**
	 * Obtains the {@link SuppliedManagedObjectSourceType} instances.
	 * 
	 * @return {@link SuppliedManagedObjectSourceType} instances.
	 */
	SuppliedManagedObjectSourceType[] getSuppliedManagedObjectSourceTypes();

}