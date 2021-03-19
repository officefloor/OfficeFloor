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
