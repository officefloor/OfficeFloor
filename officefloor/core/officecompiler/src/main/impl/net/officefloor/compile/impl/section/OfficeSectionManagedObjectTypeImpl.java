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
