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
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.source.ProcessContextListener;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectCleanupImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessStateImpl implements ProcessState {

	/**
	 * Identifier for this {@link ProcessState}.
	 */
	private final Object processIdentifier = new Object();

	/**
	 * Active {@link ThreadState} instances for this {@link ProcessState}.
	 */
	private final LinkedListSet<ThreadState, ProcessState> activeThreads = new StrictLinkedListSet<ThreadState, ProcessState>() {
		@Override
		protected ProcessState getOwner() {
			return ProcessStateImpl.this;
		}
	};

	/**
	 * {@link ManagedObjectCleanup}.
	 */
	private final ManagedObjectCleanup cleanup;

	/**
	 * {@link ProcessMetaData}.
	 */
	private final ProcessMetaData processMetaData;

	/**
	 * {@link ProcessContextListener} instances.
	 */
	private final ProcessContextListener[] processContextListeners;

	/**
	 * Main {@link ThreadState} for this {@link ProcessState}.
	 */
	private final ThreadState mainThreadState;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for the {@link ProcessState}.
	 */
	private final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link ProcessProfiler}.
	 */
	private final ProcessProfiler processProfiler;

	/**
	 * Initiate.
	 * 
	 * @param processMetaData
	 *            {@link ProcessMetaData} for this {@link ProcessState}.
	 * @param processContextListeners
	 *            {@link ProcessContextListener} instances.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param callback
	 *            Optional {@link FlowCallback}. May be <code>null</code>.
	 * @param callbackThreadState
	 *            Optional {@link FlowCallback} {@link ThreadState}. May be
	 *            <code>null</code>.
	 * @param processProfiler
	 *            Optional {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData, FlowCallback callback, ThreadState callbackThreadState,
			ProcessProfiler processProfiler) {
		this(processMetaData, processContextListeners, officeMetaData, callback, callbackThreadState, processProfiler,
				null, null, -1);
	}

	/**
	 * Initiate for a {@link ProcessState} initiated by a {@link ManagedObject}.
	 * 
	 * @param processMetaData
	 *            {@link ProcessMetaData} for this {@link ProcessState}.
	 * @param processContextListeners
	 *            {@link ProcessContextListener} instances.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param callback
	 *            Optional {@link FlowCallback}. May be <code>null</code>.
	 * @param callbackThreadState
	 *            Optional {@link FlowCallback} {@link ThreadState}. May be
	 *            <code>null</code>.
	 * @param processProfiler
	 *            Optional {@link ProcessProfiler}. May be <code>null</code>.
	 * @param inputManagedObject
	 *            {@link ManagedObject} that invoked this {@link ProcessState}.
	 *            May be <code>null</code>.
	 * @param inputManagedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the input
	 *            {@link ManagedObject}. Should the input {@link ManagedObject}
	 *            be provided this must be also provided.
	 * @param inputManagedObjectIndex
	 *            Index of the input {@link ManagedObject} within this
	 *            {@link ProcessState}.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData, FlowCallback callback, ThreadState callbackThreadState,
			ProcessProfiler processProfiler, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData, int inputManagedObjectIndex) {
		this.processMetaData = processMetaData;
		this.processContextListeners = processContextListeners;
		this.officeMetaData = officeMetaData;
		this.processProfiler = processProfiler;

		// Create the main thread state
		this.mainThreadState = new ThreadStateImpl(this.processMetaData.getThreadMetaData(), callback,
				callbackThreadState, this, this.processProfiler);
		this.activeThreads.addEntry(this.mainThreadState);

		// Create all managed object containers (final for thread safety)
		ManagedObjectMetaData<?>[] managedObjectMetaData = this.processMetaData.getManagedObjectMetaData();
		ManagedObjectContainer[] managedObjectContainers = new ManagedObjectContainer[managedObjectMetaData.length];
		for (int i = 0; i < managedObjectContainers.length; i++) {
			managedObjectContainers[i] = new ManagedObjectContainerImpl(managedObjectMetaData[i], this.mainThreadState);
		}
		if (inputManagedObject != null) {
			// Overwrite the Container for the Input Managed Object
			managedObjectContainers[inputManagedObjectIndex] = new ManagedObjectContainerImpl(inputManagedObject,
					inputManagedObjectMetaData, this.mainThreadState);
		}
		this.managedObjects = managedObjectContainers;

		// Create the clean up
		this.cleanup = new ManagedObjectCleanupImpl(this, this.officeMetaData);
	}

	/*
	 * ===================== ProcessState ===============================
	 */

	@Override
	public Object getProcessIdentifier() {
		return this.processIdentifier;
	}

	@Override
	public ThreadState getMainThreadState() {
		return this.mainThreadState;
	}

	@Override
	public ManagedObjectCleanup getManagedObjectCleanup() {
		return this.cleanup;
	}

	@Override
	public FunctionState spawnThreadState(ManagedFunctionMetaData<?, ?> managedFunctionMetaData, Object parameter,
			FlowCompletion completion) {
		return new ProcessOperation() {
			@Override
			public FunctionState execute() throws Throwable {

				// Easy access to process state
				ProcessStateImpl process = ProcessStateImpl.this;

				// Create the spawned thread state
				ThreadState threadState = new ThreadStateImpl(process.processMetaData.getThreadMetaData(), completion,
						process, process.processProfiler);

				// Register as active thread
				process.activeThreads.addEntry(threadState);

				// Create the function for spawned thread state
				Flow flow = threadState.createFlow(null);
				ManagedFunctionContainer function = flow.createManagedFunction(parameter, managedFunctionMetaData, true,
						null);

				// Spawn the thread state
				FunctionLoop loop = process.officeMetaData.getFunctionLoop();
				loop.delegateFunction(function);

				// Thread state spawned
				return null;
			}
		};
	}

	@Override
	public FunctionState threadComplete(ThreadState thread) {
		return new ProcessOperation() {
			@Override
			public FunctionState execute() throws Throwable {

				// Easy access to process state
				final ProcessStateImpl process = ProcessStateImpl.this;

				// Remove thread from active thread listing
				if (process.activeThreads.removeEntry(thread)) {

					// Clean up process
					FunctionState cleanUpFunctions = null;

					// Unload managed objects
					for (int i = 0; i < process.managedObjects.length; i++) {
						ManagedObjectContainer container = process.managedObjects[i];
						cleanUpFunctions = Promise.then(cleanUpFunctions, container.unloadManagedObject());
					}

					// Clean up process state
					return Promise.then(cleanUpFunctions, new ProcessOperation() {
						@Override
						public FunctionState execute() throws Throwable {

							// Notify process context complete
							for (ProcessContextListener listener : process.processContextListeners) {
								listener.processCompleted(process.processIdentifier);
							}

							// Flag to profile that process complete
							if (process.processProfiler != null) {
								process.processProfiler.processCompleted();
							}

							// Nothing further for process
							return null;
						}
					});
				}

				// No further processing to complete thread
				return null;
			}
		};
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjects[index];
	}

	/**
	 * {@link ProcessState} operation.
	 */
	private abstract class ProcessOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return ProcessStateImpl.this.mainThreadState;
		}
	}

}