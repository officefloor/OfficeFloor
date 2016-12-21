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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Container managing the state for a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // to be merged into AbstractJobContainer
public interface WorkContainer<W extends Work> {

	/**
	 * Obtains the {@link Work} being managed.
	 * 
	 * @param threadState
	 *            {@link ThreadState} requiring the {@link Work}.
	 * @return {@link Work} being managed.
	 */
	@Deprecated // use managed object for state
	W getWork(ThreadState threadState);

	/**
	 * Loads the required {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances to be loaded.
	 * @param managedJobNode
	 *            {@link ManagedFunctionContainer} requiring the {@link ManagedObject}.
	 * @return {@link FunctionState} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link FunctionState} instances are required to
	 *         load {@link ManagedObject} instances.
	 */
	FunctionState loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes, ManagedFunctionContainer managedJobNode);

	/**
	 * Obtains the {@link ManagedObjectContainer} for the
	 * {@link ManagedObjectIndex}.
	 * 
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} identify the
	 *            {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer} for the
	 *         {@link ManagedObjectIndex}.
	 */
	ManagedObjectContainer getManagedObjectContainer(ManagedObjectIndex managedObjectIndex);

	/**
	 * Administers the {@link ManagedObject} instances as per the input
	 * {@link TaskDutyAssociation}.
	 * 
	 * @param duty
	 *            {@link TaskDutyAssociation} specifying the administration to
	 *            be undertaken.
	 * @param adminContext
	 *            {@link AdministratorContext}.
	 * @throws Throwable
	 *             If fails to administer the {@link ManagedObject} instances.
	 * @return {@link FunctionState} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link FunctionState} instances are required to
	 *         load {@link ManagedObject} instances.
	 */
	FunctionState administerManagedObjects(TaskDutyAssociation<?> duty, AdministratorContext adminContext) throws Throwable;

	/**
	 * Obtains the Object of the particular {@link ManagedObject}.
	 * 
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} identifying the
	 *            {@link ManagedObject}.
	 * @return Object of the particular {@link ManagedObject}.
	 */
	Object getObject(ManagedObjectIndex managedObjectIndex);

	/**
	 * Unloads the {@link Work}.
	 * 
	 * @return Optional {@link FunctionState} to unload the {@link Work}.
	 */
	FunctionState unloadWork();

}