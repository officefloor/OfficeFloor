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

package net.officefloor.compile.impl.section;

import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;

/**
 * {@link OfficeSectionManagedObjectType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionManagedObjectTypeImpl implements
		OfficeSectionManagedObjectType {

	/**
	 * Name of the {@link OfficeSectionManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link TypeQualification} instances for the
	 * {@link OfficeSectionManagedObject}.
	 */
	private final TypeQualification[] typeQualifications;

	/**
	 * Extension interfaces for the {@link OfficeSectionManagedObject}.
	 */
	private final Class<?>[] extensionInterfaces;

	/**
	 * {@link ObjectDependencyType} instances of the dependent objects.
	 */
	private final ObjectDependencyType[] objectDependencies;

	/**
	 * {@link OfficeSectionManagedObjectSourceType}.
	 */
	private final OfficeSectionManagedObjectSourceType managedObjectSourceType;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeSectionManagedObject}.
	 * @param typeQualifications
	 *            {@link TypeQualification} instances for the
	 *            {@link OfficeSectionManagedObject}.
	 * @param extensionInterfaces
	 *            Extension interfaces for the
	 *            {@link OfficeSectionManagedObject}.
	 * @param objectDependencies
	 *            {@link ObjectDependencyType} instances of the dependent
	 *            objects.
	 * @param managedObjectSourceType
	 *            {@link OfficeSectionManagedObjectSourceType}.
	 */
	public OfficeSectionManagedObjectTypeImpl(String managedObjectName,
			TypeQualification[] typeQualifications,
			Class<?>[] extensionInterfaces,
			ObjectDependencyType[] objectDependencies,
			OfficeSectionManagedObjectSourceType managedObjectSourceType) {
		this.managedObjectName = managedObjectName;
		this.typeQualifications = typeQualifications;
		this.extensionInterfaces = extensionInterfaces;
		this.objectDependencies = objectDependencies;
		this.managedObjectSourceType = managedObjectSourceType;
	}

	/*
	 * ================== OfficeSectionManagedObjectType =================
	 */

	@Override
	public String getOfficeSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public Class<?>[] getSupportedExtensionInterfaces() {
		return this.extensionInterfaces;
	}

	/*
	 * ================== DependentManagedObjectType =================
	 */

	@Override
	public String getDependentObjectName() {
		return this.managedObjectName;
	}

	@Override
	public TypeQualification[] getTypeQualifications() {
		return this.typeQualifications;
	}

	@Override
	public ObjectDependencyType[] getObjectDependencies() {
		return this.objectDependencies;
	}

	@Override
	public OfficeSectionManagedObjectSourceType getOfficeSectionManagedObjectSourceType() {
		return this.managedObjectSourceType;
	}

}
