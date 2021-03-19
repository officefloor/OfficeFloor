/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.internal.structure;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessManager;

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
	 * Obtains the {@link ProcessIdentifier} for this {@link ProcessState}.
	 * 
	 * @return {@link ProcessIdentifier} for this {@link ProcessState}.
	 */
	ProcessIdentifier getProcessIdentifier();

	/**
	 * Obtains the {@link ProcessManager} for this {@link ProcessState}.
	 * 
	 * @return {@link ProcessManager} for this {@link ProcessState}.
	 */
	ProcessManager getProcessManager();

	/**
	 * Obtains the {@link OfficeManager} for this {@link ProcessState}.
	 * 
	 * @return {@link OfficeManager} for this {@link ProcessState}.
	 */
	OfficeManager getOfficeManager();

	/**
	 * Obtains the {@link Executor} for this {@link ProcessState}.
	 * 
	 * @return {@link Executor} for this {@link ProcessState}.
	 */
	Executor getExecutor();

	/**
	 * <p>
	 * Indicates if the {@link ProcessState} has been cancelled.
	 * <p>
	 * This is only valid after synchronising with this {@link ProcessState} (in
	 * other words the main {@link ThreadState}).
	 * 
	 * @return <code>true</code> if cancelled - or indeterminate if have not
	 *         synchronised on {@link ProcessState}.
	 */
	boolean isCancelled();

	/**
	 * <p>
	 * Obtains the main {@link ThreadState} for this {@link ProcessState}.
	 * <p>
	 * The main {@link ThreadState} is used for any {@link ProcessState} mutations.
	 * This avoids the possibility of data corruption, as only one
	 * {@link ThreadState} may alter the {@link ProcessState}.
	 * 
	 * @return Main {@link ThreadState} for this {@link ProcessState}.
	 */
	ThreadState getMainThreadState();

	/**
	 * Spawns a new {@link ThreadState} contained in this {@link ProcessState}.
	 * 
	 * @param managedFunctionMetaData         {@link ManagedFunctionMetaData} of the
	 *                                        initial {@link ManagedFunction} within
	 *                                        the spawned {@link ThreadState}.
	 * @param parameter                       Parameter for the initial
	 *                                        {@link ManagedFunction}.
	 * @param completion                      Optional {@link FlowCompletion} to be
	 *                                        notified of completion of the spawned
	 *                                        {@link ThreadState}.
	 * @param isEscalationHandlingThreadState Indicates whether the
	 *                                        {@link ThreadState} is for
	 *                                        {@link Escalation} handling.
	 * @return {@link FunctionState} to spawn the {@link ThreadState}.
	 */
	FunctionState spawnThreadState(ManagedFunctionMetaData<?, ?> managedFunctionMetaData, Object parameter,
			FlowCompletion completion, boolean isEscalationHandlingThreadState);

	/**
	 * Flags that the input {@link ThreadState} has complete.
	 * 
	 * @param thread           {@link ThreadState} that has completed.
	 * @param threadCompletion Optional {@link FunctionState} for the completion of
	 *                         the{@link ThreadState}. May be <code>null</code>.
	 * @return {@link FunctionState} to complete the {@link ThreadState}.
	 */
	FunctionState threadComplete(ThreadState thread, FunctionState threadCompletion);

	/**
	 * Obtains the {@link FunctionLoop} for the {@link ProcessState}.
	 * 
	 * @return {@link FunctionLoop} for the {@link ProcessState}.
	 */
	FunctionLoop getFunctionLoop();

	/**
	 * Obtains the {@link ManagedObjectContainer} for the input index.
	 * 
	 * @param index Index of the {@link ManagedObjectContainer} to be returned.
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
