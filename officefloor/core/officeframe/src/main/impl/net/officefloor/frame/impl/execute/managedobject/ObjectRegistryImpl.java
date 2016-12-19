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

import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;

/**
 * Implementation of the {@link ObjectRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectRegistryImpl<D extends Enum<D>> implements ObjectRegistry<D> {

	/**
	 * {@link WorkContainer} to obtain the coordinating {@link ManagedObject}.
	 */
	private final WorkContainer<?> workContainer;

	/**
	 * {@link ManagedObjectIndex} for the dependencies in the index order
	 * required.
	 */
	private final ManagedObjectIndex[] dependencies;

	/**
	 * {@link ThreadState} requesting the coordinating of the
	 * {@link ManagedObject}.
	 */
	private final ThreadState threadState;

	/**
	 * Initiate.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer} to obtain the coordinating
	 *            {@link ManagedObject}.
	 * @param dependencies
	 *            {@link ManagedObjectIndex} for the dependencies in the index
	 *            order required.
	 * @param threadState
	 *            {@link ThreadState} requesting the coordinating of the
	 *            {@link ManagedObject}. This is used to access the
	 *            {@link ProcessState} bound {@link ManagedObject} instances.
	 */
	public ObjectRegistryImpl(WorkContainer<?> workContainer, ManagedObjectIndex[] dependencies,
			ThreadState threadState) {
		this.workContainer = workContainer;
		this.dependencies = dependencies;
		this.threadState = threadState;
	}

	/*
	 * ===================== ObjectRegistry ===============================
	 */

	@Override
	public Object getObject(D key) {
		return this.getObject(key.ordinal());
	}

	@Override
	public Object getObject(int index) {

		// Obtain the managed object index for the dependency
		ManagedObjectIndex moIndex = this.dependencies[index];

		// Obtain the dependency
		Object dependency = this.workContainer.getObject(moIndex);

		// Return the dependency
		return dependency;
	}

}