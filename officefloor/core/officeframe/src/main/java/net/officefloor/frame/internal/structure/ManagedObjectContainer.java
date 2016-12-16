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
	 * @return <code>true</code> indicating the {@link ManagedObject} was
	 *         loaded. <code>false</code> indicates must wait asynchronously for
	 *         the {@link ManagedObject} to be loaded.
	 */
	boolean loadManagedObject(JobContext jobContext, JobNode jobNode);

	/**
	 * Sets up the {@link ManagedObject} for use.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param flow
	 *            {@link Flow} containing the {@link JobNode}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} to be
	 *            setup.
	 * @return Optional {@link JobNode} to setup the {@link ManagedObject}.
	 */
	JobNode setupManagedObject(WorkContainer<?> workContainer, JobContext jobContext, Flow flow, JobNode jobNode);

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
	 * @return Optional {@link JobNode} to undertake to ready the
	 *         {@link ManagedObject}. Returning <code>null</code> indicates the
	 *         {@link ManagedObject} is ready.
	 */
	<W extends Work> JobNode isManagedObjectReady(WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode);

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
	<I extends Object> I extractExtensionInterface(ExtensionInterfaceExtractor<I> extractor);

	/**
	 * Unregisters this {@link ManagedObject} from {@link Governance}.
	 * 
	 * @param governance
	 *            {@link ActiveGovernance}.
	 * @return Optional {@link JobNode} to unregister the {@link ManagedObject}
	 *         from {@link Governance}.
	 */
	JobNode unregisterManagedObjectFromGovernance(ActiveGovernance<?, ?> governance);

	/**
	 * Unloads the {@link ManagedObject}.
	 * 
	 * @param continueJobNode
	 *            {@link JobNode} to continue unloading of the
	 *            {@link ManagedObject}.
	 * @return Optional {@link JobNode} to unload the {@link ManagedObject}.
	 */
	JobNode unloadManagedObject(JobNode continueJobNode);

}