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

import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;

/**
 * Implementation of {@link ManagedObjectExtensionMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExtensionMetaDataImpl<E extends Object> implements ManagedObjectExtensionMetaData<E> {

	/**
	 * Extension type.
	 */
	private final Class<E> type;

	/**
	 * {@link ExtensionFactory}.
	 */
	private final ExtensionFactory<E> factory;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            Extension type.
	 * @param factory
	 *            {@link ExtensionFactory}.
	 */
	public ManagedObjectExtensionMetaDataImpl(Class<E> type, ExtensionFactory<E> factory) {
		this.type = type;
		this.factory = factory;
	}

	/*
	 * ================== ManagedObjectExtensionMetaData ==============
	 */

	@Override
	public Class<E> getExtensionType() {
		return this.type;
	}

	@Override
	public ExtensionFactory<E> getExtensionFactory() {
		return this.factory;
	}

}