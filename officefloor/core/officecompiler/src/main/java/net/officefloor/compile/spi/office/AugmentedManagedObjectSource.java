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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectSource} loaded from the {@link ManagedFunctionAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedManagedObjectSource {

	/**
	 * Obtains the name of this {@link AugmentedManagedObjectSource}.
	 * 
	 * @return Name of this {@link AugmentedManagedObjectSource}.
	 */
	String getAugmentedManagedObjectSourceName();

	/**
	 * Obtains the {@link AugmentedManagedObject} representing an instance use
	 * of a {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link AugmentedManagedObject}. Typically this
	 *            will be the name under which the {@link ManagedObject} will be
	 *            registered to the {@link Office}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} of the
	 *            {@link AugmentedManagedObject} within the {@link Office}.
	 * @return {@link AugmentedManagedObject}.
	 */
	AugmentedManagedObject addAugmentedManagedObject(String managedObjectName, ManagedObjectScope managedObjectScope);

}