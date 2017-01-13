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
package net.officefloor.compile;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Encapsulates {@link ClassLoader} handling to load the various
 * {@link OfficeFloor} types.
 * <p>
 * As the {@link OfficeFloorCompiler} being used may be loading {@link Class}
 * instances via an alternate {@link ClassLoader}, this interface provides means
 * to use that alternate {@link ClassLoader} to load the necessary
 * {@link OfficeFloor} types.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeLoader {

	/**
	 * Loads the {@link FunctionNamespaceType}.
	 * 
	 * @param workSourceClassName
	 *            {@link ManagedFunctionSource} class name.
	 * @param properties
	 *            {@link PropertyList}.
	 * @return {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType<?> loadWorkType(String workSourceClassName, PropertyList properties);

	/**
	 * Loads the {@link ManagedObjectType}.
	 * 
	 * @param managedObjectSourceClassName
	 *            {@link ManagedObjectSource} class name.
	 * @param properties
	 *            {@link PropertyList}.
	 * @return {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(
			String managedObjectSourceClassName, PropertyList properties);

}