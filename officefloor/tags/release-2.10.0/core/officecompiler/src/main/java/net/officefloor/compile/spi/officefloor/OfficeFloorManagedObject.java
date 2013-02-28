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
package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObject} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObject {

	/**
	 * Obtains the name of this {@link OfficeFloorManagedObject}.
	 * 
	 * @return Name of this {@link OfficeFloorManagedObject}.
	 */
	String getOfficeFloorManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @return {@link ManagedObjectDependency}.
	 */
	ManagedObjectDependency getManagedObjectDependency(
			String managedObjectDependencyName);

}