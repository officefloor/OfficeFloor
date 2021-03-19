/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
