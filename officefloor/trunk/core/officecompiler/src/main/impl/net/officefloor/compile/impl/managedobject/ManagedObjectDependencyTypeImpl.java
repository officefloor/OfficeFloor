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
package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;

/**
 * {@link ManagedObjectDependencyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyTypeImpl<D extends Enum<D>> implements
		ManagedObjectDependencyType<D> {

	/**
	 * Name describing this dependency.
	 */
	private final String name;

	/**
	 * Index identifying this dependency.
	 */
	private final int index;

	/**
	 * Type required of the dependency.
	 */
	private final Class<?> type;

	/**
	 * Type qualifier.
	 */
	private final String typeQualifier;

	/**
	 * Key identifying this dependency.
	 */
	private final D key;

	/**
	 * Initialise.
	 * 
	 * @param index
	 *            Index identifying this dependency.
	 * @param type
	 *            Type required of the dependency.
	 * @param typeQualifier
	 *            Type qualifier.
	 * @param key
	 *            Key identifying this dependency. May be <code>null</code>.
	 * @param label
	 *            Label describing the dependency. May be <code>null</code>.
	 */
	public ManagedObjectDependencyTypeImpl(int index, Class<?> type,
			String typeQualifier, D key, String label) {
		this.index = index;
		this.type = type;
		this.typeQualifier = typeQualifier;
		this.key = key;

		// Determine the name
		if (!CompileUtil.isBlank(label)) {
			this.name = label;
		} else if (this.key != null) {
			this.name = this.key.toString();
		} else {
			this.name = this.type.getSimpleName();
		}
	}

	/*
	 * ==================== ManagedObjectDependencyType =======================
	 */

	@Override
	public String getDependencyName() {
		return this.name;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getDependencyType() {
		return this.type;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public D getKey() {
		return this.key;
	}

}