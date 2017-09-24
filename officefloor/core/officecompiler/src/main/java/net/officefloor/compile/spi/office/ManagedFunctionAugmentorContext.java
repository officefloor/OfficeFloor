/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link ManagedFunctionAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionAugmentorContext {

	/**
	 * Obtains the {@link ManagedFunctionType} of the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionType} of the {@link ManagedFunction}.
	 */
	ManagedFunctionType<?, ?> getManagedFunctionType();

	/**
	 * Obtains the {@link AugmentedFunctionObject} for the
	 * {@link ManagedFunction}.
	 * 
	 * @param objectName
	 *            Name of the {@link FunctionObject} on the
	 *            {@link ManagedFunction}.
	 * @return {@link AugmentedFunctionObject}.
	 */
	AugmentedFunctionObject getFunctionObject(String objectName);

	/**
	 * Adds a {@link AugmentedManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link AugmentedManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @return Added {@link AugmentedManagedObjectSource}.
	 */
	AugmentedManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName);

	/**
	 * Adds a {@link AugmentedManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link AugmentedManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 * @return Added {@link AugmentedManagedObjectSource}.
	 */
	AugmentedManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Links the {@link AugmentedManagedObject} to the
	 * {@link AugmentedFunctionObject}.
	 * 
	 * @param object
	 *            {@link AugmentedFunctionObject}.
	 * @param managedObject
	 *            {@link AugmentedManagedObject}.
	 */
	void link(AugmentedFunctionObject object, AugmentedManagedObject managedObject);

}