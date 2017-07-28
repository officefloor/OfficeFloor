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
package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;

/**
 * Implementation of {@link ManagedObjectExtensionInterfaceMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExtensionInterfaceMetaDataImpl<E extends Object>
		implements ManagedObjectExtensionInterfaceMetaData<E> {

	/**
	 * Extension interface type.
	 */
	private final Class<E> type;

	/**
	 * {@link ExtensionInterfaceFactory}.
	 */
	private final ExtensionInterfaceFactory<E> factory;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            Extension interface type.
	 * @param factory
	 *            {@link ExtensionInterfaceFactory}.
	 */
	public ManagedObjectExtensionInterfaceMetaDataImpl(Class<E> type, ExtensionInterfaceFactory<E> factory) {
		this.type = type;
		this.factory = factory;
	}

	/*
	 * ================== ManagedObjectExtensionInterfaceMetaData ==============
	 */

	@Override
	public Class<E> getExtensionInterfaceType() {
		return this.type;
	}

	@Override
	public ExtensionInterfaceFactory<E> getExtensionInterfaceFactory() {
		return this.factory;
	}

}