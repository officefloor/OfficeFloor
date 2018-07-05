/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;

/**
 * Implementation of the {@link ManagedObjectDependencyMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyMetaDataImpl<O extends Enum<O>> implements ManagedObjectDependencyMetaData<O> {

	/**
	 * Key identifying the dependency.
	 */
	private final O key;

	/**
	 * Type of dependency required.
	 */
	private final Class<?> type;

	/**
	 * Optional qualifier for the type.
	 */
	private String qualifier = null;

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
	public ManagedObjectDependencyMetaDataImpl(O key, Class<?> type) {
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

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	public void setTypeQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/*
	 * ================= ManagedObjectDependencyMetaData =================
	 */

	@Override
	public O getKey() {
		return this.key;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

	@Override
	public String getTypeQualifier() {
		return this.qualifier;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}