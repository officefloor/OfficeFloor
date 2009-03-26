/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.process;

import java.util.List;

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.administrator.AdministratorContainerImpl;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedList;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link ProcessState}.
 * 
 * @author Daniel
 */
public class ProcessStateImpl implements ProcessState {

	/**
	 * Active {@link ThreadState} instances for this {@link ProcessState}.
	 */
	private final LinkedList<ThreadState, JobNodeActivateSet> activeThreads = new AbstractLinkedList<ThreadState, JobNodeActivateSet>() {
		@Override
		public void lastLinkedListEntryRemoved(JobNodeActivateSet activateSet) {
			// Process complete
			synchronized (ProcessStateImpl.this.getProcessLock()) {

				// Unload managed objects
				for (int i = 0; i < ProcessStateImpl.this.managedObjectContainers.length; i++) {
					ProcessStateImpl.this.managedObjectContainers[i]
							.unloadManagedObject(activateSet);
				}

				// Notify process complete
				for (ProcessCompletionListener listener : ProcessStateImpl.this.completionListeners) {
					listener.processComplete();
				}

				// Flag the process now complete
				ProcessStateImpl.this.isComplete = true;
			}
		}
	};

	/**
	 * {@link ProcessMetaData}.
	 */
	private final ProcessMetaData processMetaData;

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
	 * {@link OfficeFloor} {@link Escalation}.
	 */
	private final Escalation officeFloorEscalation;

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
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param managedObjectEscalationHandler
	 *            {@link EscalationHandler} provided by the
	 *            {@link ManagedObject} that invoked this {@link ProcessState}.
	 * @param officeFloorEscalaion
	 *            {@link OfficeFloor} {@link Escalation}.
	 */
	@SuppressWarnings("unchecked")
	public ProcessStateImpl(ProcessMetaData processMetaData,
			OfficeMetaData officeMetaData,
			EscalationHandler managedObjectEscalationHandler,
			Escalation officeFloorEscalaion) {
		this.processMetaData = processMetaData;
		this.officeMetaData = officeMetaData;
		this.officeFloorEscalation = officeFloorEscalaion;

		// Managed Objects
		ManagedObjectMetaData<?>[] managedObjectMetaData = this.processMetaData
				.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[managedObjectMetaData.length];
		for (int i = 0; i < this.managedObjectContainers.length; i++) {
			this.managedObjectContainers[i] = new ManagedObjectContainerImpl(
					managedObjectMetaData[i], this);
		}

		// Administrators
		AdministratorMetaData<?, ?>[] administratorMetaData = this.processMetaData
				.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[administratorMetaData.length];
		for (int i = 0; i < this.administratorContainers.length; i++) {
			this.administratorContainers[i] = new AdministratorContainerImpl(
					administratorMetaData[i]);
		}

		// TODO allow configuring the team responsible for MO handling
		Team team = new PassiveTeam();

		// Escalation handled by managed object source
		this.managedObjectSourceEscalation = (managedObjectEscalationHandler == null ? null
				: new EscalationHandlerEscalation(
						managedObjectEscalationHandler, team));
	}

	/*
	 * ===================== ProcessState ===============================
	 */

	@Override
	public Object getProcessLock() {
		return this;
	}

	@Override
	public ProcessMetaData getProcessMetaData() {
		return this.processMetaData;
	}

	@Override
	public <W extends Work> Flow createThread(FlowMetaData<W> flowMetaData) {

		// Create the thread
		ThreadState threadState = new ThreadStateImpl(this.processMetaData
				.getThreadMetaData(), this.activeThreads, this, flowMetaData);

		// Register as active thread
		synchronized (this.getProcessLock()) {
			this.activeThreads.addLinkedListEntry(threadState);
		}

		// Return the flow for the new thread
		return threadState.createFlow(flowMetaData);
	}

	@Override
	public void threadComplete(ThreadState thread,
			JobNodeActivateSet activateSet) {
		// Remove thread from listing.
		// Will trigger process complete if last thread of process.
		thread.removeFromLinkedList(activateSet);
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
	public Escalation getManagedObjectSourceEscalation() {
		return this.managedObjectSourceEscalation;
	}

	@Override
	public EscalationProcedure getOfficeEscalationProcedure() {
		return this.officeMetaData.getEscalationProcedure();
	}

	@Override
	public Escalation getOfficeFloorEscalation() {
		return this.officeFloorEscalation;
	}

	@Override
	public void registerProcessCompletionListener(
			ProcessCompletionListener listener) {
		this.completionListeners.add(listener);
	}

	/*
	 * ==================== FlowFuture ======================================
	 */

	@Override
	public boolean isComplete() {
		return this.isComplete;
	}

}