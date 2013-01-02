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
package net.officefloor.autowire.impl.supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;

/**
 * {@link SuppliedManagedObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectTypeImpl implements SuppliedManagedObjectType {

	/**
	 * {@link AutoWire} instances.
	 */
	private final AutoWire[] autoWiring;

	/**
	 * Indicates if this is an {@link OfficeFloorInputManagedObject},
	 */
	private final boolean isInputManagedObject;

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
	 * @param autoWiring
	 *            {@link AutoWire} instances.
	 * @param isInputManagedObject
	 *            Indicates if this is an {@link OfficeFloorInputManagedObject},
	 * @param dependencyTypes
	 *            {@link SuppliedManagedObjectDependencyType} instances.
	 * @param flowTypes
	 *            {@link SuppliedManagedObjectFlowType} instances.
	 * @param teamTypes
	 *            {@link SuppliedManagedObjectTeamType} instances.
	 * @param extensionInterfaces
	 *            Extension interfaces.
	 */
	public SuppliedManagedObjectTypeImpl(AutoWire[] autoWiring,
			boolean isInputManagedObject,
			SuppliedManagedObjectDependencyType[] dependencyTypes,
			SuppliedManagedObjectFlowType[] flowTypes,
			SuppliedManagedObjectTeamType[] teamTypes,
			Class<?>[] extensionInterfaces) {
		this.autoWiring = autoWiring;
		this.isInputManagedObject = isInputManagedObject;
		this.dependencyTypes = dependencyTypes;
		this.flowTypes = flowTypes;
		this.teamTypes = teamTypes;
		this.extensionInterfaces = extensionInterfaces;
	}

	/*
	 * ======================== SuppliedManagedObjectType ======================
	 */

	@Override
	public AutoWire[] getAutoWiring() {
		return this.autoWiring;
	}

	@Override
	public boolean isInputManagedObject() {
		return this.isInputManagedObject;
	}

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