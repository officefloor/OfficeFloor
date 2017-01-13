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
package net.officefloor.frame.impl.execute.administrator;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

/**
 * Implementation of the {@link ExtensionInterfaceMetaData}.
 *
 * @author Daniel Sagenschneider
 */
public class ExtensionInterfaceMetaDataImpl<I extends Object> implements
		ExtensionInterfaceMetaData<I>, ExtensionInterfaceExtractor<I> {

	/**
	 * {@link ManagedObjectIndex} identifying the {@link ManagedObject}
	 * implementing the extension interface.
	 */
	private final ManagedObjectIndex managedObjectIndex;

	/**
	 * {@link ExtensionInterfaceFactory} to create the extension interface from
	 * the {@link ManagedObject}.
	 */
	private final ExtensionInterfaceFactory<I>[] extensionInterfaceFactories;

	/**
	 * Initiate.
	 *
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} identifying the
	 *            {@link ManagedObject} implementing the extension interface.
	 * @param extensionInterfaceFactories
	 *            {@link ExtensionInterfaceFactory} instances in the order of
	 *            the {@link ManagedObjectMetaData} instances corresponding to
	 *            the {@link ManagedObject} instances for the
	 *            {@link ManagedObjectIndex}.
	 */
	public ExtensionInterfaceMetaDataImpl(
			ManagedObjectIndex managedObjectIndex,
			ExtensionInterfaceFactory<I>[] extensionInterfaceFactories) {
		this.managedObjectIndex = managedObjectIndex;
		this.extensionInterfaceFactories = extensionInterfaceFactories;
	}

	/*
	 * ====================== ExtensionInterfaceMetaData =====================
	 */

	@Override
	public ManagedObjectIndex getManagedObjectIndex() {
		return this.managedObjectIndex;
	}

	@Override
	public ExtensionInterfaceExtractor<I> getExtensionInterfaceExtractor() {
		return this;
	}

	/*
	 * ====================== ExtensionInterfaceExtractor =====================
	 */

	@Override
	public I extractExtensionInterface(ManagedObject managedObject,
			ManagedObjectMetaData<?> managedObjectMetaData) {

		// Obtain the instance index of the Managed Object
		int instanceIndex = managedObjectMetaData.getInstanceIndex();

		// Obtain the corresponding factory for the instance
		ExtensionInterfaceFactory<I> factory = this.extensionInterfaceFactories[instanceIndex];

		// Return the extension interface
		return factory.createExtensionInterface(managedObject);
	}

}