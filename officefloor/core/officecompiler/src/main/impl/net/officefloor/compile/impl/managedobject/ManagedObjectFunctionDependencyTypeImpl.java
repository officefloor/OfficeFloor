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
