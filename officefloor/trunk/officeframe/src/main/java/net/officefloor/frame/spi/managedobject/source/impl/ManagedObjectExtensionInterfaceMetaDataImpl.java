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
package net.officefloor.frame.spi.managedobject.source.impl;

import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;

/**
 * Implementation of
 * {@link net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData}.
 * 
 * @author Daniel
 */
public class ManagedObjectExtensionInterfaceMetaDataImpl<I extends Object>
		implements ManagedObjectExtensionInterfaceMetaData<I> {
	
	/**
	 * Extension interface type.
	 */
	private final Class<I> type;

	/**
	 * {@link ExtensionInterfaceFactory}.
	 */
	private final ExtensionInterfaceFactory<I> factory;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            Extension interface type.
	 * @param factory
	 *            {@link ExtensionInterfaceFactory}.
	 */
	public ManagedObjectExtensionInterfaceMetaDataImpl(Class<I> type,
			ExtensionInterfaceFactory<I> factory) {
		this.type = type;
		this.factory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData#getExtensionInterfaceType()
	 */
	public Class<I> getExtensionInterfaceType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData#getExtensionInterfaceFactory()
	 */
	public ExtensionInterfaceFactory<I> getExtensionInterfaceFactory() {
		return this.factory;
	}

}
