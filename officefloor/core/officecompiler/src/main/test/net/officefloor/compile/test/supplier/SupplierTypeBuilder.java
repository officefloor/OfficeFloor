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
