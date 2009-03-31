/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.spi.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.spi.managedobject.ManagedObjectType;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObjectType} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectTypeImpl<D extends Enum<D>> implements
		ManagedObjectType<D> {

	/**
	 * {@link Class} of the {@link Object} returned from the
	 * {@link ManagedObject}.
	 */
	private final Class<?> objectClass;

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
	private final Class<?>[] extensionInterfaces;

	/**
	 * Initiate.
	 * 
	 * @param objectClass
	 *            {@link Class} of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param dependencies
	 *            {@link ManagedObjectDependencyType} instances.
	 * @param flows
	 *            {@link ManagedObjectFlowType} instances.
	 * @param teams
	 *            {@link ManagedObjectTeamType} instances.
	 * @param extensionInterfaces
	 *            Extension interfaces supported by the {@link ManagedObject}.
	 */
	public ManagedObjectTypeImpl(Class<?> objectClass,
			ManagedObjectDependencyType<D>[] dependencies,
			ManagedObjectFlowType<?>[] flows, ManagedObjectTeamType[] teams,
			Class<?>[] extensionInterfaces) {
		this.objectClass = objectClass;
		this.dependencies = dependencies;
		this.flows = flows;
		this.teams = teams;
		this.extensionInterfaces = extensionInterfaces;
	}

	/*
	 * ====================== ManagedObjectType ================================
	 */

	@Override
	public Class<?> getObjectClass() {
		return this.objectClass;
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
	public Class<?>[] getExtensionInterfaces() {
		return this.extensionInterfaces;
	}

}