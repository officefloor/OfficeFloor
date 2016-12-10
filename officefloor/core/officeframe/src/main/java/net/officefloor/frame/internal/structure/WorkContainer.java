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
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Container managing the {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkContainer<W extends Work> {

	/**
	 * Obtains the {@link Work} being managed.
	 * 
	 * @param threadState
	 *            {@link ThreadState} requiring the {@link Work}.
	 * @return {@link Work} being managed.
	 */
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
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} loading the
	 *            {@link ManagedObject} instances.
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context);

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
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void governManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context);

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
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void coordinateManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context);

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
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if the {@link ManagedObject} is ready for use,
	 *         otherwise <code>false</code> indicating that waiting on the
	 *         {@link ManagedObject}.
	 */
	boolean isManagedObjectsReady(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context);

	/**
	 * Administers the {@link ManagedObject} instances as per the input
	 * {@link TaskDutyAssociation}.
	 * 
	 * @param duty
	 *            {@link TaskDutyAssociation} specifying the administration to
	 *            be undertaken.
	 * @param adminContext
	 *            {@link AdministratorContext}.
	 * @param containerContext
	 *            {@link ContainerContext}.
	 * @throws Throwable
	 *             If fails to administer the {@link ManagedObject} instances.
	 */
	void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext, ContainerContext containerContext)
			throws Throwable;

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
	Object getObject(ManagedObjectIndex managedObjectIndex,
			ThreadState threadState);

	/**
	 * Unloads the {@link Work}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} unloading
	 *            the {@link Work}.
	 */
	void unloadWork(JobNodeActivateSet activateSet, TeamIdentifier currentTeam);

}