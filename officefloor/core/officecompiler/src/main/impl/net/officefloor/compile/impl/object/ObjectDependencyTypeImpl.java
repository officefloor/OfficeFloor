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

package net.officefloor.compile.impl.object;

import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;

/**
 * {@link ObjectDependencyType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ObjectDependencyTypeImpl implements ObjectDependencyType {

	/**
	 * Name of the object dependency.
	 */
	private final String dependencyName;

	/**
	 * Fully qualified type of the object dependency.
	 */
	private final String dependencyType;

	/**
	 * Type qualifier of the object dependency.
	 */
	private final String typeQualifier;

	/**
	 * Flag indicating if parameter.
	 */
	private final boolean isParameter;

	/**
	 * {@link DependentObjectType} fulfilling the object dependency.
	 */
	private final DependentObjectType dependentObjectType;

	/**
	 * Instantiate.
	 * 
	 * @param dependencyName
	 *            Name of the object dependency.
	 * @param dependencyType
	 *            Fully qualified type of the object dependency.
	 * @param typeQualifier
	 *            Type qualifier of the object dependency.
	 * @param isParameter
	 *            Flag indicating if parameter.
	 * @param dependentObjectType
	 *            {@link DependentObjectType} fulfilling the object dependency.
	 */
	public ObjectDependencyTypeImpl(String dependencyName,
			String dependencyType, String typeQualifier, boolean isParameter,
			DependentObjectType dependentObjectType) {
		this.dependencyName = dependencyName;
		this.dependencyType = dependencyType;
		this.typeQualifier = typeQualifier;
		this.isParameter = isParameter;
		this.dependentObjectType = dependentObjectType;
	}

	/*
	 * ================= ObjectDependencyType =====================
	 */

	@Override
	public String getObjectDependencyName() {
		return this.dependencyName;
	}

	@Override
	public String getObjectDependencyType() {
		return this.dependencyType;
	}

	@Override
	public String getObjectDependencyTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public boolean isParameter() {
		return this.isParameter;
	}

	@Override
	public DependentObjectType getDependentObjectType() {
		return this.dependentObjectType;
	}

}
