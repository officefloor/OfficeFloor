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
package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;

/**
 * Meta-data regarding an extension interface implemented by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionInterfaceMetaData<E> {

	/**
	 * Obtains the type of extension interface.
	 * 
	 * @return {@link Class} representing the type of extension interface.
	 */
	Class<E> getExtensionInterfaceType();

	/**
	 * Obtains the {@link ExtensionInterfaceFactory} to create the extension
	 * interface for the {@link ManagedObject}.
	 * 
	 * @return {@link ExtensionInterfaceFactory} to create the extension
	 *         interface for the {@link ManagedObject}.
	 */
	ExtensionInterfaceFactory<E> getExtensionInterfaceFactory();

}