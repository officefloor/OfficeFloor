/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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