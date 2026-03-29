/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
