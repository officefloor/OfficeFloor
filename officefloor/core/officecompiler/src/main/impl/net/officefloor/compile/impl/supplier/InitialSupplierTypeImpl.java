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
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompileConfiguration;
import net.officefloor.compile.spi.supplier.source.SupplierCompileContext;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * {@link InitialSupplierType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class InitialSupplierTypeImpl extends SupplierTypeImpl implements InitialSupplierType {

	/**
	 * {@link SupplierCompileCompletion} instances.
	 */
	private final SupplierCompileCompletion[] compileCompletions;

	/**
	 * {@link SupplierCompileConfiguration}.
	 */
	private final SupplierCompileConfiguration compileConfiguration;

	/**
	 * Initiate.
	 * 
	 * @param supplierThreadLocalTypes   {@link SupplierThreadLocalType} instances.
	 * @param threadSynchronisers        {@link ThreadSynchroniserFactory}
	 *                                   instances.
	 * @param suppliedManagedObjectTypes {@link SuppliedManagedObjectSourceType}
	 *                                   instances.
	 * @param internalSuppliers          {@link InternalSupplier} instances.
	 * @param compileCompletions         {@link SupplierCompileCompletion}.
	 * @param compileConfiguration       {@link SupplierCompileContext}.
	 */
	public InitialSupplierTypeImpl(SupplierThreadLocalType[] supplierThreadLocalTypes,
			ThreadSynchroniserFactory[] threadSynchronisers,
			SuppliedManagedObjectSourceType[] suppliedManagedObjectTypes, InternalSupplier[] internalSuppliers,
			SupplierCompileCompletion[] compileCompletions, SupplierCompileConfiguration compileConfiguration) {
		super(supplierThreadLocalTypes, threadSynchronisers, suppliedManagedObjectTypes, internalSuppliers);
		this.compileCompletions = compileCompletions;
		this.compileConfiguration = compileConfiguration;
	}

	/*
	 * ================= InitialSupplierType =====================
	 */

	@Override
	public SupplierCompileCompletion[] getCompileCompletions() {
		return this.compileCompletions;
	}

	@Override
	public SupplierCompileConfiguration getCompileConfiguration() {
		return this.compileConfiguration;
	}

}
