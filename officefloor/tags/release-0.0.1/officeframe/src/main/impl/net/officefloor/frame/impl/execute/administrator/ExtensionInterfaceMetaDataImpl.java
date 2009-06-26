/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.administrator;

import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;

/**
 * Implementation of the {@link ExtensionInterfaceMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtensionInterfaceMetaDataImpl<I extends Object> implements
		ExtensionInterfaceMetaData<I> {

	/**
	 * {@link ManagedObjectIndex} identifying the {@link ManagedObject}
	 * implementing the extension interface.
	 */
	protected final ManagedObjectIndex managedObjectIndex;

	/**
	 * {@link ExtensionInterfaceFactory} to create the extension interface from
	 * the {@link ManagedObject}.
	 */
	protected final ExtensionInterfaceFactory<I> extensionInterfaceFactory;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} identifying the
	 *            {@link ManagedObject} implementing the extension interface.
	 * @param extensionInterfaceFactory
	 *            {@link ExtensionInterfaceFactory} to create the extension
	 *            interface from the {@link ManagedObject}.
	 */
	public ExtensionInterfaceMetaDataImpl(
			ManagedObjectIndex managedObjectIndex,
			ExtensionInterfaceFactory<I> extensionInterfaceFactory) {
		this.managedObjectIndex = managedObjectIndex;
		this.extensionInterfaceFactory = extensionInterfaceFactory;
	}

	/*
	 * ====================== ExtensionInterfaceMetaData =====================
	 */

	@Override
	public ManagedObjectIndex getManagedObjectIndex() {
		return this.managedObjectIndex;
	}

	@Override
	public ExtensionInterfaceFactory<I> getExtensionInterfaceFactory() {
		return this.extensionInterfaceFactory;
	}

}