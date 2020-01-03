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
