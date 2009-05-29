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
package net.officefloor.frame.spi.managedobject.source.impl;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;

/**
 * Implementation of the {@link ManagedObjectDependencyMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyMetaDataImpl<D extends Enum<D>> implements
		ManagedObjectDependencyMetaData<D> {

	/**
	 * Key identifying the dependency.
	 */
	private final D key;

	/**
	 * Type of dependency required.
	 */
	private final Class<?> type;

	/**
	 * Optional label to describe the dependency.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @param type
	 *            Type of dependency.
	 */
	public ManagedObjectDependencyMetaDataImpl(D key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Specifies a label to describe the dependency.
	 * 
	 * @param label
	 *            Label to describe the dependency.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * ================= ManagedObjectDependencyMetaData =================
	 */

	@Override
	public D getKey() {
		return this.key;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}