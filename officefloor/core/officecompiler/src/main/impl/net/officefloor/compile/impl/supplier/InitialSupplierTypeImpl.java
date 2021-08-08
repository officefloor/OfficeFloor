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
