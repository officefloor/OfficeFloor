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

package net.officefloor.compile.test.supplier;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * Builder for the {@link InitialSupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierTypeBuilder {

	/**
	 * Adds a {@link SupplierThreadLocalType}.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type for the {@link SupplierThreadLocalType}.
	 */
	void addSupplierThreadLocal(String qualifier, Class<?> objectType);

	/**
	 * Adds a {@link ThreadSynchroniserFactory}.
	 */
	void addThreadSynchroniser();

	/**
	 * Adds an {@link InternalSupplier}.
	 */
	void addInternalSupplier();

	/**
	 * Adds a {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param <O>                 Dependency keys type.
	 * @param <F>                 Flow keys type.
	 * @param <MS>                {@link ManagedObjectSource} type.
	 * @param qualifier           Qualifier. May be <code>null</code>.
	 * @param objectType          Object type for the
	 *                            {@link SuppliedManagedObjectSourceType}.
	 * @param managedObjectSource Expected {@link ManagedObjectSource}.
	 * @return {@link PropertyList} to load the expected {@link Property} instances.
	 */
	<O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> PropertyList addSuppliedManagedObjectSource(
			String qualifier, Class<?> objectType, MS managedObjectSource);
}
