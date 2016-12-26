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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectCleanupImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowCallbackFactory;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
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
	 * {@link EscalationHandler} provided by the invocation of this
	 * {@link ProcessState}. May be <code>null</code>.
	 */
	private final EscalationHandlerEscalation invocationEscalation;

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation;

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
	 * @param officeFloorEscalation
	 *            {@link OfficeFloor} {@link EscalationFlow}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}.
	 * @param invocationEscalationHandler
	 *            {@link EscalationHandler} provided by the invocation of this
	 *            {@link ProcessState}.
	 * @param escalationResponsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for
	 *            handling {@link Escalation}.
	 * @param escalationHandlerRequiredGovernance
	 *            {@link EscalationHandler} required {@link Governance}.
	 * @param completionListeners
	 *            {@link ProcessCompletionListener} instances.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData, EscalationFlow officeFloorEscalation, ProcessProfiler processProfiler,
			EscalationHandler invocationEscalationHandler, TeamManagement escalationResponsibleTeam,
			boolean[] escalationHandlerRequiredGovernance, ProcessCompletionListener[] completionListeners) {
		this(processMetaData, processContextListeners, officeMetaData, officeFloorEscalation, processProfiler, null,
				null, -1, invocationEscalationHandler, escalationResponsibleTeam, escalationHandlerRequiredGovernance,
				completionListeners);
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
	 * @param invocationEscalationHandler
	 *            {@link EscalationHandler} provided by the invocation of this
	 *            {@link ProcessState}.
	 * @param escalationResponsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for
	 *            handling {@link Escalation}.
	 * @param escalationHandlerRequiredGovernance
	 *            {@link EscalationHandler} required {@link Governance}.
	 * @param completionListeners
	 *            {@link ProcessCompletionListener} listeners.
	 */
	public ProcessStateImpl(ProcessMetaData processMetaData, ProcessContextListener[] processContextListeners,
			OfficeMetaData officeMetaData, EscalationFlow officeFloorEscalation, ProcessProfiler processProfiler,
			ManagedObject inputManagedObject, ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int inputManagedObjectIndex, EscalationHandler invocationEscalationHandler,
			TeamManagement escalationResponsibleTeam, boolean[] escalationHandlerRequiredGovernance,
			ProcessCompletionListener[] completionListeners) {
		this.processMetaData = processMetaData;
		this.processContextListeners = processContextListeners;
		this.officeMetaData = officeMetaData;
		this.officeFloorEscalation = officeFloorEscalation;
		this.processProfiler = processProfiler;
		this.completionListeners = completionListeners;

		// Create the main thread state
		AssetManager mainThreadAssetManager = this.processMetaData.getMainThreadAssetManager();
		this.mainThreadState = new ThreadStateImpl(this.processMetaData.getThreadMetaData(), mainThreadAssetManager,
				null, this, this.processProfiler);

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
		this.managedObjectContainers = managedObjectContainers;

		// Create all admin containers (final for thread safety)
		AdministratorMetaData<?, ?>[] administratorMetaData = this.processMetaData.getAdministratorMetaData();
		AdministratorContainer<?, ?>[] administratorContainers = new AdministratorContainer[administratorMetaData.length];
		for (int i = 0; i < administratorContainers.length; i++) {
			administratorContainers[i] = administratorMetaData[i].createAdministratorContainer(this.mainThreadState);
		}
		this.administratorContainers = administratorContainers;

		// Escalation handled by invocation of this process
		this.invocationEscalation = (invocationEscalationHandler == null ? null
				: new EscalationHandlerEscalation(invocationEscalationHandler, escalationResponsibleTeam,
						escalationHandlerRequiredGovernance, officeMetaData.getFunctionLoop()));

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
	public ProcessMetaData getProcessMetaData() {
		return this.processMetaData;
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
	public ManagedFunctionMetaData<?, ?, ?> getTaskMetaData(String workName, String taskName)
			throws UnknownWorkException, UnknownFunctionException {

		// Look for work
		for (WorkMetaData<?> work : this.officeMetaData.getWorkMetaData()) {
			if (work.getWorkName().equals(workName)) {

				// Found work, look for task
				for (ManagedFunctionMetaData<?, ?, ?> task : work.getTaskMetaData()) {
					if (task.getFunctionName().equals(taskName)) {
						// Found the task
						return task;
					}
				}

				// Task not found on matching work
				throw new UnknownFunctionException(taskName);
			}
		}

		// Work not found
		throw new UnknownWorkException(workName);
	}

	@Override
	public ThreadState createThread(AssetManager assetManager, FlowCallbackFactory callbackFactory) {

		// Create the thread
		ThreadState threadState = new ThreadStateImpl(this.processMetaData.getThreadMetaData(), assetManager,
				callbackFactory, this, this.processProfiler);

		// Register as active thread
		this.activeThreads.addEntry(threadState);

		// Return the new thread state
		return threadState;
	}

	@Override
	public FunctionState threadComplete(ThreadState thread) {

		// Remove thread from active thread listing
		if (this.activeThreads.removeEntry(thread)) {

			// TODO run clean ups

			// Notify process complete
			for (ProcessCompletionListener listener : this.completionListeners) {
				listener.processComplete();
			}

			// Unload managed objects (some may not have been used)
			for (int i = 0; i < this.managedObjectContainers.length; i++) {
				ManagedObjectContainer container = this.managedObjectContainers[i];
				if (container != null) {
					FunctionState unloadJobNode = container.unloadManagedObject();
					if (unloadJobNode == null) {
						return unloadJobNode;
					}
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
		}

		// No further processing to complete thread
		return null;
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjectContainers[index];
	}

	@Override
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		return this.administratorContainers[index];
	}

	@Override
	public EscalationFlow getInvocationEscalation() {
		return this.invocationEscalation;
	}

	@Override
	public EscalationProcedure getOfficeEscalationProcedure() {
		return this.officeMetaData.getEscalationProcedure();
	}

	@Override
	public EscalationFlow getOfficeFloorEscalation() {
		return this.officeFloorEscalation;
	}

}