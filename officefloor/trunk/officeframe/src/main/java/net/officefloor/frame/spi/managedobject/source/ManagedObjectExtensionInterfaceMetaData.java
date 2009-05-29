/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;

/**
 * Meta-data regarding an extension interface implemented by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionInterfaceMetaData<I> {

	/**
	 * Obtains the type of extension interface.
	 * 
	 * @return {@link Class} representing the type of extension interface.
	 */
	Class<I> getExtensionInterfaceType();

	/**
	 * Obtains the {@link ExtensionInterfaceFactory} to create the extension
	 * interface for the {@link ManagedObject}.
	 * 
	 * @return {@link ExtensionInterfaceFactory} to create the extension
	 *         interface for the {@link ManagedObject}.
	 */
	ExtensionInterfaceFactory<I> getExtensionInterfaceFactory();

}