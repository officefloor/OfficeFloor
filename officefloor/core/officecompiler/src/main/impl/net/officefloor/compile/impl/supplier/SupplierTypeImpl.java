package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * {@link SupplierType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierTypeImpl implements SupplierType {

	/**
	 * {@link SupplierThreadLocalType} instances.
	 */
	private final SupplierThreadLocalType[] supplierThreadLocalTypes;

	/**
	 * {@link ThreadSynchroniserFactory} instances.
	 */
	private final ThreadSynchroniserFactory[] threadSynchronisers;

	/**
	 * {@link SuppliedManagedObjectSourceType} instances.
	 */
	private final SuppliedManagedObjectSourceType[] suppliedManagedObjectTypes;

	/**
	 * Initiate.
	 * 
	 * @param supplierThreadLocalTypes   {@link SupplierThreadLocalType} instances.
	 * @param threadSynchronisers        {@link ThreadSynchroniserFactory}
	 *                                   instances.
	 * @param suppliedManagedObjectTypes {@link SuppliedManagedObjectSourceType}
	 *                                   instances.
	 */
	public SupplierTypeImpl(SupplierThreadLocalType[] supplierThreadLocalTypes,
			ThreadSynchroniserFactory[] threadSynchronisers,
			SuppliedManagedObjectSourceType[] suppliedManagedObjectTypes) {
		this.supplierThreadLocalTypes = supplierThreadLocalTypes;
		this.threadSynchronisers = threadSynchronisers;
		this.suppliedManagedObjectTypes = suppliedManagedObjectTypes;
	}

	/*
	 * ====================== SupplierType =========================
	 */

	@Override
	public SupplierThreadLocalType[] getSupplierThreadLocalTypes() {
		return this.supplierThreadLocalTypes;
	}

	@Override
	public ThreadSynchroniserFactory[] getThreadSynchronisers() {
		return this.threadSynchronisers;
	}

	@Override
	public SuppliedManagedObjectSourceType[] getSuppliedManagedObjectTypes() {
		return this.suppliedManagedObjectTypes;
	}

}