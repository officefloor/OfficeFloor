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
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Container managing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContainer {

	/**
	 * Triggers loading the {@link ManagedObject}.
	 * 
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} to be
	 *            loaded.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} loading the
	 *            {@link ManagedObject}.
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void loadManagedObject(JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context);

	/**
	 * Provides any active {@link Governance} over the {@link ManagedObject}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param workContainer
	 *            {@link WorkContainer} to possibly source the
	 *            {@link Governance}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requesting {@link Governance} for the
	 *            {@link ManagedObject}.
	 * @param activateSet
	 *            {@link JobNodeActivatableSet} to add {@link JobNode} instances
	 *            to activate.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if {@link Governance} is in place for the
	 *         {@link ManagedObject} and may move onto the next
	 *         {@link ManagedObject}.
	 */
	<W extends Work> boolean governManagedObject(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context);

	/**
	 * Allows this {@link ManagedObject} to coordinate with the other
	 * {@link ManagedObject} instances. Also handles completion of loading the
	 * {@link ManagedObject} and obtaining the {@link Object}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param workContainer
	 *            {@link WorkContainer} to source the other
	 *            {@link ManagedObject} instances.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} to
	 *            coordinate.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if coordination is in place for the
	 *         {@link ManagedObject} and may move onto the next
	 *         {@link ManagedObject}.
	 */
	<W extends Work> boolean coordinateManagedObject(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context);

	/**
	 * Indicates if the {@link ManagedObject} is ready. This is to ensure the
	 * {@link ManagedObject} is not currently involved within an asynchronous
	 * operation (in other words the {@link AsynchronousManagedObject} completed
	 * execution and ready for another operation).
	 *
	 * @param <W>
	 *            {@link Work} type.
	 * @param workContainer
	 *            {@link WorkContainer} to source the other
	 *            {@link ManagedObject} instances. This may be required should
	 *            coordination be necessary to make the {@link ManagedObject}
	 *            ready.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requiring the {@link ManagedObject} to be
	 *            ready.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if the {@link ManagedObject} is ready,
	 *         otherwise <code>false</code> indicating that waiting on a
	 *         {@link ManagedObject}.
	 */
	<W extends Work> boolean isManagedObjectReady(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context);

	/**
	 * Obtains the object being managed by the {@link ManagedObject}.
	 * 
	 * @param threadState
	 *            {@link ThreadState} of thread requiring the object.
	 * @return Object being managed by the {@link ManagedObject}.
	 */
	Object getObject(ThreadState threadState);

	/**
	 * Extracts the extension interface from the {@link ManagedObject} within
	 * this {@link ManagedObjectContainer}.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param extractor
	 *            {@link ExtensionInterfaceExtractor} to extract the extension
	 *            interface from the {@link ManagedObject}.
	 * @return Extracted extension interface.
	 */
	<I extends Object> I extractExtensionInterface(
			ExtensionInterfaceExtractor<I> extractor);

	/**
	 * Unregisters this {@link ManagedObject} from {@link Governance}.
	 * 
	 * @param governance
	 *            {@link ActiveGovernance}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team}
	 *            unregistering the {@link ManagedObject} from
	 *            {@link Governance}.
	 */
	void unregisterManagedObjectFromGovernance(
			ActiveGovernance<?, ?> governance, JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam);

	/**
	 * Unloads the {@link ManagedObject}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} unloading
	 *            the {@link ManagedObject}.
	 */
	void unloadManagedObject(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam);

}