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

	/**
	 * Obtains the {@link InternalSupplier} instances.
	 * 
	 * @return {@link InternalSupplier} instances.
	 */
	InternalSupplier[] getInternalSuppliers();

}
