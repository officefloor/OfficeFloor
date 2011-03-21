/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.compile.managedobject;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

/**
 * Loads the {@link ManagedObjectType} from the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedObjectSourceSpecification} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @return {@link PropertyList} of the {@link ManagedObjectSourceProperty}
	 *         instances of the {@link ManagedObjectSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> PropertyList loadSpecification(
			Class<MS> managedObjectSourceClass);

	/**
	 * Loads and returns the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            Class of the {@link ManagedObjectSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectType<D> loadManagedObjectType(
			Class<MS> managedObjectSourceClass, PropertyList propertyList);

}