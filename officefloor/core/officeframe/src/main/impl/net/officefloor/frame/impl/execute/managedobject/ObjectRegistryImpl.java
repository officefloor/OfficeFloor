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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * Implementation of the {@link ObjectRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectRegistryImpl<O extends Enum<O>> implements ObjectRegistry<O> {

	/**
	 * {@link ManagedFunctionContainer} to obtain the coordinating
	 * {@link ManagedObject} instances.
	 */
	private final ManagedFunctionContainer managedFunction;

	/**
	 * {@link ManagedObjectIndex} for the dependencies in the index order
	 * required.
	 */
	private final ManagedObjectIndex[] dependencies;

	/**
	 * Initiate.
	 * 
	 * @param managedFunction
	 *            {@link ManagedFunctionContainer} to obtain the coordinating
	 *            {@link ManagedObject} instances.
	 * @param dependencies
	 *            {@link ManagedObjectIndex} for the dependencies in the index
	 *            order required.
	 */
	public ObjectRegistryImpl(ManagedFunctionContainer managedFunction, ManagedObjectIndex[] dependencies) {
		this.managedFunction = managedFunction;
		this.dependencies = dependencies;
	}

	/*
	 * ===================== ObjectRegistry ===============================
	 */

	@Override
	public Object getObject(O key) {
		return this.getObject(key.ordinal());
	}

	@Override
	public Object getObject(int index) {

		// Obtain the managed object index for the dependency
		ManagedObjectIndex moIndex = this.dependencies[index];

		// Obtain the managed object container
		ManagedObjectContainer moContainer = ManagedObjectContainerImpl.getManagedObjectContainer(moIndex,
				this.managedFunction);

		// Obtain the dependency
		Object dependency = moContainer.getObject();

		// Return the dependency
		return dependency;
	}

}