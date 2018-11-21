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
package net.officefloor.compile.spi.supplier.source;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSourceContext extends SourceContext {

	/**
	 * <p>
	 * Adds a {@link SupplierThreadLocal}.
	 * <p>
	 * This allows integrating third party libraries that require
	 * {@link ThreadLocal} access to objects.
	 *
	 * @param           <T> Type of object to be returned from the
	 *                  {@link SupplierThreadLocal}.
	 * @param qualifier Qualifier for the {@link AutoWire}. May be <code>null</code>
	 *                  to match only on type.
	 * @param type      Type for {@link AutoWire}.
	 * @return {@link SupplierThreadLocal} to obtain the object.
	 */
	<T> SupplierThreadLocal<T> addSupplierThreadLocal(String qualifier, Class<? extends T> type);

	/**
	 * <p>
	 * Adds a {@link ThreadSynchroniser}.
	 * <p>
	 * This enables keeping the {@link ThreadLocal} instances of the integrating
	 * third party library consistent across the {@link Thread} instances.
	 * 
	 * @param threadSynchroniserFactory {@link ThreadSynchroniserFactory}.
	 */
	void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory);

	/**
	 * Adds a potential {@link ManagedObjectSource} for dependency injection.
	 * 
	 * @param                     <D> Dependency type keys.
	 * @param                     <F> {@link Flow} type keys.
	 * @param qualifier           Qualifier for the {@link ManagedObjectSource}. May
	 *                            be <code>null</code> to match only on type.
	 * @param type                Type of the {@link ManagedObjectSource}.
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @return {@link SuppliedManagedObjectSource}.
	 */
	<D extends Enum<D>, F extends Enum<F>> SuppliedManagedObjectSource addManagedObjectSource(String qualifier,
			Class<?> type, ManagedObjectSource<D, F> managedObjectSource);

}