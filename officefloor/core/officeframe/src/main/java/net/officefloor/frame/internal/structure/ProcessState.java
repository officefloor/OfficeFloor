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
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;

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
	 * Obtains the {@link ProcessMetaData} for this {@link ProcessState}.
	 * 
	 * @return {@link ProcessMetaData} for this {@link ProcessState}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * <p>
	 * Obtains the main {@link ThreadState} for this {@link ProcessState}.
	 * <p>
	 * The main {@link ThreadState} is used for any {@link ProcessState}
	 * mutations. This avoids the possibility of data corruption, as only one
	 * {@link ThreadState} may alter the {@link ProcessState}.
	 * 
	 * @return Main {@link ThreadState} for this {@link ProcessState}.
	 */
	ThreadState getMainThreadState();

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
	 * Creates a new {@link ThreadState} contained in this {@link ProcessState}.
	 * 
	 * @param assetManager
	 *            {@link AssetManager} for the {@link ThreadState}.
	 * @param callbackFactory
	 *            Optional {@link FlowCallbackFactory} to create a
	 *            {@link FunctionState} to be instigating on completion of the created
	 *            {@link ThreadState}.
	 * @return New {@link ThreadState} contained in this {@link ProcessState}.
	 */
	ThreadState createThread(AssetManager assetManager, FlowCallbackFactory callbackFactory);

	/**
	 * Flags that the input {@link ThreadState} has complete.
	 * 
	 * @param thread
	 *            {@link ThreadState} that has completed.
	 * @return Optional {@link FunctionState} to complete the {@link ThreadState}.
	 */
	FunctionState threadComplete(ThreadState thread);

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
	 * provided by the invocation of this {@link ProcessState}.
	 * 
	 * @return {@link EscalationFlow} or <code>null</code> if the invoker did
	 *         not provide a {@link EscalationHandler}.
	 */
	EscalationFlow getInvocationEscalation();

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
	 * Obtains the {@link ManagedObjectCleanup} for this {@link ProcessState}.
	 * 
	 * @return {@link ManagedObjectCleanup} for this {@link ProcessState}.
	 */
	ManagedObjectCleanup getManagedObjectCleanup();

}