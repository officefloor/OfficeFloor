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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;

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
	 * Obtains the {@link ManagedFunctionMetaData} for the
	 * {@link ManagedFunction} within the {@link Office} containing this
	 * {@link ProcessState}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link ManagedFunctionMetaData} for the {@link ManagedFunction}.
	 * @throws UnknownFunctionException
	 *             If no {@link ManagedFunction} by name within the
	 *             {@link Office}.
	 */
	ManagedFunctionMetaData<?, ?> getFunctionMetaData(String functionName) throws UnknownFunctionException;

	/**
	 * Spawns a new {@link ThreadState} contained in this {@link ProcessState}.
	 * 
	 * @param managedFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the initial
	 *            {@link ManagedFunction} within the spawned
	 *            {@link ThreadState}.
	 * @param parameter
	 *            Parameter for the initial {@link ManagedFunction}.
	 * @param callback
	 *            Optional {@link FlowCompletion} to be notified of completion
	 *            of the spawned {@link ThreadState}.
	 * @return {@link FunctionState} to spawn the {@link ThreadState}.
	 */
	FunctionState spawnThreadState(ManagedFunctionMetaData<?, ?> managedFunctionMetaData, Object parameter,
			FlowCompletion completion);

	/**
	 * Flags that the input {@link ThreadState} has complete.
	 * 
	 * @param thread
	 *            {@link ThreadState} that has completed.
	 * @return {@link FunctionState} to complete the {@link ThreadState}.
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
	 * Obtains the {@link ManagedObjectCleanup} for this {@link ProcessState}.
	 * 
	 * @return {@link ManagedObjectCleanup} for this {@link ProcessState}.
	 */
	ManagedObjectCleanup getManagedObjectCleanup();

}