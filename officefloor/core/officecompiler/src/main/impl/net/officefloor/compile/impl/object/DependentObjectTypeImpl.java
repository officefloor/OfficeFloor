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

import net.officefloor.compile.internal.structure.DependentObjectNode;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.TypeQualification;

/**
 * {@link DependentObjectType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class DependentObjectTypeImpl implements DependentObjectType {

	/**
	 * Name of the {@link DependentObjectNode}.
	 */
	private final String objectName;

	/**
	 * {@link TypeQualification} instances for the {@link DependentObjectNode}.
	 */
	private TypeQualification[] typeQualifications;

	/**
	 * {@link ObjectDependencyType} instances for the
	 * {@link DependentObjectNode}.
	 */
	private ObjectDependencyType[] dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link DependentObjectNode}.
	 * @param typeQualifications
	 *            {@link TypeQualification} instances for the
	 *            {@link DependentObjectNode}.
	 * @param dependencies
	 *            {@link ObjectDependencyType} instances for the
	 *            {@link DependentObjectNode}.
	 */
	public DependentObjectTypeImpl(String objectName,
			TypeQualification[] typeQualifications,
			ObjectDependencyType[] dependencies) {
		this.objectName = objectName;
		this.typeQualifications = typeQualifications;
		this.dependencies = dependencies;
	}

	/*
	 * ==================== DependentObjectType ============================
	 */

	@Override
	public String getDependentObjectName() {
		return this.objectName;
	}

	@Override
	public TypeQualification[] getTypeQualifications() {
		return this.typeQualifications;
	}

	@Override
	public ObjectDependencyType[] getObjectDependencies() {
		return this.dependencies;
	}

}
