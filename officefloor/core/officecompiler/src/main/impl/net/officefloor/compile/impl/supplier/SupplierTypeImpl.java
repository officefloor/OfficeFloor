/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * {@link InitialSupplierType} implementation.
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
	 * {@link InternalSupplier} instances.
	 */
	private final InternalSupplier[] internalSuppliers;

	/**
	 * Initiate.
	 * 
	 * @param supplierThreadLocalTypes   {@link SupplierThreadLocalType} instances.
	 * @param threadSynchronisers        {@link ThreadSynchroniserFactory}
	 *                                   instances.
	 * @param suppliedManagedObjectTypes {@link SuppliedManagedObjectSourceType}
	 *                                   instances.
	 * @param internalSuppliers          {@link InternalSupplier} instances.
	 */
	public SupplierTypeImpl(SupplierThreadLocalType[] supplierThreadLocalTypes,
			ThreadSynchroniserFactory[] threadSynchronisers,
			SuppliedManagedObjectSourceType[] suppliedManagedObjectTypes, InternalSupplier[] internalSuppliers) {
		this.supplierThreadLocalTypes = supplierThreadLocalTypes;
		this.threadSynchronisers = threadSynchronisers;
		this.suppliedManagedObjectTypes = suppliedManagedObjectTypes;
		this.internalSuppliers = internalSuppliers;
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

	@Override
	public InternalSupplier[] getInternalSuppliers() {
		return this.internalSuppliers;
	}

}
