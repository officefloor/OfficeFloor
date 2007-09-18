/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;

/**
 * Implementationg of the
 * {@link net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData}.
 * 
 * @author Daniel
 */
public class ExtensionInterfaceMetaDataImpl<I extends Object> implements
		ExtensionInterfaceMetaData<I> {

	/**
	 * Index identifying the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * implementing the extension interface.
	 */
	protected final int managedObjectIndex;

	/**
	 * {@link ExtensionInterfaceFactory} to create the extension interface from
	 * the {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected final ExtensionInterfaceFactory<I> extensionInterfaceFactory;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectIndex
	 *            Index identifying the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            implementing the extension interface.
	 * @param extensionInterfaceFactory
	 *            {@link ExtensionInterfaceFactory} to create the extension
	 *            interface from the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	public ExtensionInterfaceMetaDataImpl(int managedObjectIndex,
			ExtensionInterfaceFactory<I> extensionInterfaceFactory) {
		// Store state
		this.managedObjectIndex = managedObjectIndex;
		this.extensionInterfaceFactory = extensionInterfaceFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData#getManagedObjectKey()
	 */
	public int getManagedObjectIndex() {
		return this.managedObjectIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData#getExtensionInterfaceFactory()
	 */
	public ExtensionInterfaceFactory<I> getExtensionInterfaceFactory() {
		return this.extensionInterfaceFactory;
	}

}
