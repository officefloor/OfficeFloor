/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.autowire.spi.supplier.source;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSourceContext extends SourceContext {

	/**
	 * Adds a potential {@link ManagedObjectSource} for dependency injection.
	 * 
	 * @param <D>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link Flow} type keys.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer} to assist in configuring the
	 *            {@link ManagedObjectSource}. May be <code>null</code> if no
	 *            assistance is required.
	 * @param autoWiring
	 *            {@link AutoWire} matches that the {@link ManagedObjectSource}
	 *            is to provide dependency injection. At least one
	 *            {@link AutoWire} must be provided.
	 * @return {@link AutoWireObject} for the {@link ManagedObjectSource}.
	 */
	<D extends Enum<D>, F extends Enum<F>> AutoWireObject addManagedObject(
			ManagedObjectSource<D, F> managedObjectSource,
			ManagedObjectSourceWirer wirer, AutoWire... autoWiring);

}