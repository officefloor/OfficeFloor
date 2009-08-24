/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

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
	 */
	void loadManagedObject(JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet);

	/**
	 * Allows this {@link ManagedObject} to coordinate with the other
	 * {@link ManagedObject} instances. Also handles completion of loading the
	 * {@link ManagedObject} and obtaining the {@link Object}.
	 *
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
	 * @return <code>true</code> if the {@link ManagedObject} was coordinated.
	 *         <code>false</code> indicates this method must be called again for
	 *         the {@link ManagedObject} to be coordinated.
	 */
	<W extends Work> boolean coordinateManagedObject(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet);

	/**
	 * Indicates if the {@link ManagedObject} is ready. This is to ensure the
	 * {@link ManagedObject} is not currently involved within an asynchronous
	 * operation (in other words the {@link AsynchronousManagedObject} completed
	 * execution and ready for another operation).
	 *
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requiring the {@link ManagedObject} to be
	 *            ready.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @return <code>true</code> if the {@link ManagedObject} is ready,
	 *         otherwise <code>false</code> indicating that waiting on a
	 *         {@link ManagedObject}.
	 */
	boolean isManagedObjectReady(JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet);

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
	 * @param extractor
	 *            {@link ExtensionInterfaceExtractor} to extract the extension
	 *            interface from the {@link ManagedObject}.
	 * @return Extracted extension interface.
	 */
	<I extends Object> I extractExtensionInterface(
			ExtensionInterfaceExtractor<I> extractor);

	/**
	 * Unloads the {@link ManagedObject}.
	 *
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 */
	void unloadManagedObject(JobNodeActivateSet activateSet);

}