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

import java.util.List;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
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
	 * {@link ProcessFuture} for this {@link ProcessState}.
	 */
	private final ProcessFuture processFuture = new ProcessFuture() {
		@Override
		public boolean isComplete() {
			return ProcessStateImpl.this.isComplete;
		}
	};

	/**
	 * <p>
	 * Lock {@link Object} for the {@link ProcessState} to lock on.
	 * <p>
	 * As the {@link TaskContext} provides this to {@link Task} instances
	 * (application code) it is a simple {@link Object} to prevent being used to
	 * access internals of the framework.
	 */
	private final Object processLock = new Object();

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
	 * {@link ProcessMetaData}.
	 */
	private final ProcessMetaData processMetaData;

	/**
	 * {@link ProcessContextListener} instances.
	 */
	private final ProcessContextListener[] processContextListeners;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for the {@link ProcessState}.
	 */
	private final ManagedObjectContainer[] managedObjectContainers;

	/**
	 * {@link AdministratorContainer} instances for the {@link ProcessState}.
	 */
	private final AdministratorContainer<?, ?>[] administratorContainers;

	/**
	 * {@link EscalationHandlerEscalation} containing the
	 * {@link EscalationHandler} provided by the {@link ManagedObjectSource}
	 * that invoked this {@link ProcessState}. May be <code>null</code>.
	 */
	private final EscalationHandlerEscalation managedObjectSourceEscalation;

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation;

	/**
	 * {@link ProcessProfiler}.
	 */
	private final ProcessProfiler processProfiler;

	/**
	 * Listing of {@link ProcessCompletionListener} instances.
	 */
	private final List<ProcessCompletionListener> completionListeners = new java.util.LinkedList<ProcessCompletionListener>();

	/**
	 * <p>
	 * Completion flag indicating when this {@link FlowFuture} is complete.
	 * <p>
	 * <code>volatile</code> to enable inter-thread visibility.
	 */
	private volatile boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param processMetaData
	 *            {@link ProcessMetaData} for this {@link ProcessState}.
	 * @param processContextListeners
	 *            {@link ProcessContextListener} instances.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param officeFloorEscalation
	 *            {@link OfficeFloor} {@link EscalationFlow}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData,
			ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData,
			EscalationFlow officeFloorEscalation,
			ProcessProfiler processProfiler) {
		this(processMetaData, processContextListeners, officeMetaData,
				officeFloorEscalation, processProfiler, null, null, -1, null,
				null, null, null);
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
	 * @param officeFloorEscalation
	 *            {@link OfficeFloor} {@link EscalationFlow}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}.
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
	 * @param inputManagedObjectEscalationHandler
	 *            {@link EscalationHandler} provided by the
	 *            {@link ManagedObject} that invoked this {@link ProcessState}.
	 * @param escalationResponsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for
	 *            handling {@link Escalation} from the {@link ManagedObject}.
	 * @param escalationContinueTeam
	 *            {@link Team} to enable worker ({@link Thread}) of responsible
	 *            {@link Team} to continue on to handle {@link Escalation} of
	 *            {@link ManagedObject}.
	 * @param escalationRequiredGovernance
	 *            {@link EscalationHandler} required {@link Governance}.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData,
			ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData,
			EscalationFlow officeFloorEscalation,
			ProcessProfiler processProfiler, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int inputManagedObjectIndex,
			EscalationHandler inputManagedObjectEscalationHandler,
			TeamManagement escalationResponsibleTeam,
			Team escalationContinueTeam,
			boolean[] escalationHandlerRequiredGovernance) {
		this.processMetaData = processMetaData;
		this.processContextListeners = processContextListeners;
		this.officeMetaData = officeMetaData;
		this.officeFloorEscalation = officeFloorEscalation;
		this.processProfiler = processProfiler;

		// Create array to reference the managed objects
		ManagedObjectMetaData<?>[] managedObjectMetaData = this.processMetaData
				.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[managedObjectMetaData.length];

		// Load the Container for the Input Managed Object if provided
		if (inputManagedObject != null) {
			this.managedObjectContainers[inputManagedObjectIndex] = new ManagedObjectContainerImpl(
					inputManagedObject, inputManagedObjectMetaData, this);
		}

		// Create array to reference the administrators
		AdministratorMetaData<?, ?>[] administratorMetaData = this.processMetaData
				.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[administratorMetaData.length];

		// Escalation handled by managed object source
		this.managedObjectSourceEscalation = (inputManagedObjectEscalationHandler == null ? null
				: new EscalationHandlerEscalation(
						inputManagedObjectEscalationHandler,
						escalationResponsibleTeam, escalationContinueTeam,
						escalationHandlerRequiredGovernance));
	}

	/*
	 * ===================== ProcessState ===============================
	 */

	@Override
	public Object getProcessIdentifier() {
		return this.processIdentifier;
	}

	@Override
	public ProcessFuture getProcessFuture() {
		return this.processFuture;
	}

	@Override
	public Object getProcessLock() {
		return this.processLock;
	}

	@Override
	public ProcessMetaData getProcessMetaData() {
		return this.processMetaData;
	}

	@Override
	public TaskMetaData<?, ?, ?> getTaskMetaData(String workName,
			String taskName) throws UnknownWorkException, UnknownTaskException {

		// Look for work
		for (WorkMetaData<?> work : this.officeMetaData.getWorkMetaData()) {
			if (work.getWorkName().equals(workName)) {

				// Found work, look for task
				for (TaskMetaData<?, ?, ?> task : work.getTaskMetaData()) {
					if (task.getTaskName().equals(taskName)) {
						// Found the task
						return task;
					}
				}

				// Task not found on matching work
				throw new UnknownTaskException(taskName);
			}
		}

		// Work not found
		throw new UnknownWorkException(workName);
	}

	@Override
	public JobSequence createThread(AssetManager assetManager) {

		// Create the thread
		ThreadState threadState = new ThreadStateImpl(
				this.processMetaData.getThreadMetaData(), this, assetManager,
				this.processProfiler);

		// Register as active thread
		synchronized (this.getProcessLock()) {
			this.activeThreads.addEntry(threadState);
		}

		// Return the Job Sequence for the new thread
		return threadState.createJobSequence();
	}

	@Override
	public void threadComplete(ThreadState thread,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam) {

		// Removing completed thread so must obtain process lock
		boolean isProcessComplete = false;
		synchronized (this.getProcessLock()) {

			// Remove thread from active thread listing
			if (this.activeThreads.removeEntry(thread)) {

				// Notify process complete
				for (ProcessCompletionListener listener : this.completionListeners) {
					listener.processComplete();
				}

				// Unload managed objects (some may not have been used)
				for (int i = 0; i < this.managedObjectContainers.length; i++) {
					ManagedObjectContainer container = this.managedObjectContainers[i];
					if (container != null) {
						container.unloadManagedObject(activateSet, currentTeam);
					}
				}

				// Notify process context complete
				for (ProcessContextListener listener : this.processContextListeners) {
					listener.processCompleted(this.processIdentifier);
				}

				// Flag to profile that process complete
				if (this.processProfiler != null) {
					this.processProfiler.processCompleted();
				}

				// Flag the process now complete
				this.isComplete = true;
				isProcessComplete = true;
			}
		}

		// Notify if process is complete
		if (isProcessComplete) {
			synchronized (this.processFuture) {
				this.processFuture.notifyAll();
			}
		}
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		// Lazy load the Managed Object Container
		// (This should be thread safe as should always be called within the
		// Process lock of the Thread before the Job uses it).
		ManagedObjectContainer container = this.managedObjectContainers[index];
		if (container == null) {
			container = this.processMetaData.getManagedObjectMetaData()[index]
					.createManagedObjectContainer(this);
			this.managedObjectContainers[index] = container;
		}
		return container;
	}

	@Override
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		// Lazy load the Administrator Container
		// (This should be thread safe as should always called within the
		// Process lock by the WorkContainer)
		AdministratorContainer<?, ?> container = this.administratorContainers[index];
		if (container == null) {
			container = this.processMetaData.getAdministratorMetaData()[index]
					.createAdministratorContainer();
			this.administratorContainers[index] = container;
		}
		return container;
	}

	@Override
	public EscalationFlow getManagedObjectSourceEscalation() {
		return this.managedObjectSourceEscalation;
	}

	@Override
	public EscalationProcedure getOfficeEscalationProcedure() {
		return this.officeMetaData.getEscalationProcedure();
	}

	@Override
	public EscalationFlow getOfficeFloorEscalation() {
		return this.officeFloorEscalation;
	}

	@Override
	public void registerProcessCompletionListener(
			ProcessCompletionListener listener) {
		synchronized (this.getProcessLock()) {
			this.completionListeners.add(listener);
		}
	}

}