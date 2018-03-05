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

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectTypeImpl<D extends Enum<D>> implements ManagedObjectType<D> {

	/**
	 * {@link Class} of the {@link Object} returned from the {@link ManagedObject}.
	 */
	private final Class<?> objectClass;

	/**
	 * Indicates if may trigger a {@link Flow}.
	 */
	private final boolean isInput;

	/**
	 * {@link ManagedObjectDependencyType} instances.
	 */
	private final ManagedObjectDependencyType<D>[] dependencies;

	/**
	 * {@link ManagedObjectFlowType} instances.
	 */
	private final ManagedObjectFlowType<?>[] flows;

	/**
	 * {@link ManagedObjectTeamType} instances.
	 */
	private final ManagedObjectTeamType[] teams;

	/**
	 * Extension interfaces supported by the {@link ManagedObject}.
	 */
	private final Class<?>[] extensionTypes;

	/**
	 * Initiate.
	 * 
	 * @param objectClass
	 *            {@link Class} of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param isInput
	 *            Indicates if may trigger a {@link Flow}.
	 * @param dependencies
	 *            {@link ManagedObjectDependencyType} instances.
	 * @param flows
	 *            {@link ManagedObjectFlowType} instances.
	 * @param teams
	 *            {@link ManagedObjectTeamType} instances.
	 * @param extensionTypes
	 *            Extension types supported by the {@link ManagedObject}.
	 */
	public ManagedObjectTypeImpl(Class<?> objectClass, boolean isInput, ManagedObjectDependencyType<D>[] dependencies,
			ManagedObjectFlowType<?>[] flows, ManagedObjectTeamType[] teams, Class<?>[] extensionTypes) {
		this.objectClass = objectClass;
		this.isInput = isInput;
		this.dependencies = dependencies;
		this.flows = flows;
		this.teams = teams;
		this.extensionTypes = extensionTypes;
	}

	/*
	 * ====================== ManagedObjectType ================================
	 */

	@Override
	public String getObjectType() {
		return this.objectClass.getName();
	}

	@Override
	public boolean isInput() {
		return this.isInput;
	}

	@Override
	public ManagedObjectDependencyType<D>[] getDependencyTypes() {
		return this.dependencies;
	}

	@Override
	public ManagedObjectFlowType<?>[] getFlowTypes() {
		return this.flows;
	}

	@Override
	public ManagedObjectTeamType[] getTeamTypes() {
		return this.teams;
	}

	@Override
	public String[] getExtensionTypes() {
		String[] extensionTypeNames = new String[this.extensionTypes.length];
		for (int i = 0; i < extensionTypeNames.length; i++) {
			extensionTypeNames[i] = this.extensionTypes[i].getName();
		}
		return extensionTypeNames;
	}

}