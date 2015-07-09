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

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * <p>
 * State of a process within the {@link Office}.
 * <p>
 * {@link ProcessState} instances can not interact with each other, much like
 * processes within an Operating System can not directly interact (e.g. share
 * process space) with each other.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessState {

	/**
	 * Obtains the identifier for this {@link ProcessState}.
	 * 
	 * @return Identifier for this {@link ProcessState}.
	 */
	Object getProcessIdentifier();

	/**
	 * Obtains the {@link ProcessFuture} for this {@link ProcessState}.
	 * 
	 * @return {@link ProcessFuture} for this {@link ProcessState}.
	 */
	ProcessFuture getProcessFuture();

	/**
	 * <p>
	 * Obtains the lock for this {@link ProcessState}.
	 * <p>
	 * This is the internal lock to the {@link OfficeFloor} engine and should
	 * not be used outside of the {@link OfficeFloor} engine.
	 * 
	 * @return Lock of this {@link ProcessState}.
	 */
	Object getProcessLock();

	/**
	 * Obtains the {@link ProcessMetaData} for this {@link ProcessState}.
	 * 
	 * @return {@link ProcessMetaData} for this {@link ProcessState}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * Obtains the {@link CleanupSequence} for this {@link ProcessState}.
	 * 
	 * @return {@link CleanupSequence} for this {@link ProcessState}.
	 */
	CleanupSequence getCleanupSequence();

	/**
	 * Obtains the {@link TaskMetaData} for the {@link Work} and {@link Task}
	 * within the {@link Office} containing this {@link ProcessState}.
	 * 
	 * @param workName
	 *            {@link Work} name containing the {@link Task}.
	 * @param taskName
	 *            {@link Task} name within the {@link Work}.
	 * @return {@link TaskMetaData}.
	 * @throws UnknownWorkException
	 *             If no {@link Work} by name within the {@link Office}.
	 * @throws UnknownTaskException
	 *             If no {@link Task} by name within the {@link Work}.
	 */
	TaskMetaData<?, ?, ?> getTaskMetaData(String workName, String taskName)
			throws UnknownWorkException, UnknownTaskException;

	/**
	 * <p>
	 * Creates a {@link JobSequence} for the new {@link ThreadState} contained
	 * in this {@link ProcessState}.
	 * <p>
	 * The new {@link ThreadState} is available from the returned
	 * {@link JobSequence}.
	 * 
	 * @param assetManager
	 *            {@link AssetManager} for the {@link ThreadState}.
	 * @return {@link JobSequence} for the new {@link ThreadState} contained in
	 *         this {@link ProcessState}.
	 */
	JobSequence createThread(AssetManager assetManager);

	/**
	 * Flags that the input {@link ThreadState} has complete.
	 * 
	 * @param thread
	 *            {@link ThreadState} that has completed.
	 * @param activeSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances
	 *            waiting on this {@link ProcessState} if all
	 *            {@link ThreadState} instances of this {@link ProcessState} are
	 *            complete. This is unlikely to be used but is available for
	 *            {@link ManagedObject} instances bound to this
	 *            {@link ProcessState} requiring unloading (rather than relying
	 *            on the {@link OfficeManager}).
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} completing
	 *            the {@link ThreadState}.
	 */
	void threadComplete(ThreadState thread, JobNodeActivateSet activeSet,
			TeamIdentifier currentTeam);

	/**
	 * Obtains the {@link ManagedObjectContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link ManagedObjectContainer} to be returned.
	 * @return {@link ManagedObjectContainer} for the index.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

	/**
	 * Obtains the {@link AdministratorContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link AdministratorContainer} to be returned.
	 * @return {@link AdministratorContainer} for the index.
	 */
	AdministratorContainer<?, ?> getAdministratorContainer(int index);

	/**
	 * Obtains the {@link EscalationFlow} for the {@link EscalationHandler}
	 * provided by the {@link ManagedObjectSource}.
	 * 
	 * @return {@link EscalationFlow} or <code>null</code> if
	 *         {@link ManagedObjectSource} did not provide a
	 *         {@link EscalationHandler} or this {@link ProcessState} was not
	 *         invoked by a {@link ManagedObjectSource}.
	 */
	EscalationFlow getManagedObjectSourceEscalation();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link Office}.
	 * 
	 * @return {@link EscalationProcedure} for the {@link Office}.
	 */
	EscalationProcedure getOfficeEscalationProcedure();

	/**
	 * Obtains the catch all {@link EscalationFlow} for the {@link OfficeFloor}.
	 * 
	 * @return Catch all {@link EscalationFlow} for the {@link OfficeFloor}.
	 */
	EscalationFlow getOfficeFloorEscalation();

	/**
	 * Registers a {@link ProcessCompletionListener} with this
	 * {@link ProcessState}.
	 * 
	 * @param listener
	 *            {@link ProcessCompletionListener}.
	 */
	void registerProcessCompletionListener(ProcessCompletionListener listener);

}