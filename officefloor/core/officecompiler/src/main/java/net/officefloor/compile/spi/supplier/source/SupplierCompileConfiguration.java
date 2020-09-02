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
