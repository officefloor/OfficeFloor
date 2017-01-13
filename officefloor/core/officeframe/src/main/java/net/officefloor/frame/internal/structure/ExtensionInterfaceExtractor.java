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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Extracts the extension interface from the {@link ManagedObject} within the
 * {@link ManagedObjectContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExtensionInterfaceExtractor<E> {

	/**
	 * Extracts the extension interface from the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to extract the extension interface from.
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject} to
	 *            aid in extracting the extension interface.
	 * @return Extension Interface.
	 */
	E extractExtensionInterface(ManagedObject managedObject, ManagedObjectMetaData<?> managedObjectMetaData);

}