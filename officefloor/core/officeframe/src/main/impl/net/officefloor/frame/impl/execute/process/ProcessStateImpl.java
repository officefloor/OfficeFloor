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

package net.officefloor.frame.impl.execute.process;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectCleanupImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.profile.ProcessProfilerImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessStateImpl implements ProcessState {

	/**
	 * {@link ProcessIdentifier} for this {@link ProcessState}.
	 */
	private final ProcessIdentifier processIdentifier;

	/**
	 * {@link ProcessManager} for this {@link ProcessState}.
	 */
	private final ProcessManager processManager = new ProcessManager() {

		@Override
		public void cancel() {

			// Ensure managed function sync to main thread picks up cancelled
			synchronized (ProcessStateImpl.this.mainThreadState) {
				ProcessStateImpl.this.isCancelled = true;
			}
		}
	};

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
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor;

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
	 * {@link OfficeManager} for this {@link ProcessState}.
	 */
	private final OfficeManager officeManager;

	/**
	 * {@link Executor} for this {@link ProcessState}.
	 */
	private final Executor executor;

	/**
	 * Main {@link ThreadState} {@link FlowCompletion}. Already created as
	 * {@link FunctionState} containing the possible {@link ThreadState}
	 * {@link Escalation}.
	 */
	private FunctionState mainThreadCompletion = null;

	/**
	 * Indicates if this {@link ProcessState} has been cancelled.
	 */
	private boolean isCancelled = false;

	/**
	 * Initiate.
	 * 
	 * @param processMetaData          {@link ProcessMetaData} for this
	 *                                 {@link ProcessState}.
	 * @param officeMetaData           {@link OfficeMetaData}.
	 * @param callback                 Optional {@link FlowCallback}. May be
	 *                                 <code>null</code>.
	 * @param callbackThreadState      Optional {@link FlowCallback}
	 *                                 {@link ThreadState}. May be
	 *                                 <code>null</code>.
	 * @param threadLocalAwareExecutor {@link ThreadLocalAwareExecutor}.
	 * @param profiler                 Optional {@link Profiler}. May be
	 *                                 <code>null</code>.
	 * @param initialSetup             Initial setup with {@link ProcessState}
	 *                                 before the {@link ProcessState} initialises.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, OfficeMetaData officeMetaData, FlowCallback callback,
			ThreadState callbackThreadState, ThreadLocalAwareExecutor threadLocalAwareExecutor, Profiler profiler,
			Consumer<ProcessState> initialSetup) {
		this(processMetaData, officeMetaData, callback, callbackThreadState, threadLocalAwareExecutor, profiler, null,
				null, -1, initialSetup);
	}

	/**
	 * Initiate for a {@link ProcessState} initiated by a {@link ManagedObject}.
	 * 
	 * @param processMetaData            {@link ProcessMetaData} for this
	 *                                   {@link ProcessState}.
	 * @param officeMetaData             {@link OfficeMetaData}.
	 * @param callback                   Optional {@link FlowCallback}. May be
	 *                                   <code>null</code>.
	 * @param callbackThreadState        Optional {@link FlowCallback}
	 *                                   {@link ThreadState}. May be
	 *                                   <code>null</code>.
	 * @param threadLocalAwareExecutor   {@link ThreadLocalAwareExecutor}.
	 * @param profiler                   Optional {@link Profiler}. May be
	 *                                   <code>null</code>.
	 * @param inputManagedObject         {@link ManagedObject} that invoked this
	 *                                   {@link ProcessState}. May be
	 *                                   <code>null</code>.
	 * @param inputManagedObjectMetaData {@link ManagedObjectMetaData} of the input
	 *                                   {@link ManagedObject}. Should the input
	 *                                   {@link ManagedObject} be provided this must
	 *                                   be also provided.
	 * @param inputManagedObjectIndex    Index of the input {@link ManagedObject}
	 *                                   within this {@link ProcessState}.
	 * @param initialSetup               Initial setup with {@link ProcessState}
	 *                                   before the {@link ProcessState}
	 *                                   initialises.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, OfficeMetaData officeMetaData, FlowCallback callback,
			ThreadState callbackThreadState, ThreadLocalAwareExecutor threadLocalAwareExecutor, Profiler profiler,
			ManagedObject inputManagedObject, ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int inputManagedObjectIndex, Consumer<ProcessState> initialSetup) {

		// Undertake possible set up hook
		if (initialSetup != null) {
			initialSetup.accept(this);
		}

		// Initiate state
		this.processMetaData = processMetaData;
		this.officeMetaData = officeMetaData;
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;

		// Create the process identifier
		this.processIdentifier = this.officeMetaData.createProcessIdentifier(this);

		// Obtain the Office Manager for this process state
		// (Must be setup before managed objects, as require latches)
		this.officeManager = this.officeMetaData.getOfficeManager(this.processIdentifier);

		// Obtain the executor
		this.executor = this.officeMetaData.getExecutor(this.processIdentifier);

		// Create the process profiler (if profiling)
		this.processProfiler = (profiler == null ? null
				: new ProcessProfilerImpl(profiler, this, System.currentTimeMillis(), System.nanoTime()));

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
	public ProcessIdentifier getProcessIdentifier() {
		return this.processIdentifier;
	}

	@Override
	public ProcessManager getProcessManager() {
		return this.processManager;
	}

	@Override
	public OfficeManager getOfficeManager() {
		return this.officeManager;
	}

	@Override
	public Executor getExecutor() {
		return this.executor;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
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
			FlowCompletion completion, boolean isEscalationHandlingThreadState) {
		return new ProcessOperation() {

			@Override
			public String toString() {
				return "Spawn ThreadState for ProcessState " + Integer.toHexString(ProcessStateImpl.this.hashCode());
			}

			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to process state
				ProcessStateImpl process = ProcessStateImpl.this;

				// Create the spawned thread state
				ThreadState threadState = new ThreadStateImpl(process.processMetaData.getThreadMetaData(), completion,
						isEscalationHandlingThreadState, process, process.processProfiler);

				// Register as active thread
				process.activeThreads.addEntry(threadState);

				// Create the function for spawned thread state
				Flow flow = threadState.createFlow(null, null);
				FunctionState function = flow.createManagedFunction(parameter, managedFunctionMetaData, true, null);

				// Ensure register profiling
				FunctionState registerThreadProfiler = threadState.registerThreadProfiler();
				function = Promise.then(registerThreadProfiler, function);

				// Spawn the thread state
				FunctionLoop loop = process.officeMetaData.getFunctionLoop();
				
				// Execute on another thread (to avoid stack overflow)
				FunctionState finalFunction = function;
				process.getExecutor().execute(() -> loop.delegateFunction(finalFunction));

				// Thread state spawned
				return null;
			}
		};
	}

	@Override
	public FunctionState threadComplete(ThreadState thread, FunctionState threadCompletion) {

		// Delay main thread completion until process completes
		if (thread == this.mainThreadState) {
			// Main thread completion becomes process completion
			this.mainThreadCompletion = threadCompletion;
			threadCompletion = null; // undertaken on process completion
		}

		// Handle thread completion
		return Promise.then(threadCompletion, new ProcessOperation() {

			@Override
			public String toString() {
				return "ThreadState " + Integer.toHexString(thread.hashCode()) + " complete";
			}

			@Override
			public boolean isRequireThreadStateSafety() {
				// Only requires thread safety if not main thread
				return (thread != ProcessStateImpl.this.mainThreadState);
			}

			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

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
						public String toString() {
							return "ProcessState " + Integer.toHexString(ProcessStateImpl.this.hashCode())
									+ " complete";
						}

						@Override
						public FunctionState execute(FunctionStateContext context) throws Throwable {

							// Create the process complete operation
							FunctionState complete = new ProcessCompleteOperation();

							// Invoke process callback (if specified)
							// (Thread local aware executor to be woken up after process callback completes)
							if (process.mainThreadCompletion != null) {
								complete = Promise.then(process.mainThreadCompletion, complete);
							}

							// Complete the process
							return complete;
						}
					});
				}

				// No further processing to complete thread
				return null;
			}
		});
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjects[index];
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.officeMetaData.getFunctionLoop();
	}

	/**
	 * {@link ProcessState} operation.
	 */
	private abstract class ProcessOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			// All process operations done on main thread state
			return ProcessStateImpl.this.mainThreadState;
		}
	}

	/**
	 * Completes the {@link ProcessState}.
	 */
	private class ProcessCompleteOperation extends ProcessOperation {

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {

			// Easy access to process state
			final ProcessStateImpl process = ProcessStateImpl.this;

			// Notify process context complete
			if (process.threadLocalAwareExecutor != null) {
				process.threadLocalAwareExecutor.processComplete(process);
			}

			// Flag to profile that process complete
			if (process.processProfiler != null) {
				process.processProfiler.processStateCompleted();
			}

			// Notify executive that process complete
			Executive executive = process.officeMetaData.getExecutive();
			executive.processComplete(process.processIdentifier);

			// Nothing further, as process complete
			return null;
		}
	}

}