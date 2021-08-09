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

package net.officefloor.compile.supplier;

import net.officefloor.compile.spi.supplier.source.InternalSupplier;
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

	/**
	 * Obtains the {@link InternalSupplier} instances from the
	 * {@link SupplierSource}.
	 * 
	 * @return {@link InternalSupplier} instances from the {@link SupplierSource}.
	 */
	InternalSupplier[] getInternalSuppliers();
}
