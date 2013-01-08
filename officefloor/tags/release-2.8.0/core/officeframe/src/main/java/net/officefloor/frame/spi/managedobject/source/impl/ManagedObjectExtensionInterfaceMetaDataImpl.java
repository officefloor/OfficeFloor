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
package net.officefloor.frame.spi.managedobject.source.impl;

import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;

/**
 * Implementation of {@link ManagedObjectExtensionInterfaceMetaData}.
 * 
 * @author Daniel Sagenschneider
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
	 * ================== ManagedObjectExtensionInterfaceMetaData ==============
	 */

	@Override
	public Class<I> getExtensionInterfaceType() {
		return this.type;
	}

	@Override
	public ExtensionInterfaceFactory<I> getExtensionInterfaceFactory() {
		return this.factory;
	}

}