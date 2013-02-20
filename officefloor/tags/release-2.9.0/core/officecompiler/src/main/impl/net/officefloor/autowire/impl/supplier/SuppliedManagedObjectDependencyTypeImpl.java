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
package net.officefloor.autowire.impl.supplier;

import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;

/**
 * {@link SuppliedManagedObjectDependencyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectDependencyTypeImpl implements
		SuppliedManagedObjectDependencyType {

	/**
	 * Dependency name.
	 */
	private final String dependencyName;

	/**
	 * Dependency type.
	 */
	private final String dependencyType;

	/**
	 * Dependency qualifier.
	 */
	private final String dependencyQualifier;

	/**
	 * Initiate.
	 * 
	 * @param dependencyName
	 *            Dependency name.
	 * @param dependencyType
	 *            Dependency type.
	 * @param dependencyQualifier
	 *            Dependency qualifier.
	 */
	public SuppliedManagedObjectDependencyTypeImpl(String dependencyName,
			String dependencyType, String dependencyQualifier) {
		this.dependencyName = dependencyName;
		this.dependencyType = dependencyType;
		this.dependencyQualifier = dependencyQualifier;
	}

	/*
	 * ================== SuppliedManagedObjectDependencyType ==============
	 */

	@Override
	public String getDependencyName() {
		return this.dependencyName;
	}

	@Override
	public String getDependencyType() {
		return this.dependencyType;
	}

	@Override
	public String getTypeQualifier() {
		return this.dependencyQualifier;
	}

}