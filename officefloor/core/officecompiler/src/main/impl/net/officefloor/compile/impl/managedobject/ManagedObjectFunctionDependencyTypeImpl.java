/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * {@link ManagedObjectFunctionDependencyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFunctionDependencyTypeImpl implements ManagedObjectFunctionDependencyType {

	/**
	 * {@link ManagedObjectFunctionDependency} name.
	 */
	private final String name;

	/**
	 * {@link ManagedObjectFunctionDependency} type.
	 */
	private final Class<?> type;

	/**
	 * {@link ManagedObjectFunctionDependency} type qualifier.
	 */
	private final String typeQualifier;

	/**
	 * Instantiate.
	 * 
	 * @param name          {@link ManagedObjectFunctionDependency} name.
	 * @param type          {@link ManagedObjectFunctionDependency} type.
	 * @param typeQualifier {@link ManagedObjectFunctionDependency} type qualifier.
	 */
	public ManagedObjectFunctionDependencyTypeImpl(String name, Class<?> type, String typeQualifier) {
		this.name = name;
		this.type = type;
		this.typeQualifier = typeQualifier;
	}

	/*
	 * ================= ManagedObjectFunctionDependencyType =========
	 */

	@Override
	public String getFunctionObjectName() {
		return this.name;
	}

	@Override
	public Class<?> getFunctionObjectType() {
		return this.type;
	}

	@Override
	public String getFunctionObjectTypeQualifier() {
		return this.typeQualifier;
	}

}
