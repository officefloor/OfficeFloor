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

import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.impl.execute.flow.FlowImpl;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectCleanupImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.source.ProcessContextListener;

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
	 * {@link ProcessCompletionListener} instances.
	 */
	private final ProcessCompletionListener[] completionListeners;

	/**
	 * Main {@link ThreadState} for this {@link ProcessState}.
	 */
	private final ThreadState mainThreadState;

	/**
	 * {@link Flow} for {@link ProcessOperation} instances.
	 */
	private final Flow processOperationsFlow;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for the {@link ProcessState}.
	 */
	private final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link AdministratorContainer} instances for the {@link ProcessState}.
	 */
	private final AdministratorContainer<?>[] administrators;

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
	 * @param completion
	 *            Optional {@link FlowCompletion}. May be <code>null</code>.
	 * @param processProfiler
	 *            Optional {@link ProcessProfiler}. May be <code>null</code>.
	 * @param completionListeners
	 *            {@link ProcessCompletionListener} instances.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData, FlowCompletion completion, ProcessProfiler processProfiler,
			ProcessCompletionListener[] completionListeners) {
		this(processMetaData, processContextListeners, officeMetaData, completion, processProfiler, completionListeners,
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
	 * @param completion
	 *            Optional {@link FlowCompletion}. May be <code>null</code>.
	 * @param processProfiler
	 *            Optional {@link ProcessProfiler}. May be <code>null</code>.
	 * @param completionListeners
	 *            {@link ProcessCompletionListener} instances.
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
			OfficeMetaData officeMetaData, FlowCompletion completion, ProcessProfiler processProfiler,
			ProcessCompletionListener[] completionListeners, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData, int inputManagedObjectIndex) {
		this.processMetaData = processMetaData;
		this.processContextListeners = processContextListeners;
		this.officeMetaData = officeMetaData;
		this.processProfiler = processProfiler;
		this.completionListeners = completionListeners;

		// Create the main thread state
		AssetManager mainThreadAssetManager = this.processMetaData.getMainThreadAssetManager();
		this.mainThreadState = new ThreadStateImpl(this.processMetaData.getThreadMetaData(), mainThreadAssetManager,
				completion, this, this.processProfiler);
		this.activeThreads.addEntry(this.mainThreadState);

		// Create the processs operations flow
		// Note: should not stop the main thread from completing
		this.processOperationsFlow = new FlowImpl(null, this.mainThreadState);

		// Create all managed object containers (final for thread safety)
		ManagedObjectMetaData<?>[] managedObjectMetaData = this.processMetaData.getManagedObjectMetaData();
		ManagedObjectContainer[] managedObjectContainers = new ManagedObjectContainer[managedObjectMetaData.length];
		for (int i = 0; i < managedObjectContainers.length; i++) {
			managedObjectContainers[i] = managedObjectMetaData[i].createManagedObjectContainer(this.mainThreadState);
		}
		if (inputManagedObject != null) {
			// Overwrite the Container for the Input Managed Object
			managedObjectContainers[inputManagedObjectIndex] = new ManagedObjectContainerImpl(inputManagedObject,
					inputManagedObjectMetaData, this.mainThreadState);
		}
		this.managedObjects = managedObjectContainers;

		// Create all admin containers (final for thread safety)
		AdministratorMetaData<?, ?>[] administratorMetaData = this.processMetaData.getAdministratorMetaData();
		AdministratorContainer<?>[] administratorContainers = new AdministratorContainer[administratorMetaData.length];
		for (int i = 0; i < administratorContainers.length; i++) {
			administratorContainers[i] = administratorMetaData[i].createAdministratorContainer(this.mainThreadState);
		}
		this.administrators = administratorContainers;

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
	public ManagedFunctionMetaData<?, ?> getFunctionMetaData(String functionName) throws UnknownFunctionException {

		// Look for function
		ManagedFunctionMetaData<?, ?>[] functions = this.officeMetaData.getManagedFunctionMetaData();
		for (int i = 0; i < functions.length; i++) {
			ManagedFunctionMetaData<?, ?> function = functions[i];
			if (function.getFunctionName().equals(functionName)) {
				return function;
			}
		}

		// As here, function not found
		throw new UnknownFunctionException(functionName);
	}

	@Override
	public FunctionState spawnThreadState(ManagedFunctionMetaData<?, ?> managedFunctionMetaData, Object parameter,
			FlowCompletion completion, AssetManager flowAssetManager) {
		return new ProcessOperation() {
			@Override
			public FunctionState execute() throws Throwable {

				// Easy access to process state
				ProcessStateImpl process = ProcessStateImpl.this;

				// Create the spawned thread state
				ThreadState threadState = new ThreadStateImpl(process.processMetaData.getThreadMetaData(),
						flowAssetManager, completion, process, process.processProfiler);

				// Register as active thread
				process.activeThreads.addEntry(threadState);

				// Create the function for spawned thread state
				Flow flow = threadState.createFlow(null);
				ManagedFunctionContainer function = flow.createManagedFunction(managedFunctionMetaData, null, parameter,
						GovernanceDeactivationStrategy.ENFORCE);

				// Spawn the thread state
				process.officeMetaData.getFunctionLoop().delegateFunction(function);

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

					// Notify process complete
					for (ProcessCompletionListener listener : process.completionListeners) {
						listener.processComplete();
					}

					// Clean up process
					FunctionState cleanUpFunctions = null;

					// Unload managed objects (some may not have been used)
					for (int i = 0; i < process.managedObjects.length; i++) {
						ManagedObjectContainer container = process.managedObjects[i];
						if (container != null) {
							cleanUpFunctions = Promise.then(cleanUpFunctions, container.unloadManagedObject());
						}
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

	@Override
	public AdministratorContainer<?> getAdministratorContainer(int index) {
		return this.administrators[index];
	}

	/**
	 * {@link ProcessState} operation.
	 */
	private abstract class ProcessOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		/*
		 * ================= LinkedListSetEntry ============================
		 */

		@Override
		public Flow getLinkedListSetOwner() {
			return ProcessStateImpl.this.processOperationsFlow;
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			return null; // any team
		}

		@Override
		public Flow getFlow() {
			return ProcessStateImpl.this.processOperationsFlow;
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			// Initially all run on main thread so no need
			return false;
		}
	}

}