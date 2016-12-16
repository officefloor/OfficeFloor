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
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

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
	 * Triggers for the particular {@link ManagedObject} instances to be loaded.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances to be loaded.
	 * @param jobContext
	 *            Context for executing the {@link JobNode}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} instances
	 *            to be loaded.
	 * @return {@link JobNode} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link JobNode} instances are required to
	 *         load {@link ManagedObject} instances.
	 */
	JobNode loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext, JobNode jobNode);

	/**
	 * Sets up the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances to be loaded.
	 * @param jobContext
	 *            Context for executing the {@link JobNode}.
	 * @param flow
	 *            {@link Flow}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} instances
	 *            to be loaded.
	 * @return Optional {@link JobNode} to setup the {@link ManagedObject}
	 *         instances.
	 */
	JobNode setupManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext, Flow flow,
			JobNode jobNode);

	/**
	 * Governs the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances for {@link Governance}.
	 * @param jobContext
	 *            Context for executing the {@link JobNode}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} instances
	 *            for {@link Governance}.
	 * @return {@link JobNode} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link JobNode} instances are required to
	 *         govern the {@link ManagedObject} instances.
	 */
	@Deprecated
	JobNode governManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext, JobNode jobNode);

	/**
	 * Coordinates the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances to be coordinated.
	 * @param jobContext
	 *            Context for executing the {@link JobNode}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} instances
	 *            to be coordinated.
	 * @return {@link JobNode} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link JobNode} instances are required to
	 *         co-ordinate the {@link ManagedObject} instances.
	 */
	@Deprecated
	JobNode coordinateManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext, JobNode jobNode);

	/**
	 * Indicates if the particular {@link ManagedObject} is ready for use. In
	 * other words it has finished any asynchronous operations and is ready for
	 * further use.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances to check if ready.
	 * @param jobContext
	 *            Context for executing the {@link JobNode}.
	 * @param jobNode
	 *            {@link JobNode} requiring the {@link ManagedObject} to be
	 *            ready.
	 * @return <code>true</code> if the {@link ManagedObject} is ready for use,
	 *         otherwise <code>false</code> indicating that waiting on the
	 *         {@link ManagedObject}.
	 */
	@Deprecated
	boolean isManagedObjectsReady(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext, JobNode jobNode);

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
	 * @return {@link JobNode} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link JobNode} instances are required to
	 *         load {@link ManagedObject} instances.
	 */
	JobNode administerManagedObjects(TaskDutyAssociation<?> duty, AdministratorContext adminContext) throws Throwable;

	/**
	 * Obtains the Object of the particular {@link ManagedObject}.
	 * 
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} identifying the
	 *            {@link ManagedObject}.
	 * @param threadState
	 *            {@link ThreadState}.
	 * @return Object of the particular {@link ManagedObject}.
	 */
	Object getObject(ManagedObjectIndex managedObjectIndex, ThreadState threadState);

	/**
	 * Unloads the {@link Work}.
	 * 
	 * @param continueJobNode
	 *            {@link JobNode} to continue once {@link Work} is unloaded.
	 * @return {@link JobNode} for next {@link Job}. May be <code>null</code> to
	 *         indicate no further {@link JobNode} instances are required to
	 *         unload the {@link ManagedObject} instances.
	 */
	JobNode unloadWork(JobNode continueJobNode);

}