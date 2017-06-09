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
package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.compile.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.compile.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.compile.supplier.SuppliedManagedObjectType;

/**
 * {@link SuppliedManagedObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectTypeImpl implements SuppliedManagedObjectType {

	/**
	 * {@link SuppliedManagedObjectDependencyType} instances.
	 */
	private final SuppliedManagedObjectDependencyType[] dependencyTypes;

	/**
	 * {@link SuppliedManagedObjectFlowType} instances.
	 */
	private final SuppliedManagedObjectFlowType[] flowTypes;

	/**
	 * {@link SuppliedManagedObjectTeamType} instances.
	 */
	private final SuppliedManagedObjectTeamType[] teamTypes;

	/**
	 * Extension interfaces.
	 */
	private final Class<?>[] extensionInterfaces;

	/**
	 * Initiate.
	 * 
	 * @param dependencyTypes
	 *            {@link SuppliedManagedObjectDependencyType} instances.
	 * @param flowTypes
	 *            {@link SuppliedManagedObjectFlowType} instances.
	 * @param teamTypes
	 *            {@link SuppliedManagedObjectTeamType} instances.
	 * @param extensionInterfaces
	 *            Extension interfaces.
	 */
	public SuppliedManagedObjectTypeImpl(SuppliedManagedObjectDependencyType[] dependencyTypes,
			SuppliedManagedObjectFlowType[] flowTypes, SuppliedManagedObjectTeamType[] teamTypes,
			Class<?>[] extensionInterfaces) {
		this.dependencyTypes = dependencyTypes;
		this.flowTypes = flowTypes;
		this.teamTypes = teamTypes;
		this.extensionInterfaces = extensionInterfaces;
	}

	/*
	 * ======================== SuppliedManagedObjectType ======================
	 */

	@Override
	public SuppliedManagedObjectDependencyType[] getDependencyTypes() {
		return this.dependencyTypes;
	}

	@Override
	public SuppliedManagedObjectFlowType[] getFlowTypes() {
		return this.flowTypes;
	}

	@Override
	public SuppliedManagedObjectTeamType[] getTeamTypes() {
		return this.teamTypes;
	}

	@Override
	public Class<?>[] getExtensionInterfaces() {
		return this.extensionInterfaces;
	}

}