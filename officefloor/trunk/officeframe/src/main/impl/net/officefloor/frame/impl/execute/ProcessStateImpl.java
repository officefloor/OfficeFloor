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
package net.officefloor.frame.impl.execute;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.ProcessState}.
 * 
 * @author Daniel
 */
public class ProcessStateImpl implements ProcessState {

	/**
	 * {@link ManagedObjectContainer} instances for the {@link ProcessState}.
	 */
	protected final ManagedObjectContainer[] managedObjectContainers;

	/**
	 * {@link AdministratorContainer} instances for the {@link ProcessState}.
	 */
	protected final AdministratorContainer<?, ?>[] administratorContainers;

	/**
	 * Listing of {@link ProcessCompletionListener} instances.
	 */
	protected final List<ProcessCompletionListener> completionListeners = new LinkedList<ProcessCompletionListener>();

	/**
	 * Count of active {@link ThreadState} instances for this
	 * {@link ProcessState}.
	 */
	protected int activeThreadCount = 0;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            Meta-data of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances for the {@link ProcessState}.
	 * @param administratorMetaData
	 *            Meta-data of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            instances for the {@link ProcessState}.
	 */
	@SuppressWarnings("unchecked")
	public ProcessStateImpl(ManagedObjectMetaData[] managedObjectMetaData,
			AdministratorMetaData[] administratorMetaData) {
		// Managed Objects
		this.managedObjectContainers = new ManagedObjectContainer[managedObjectMetaData.length];
		for (int i = 0; i < this.managedObjectContainers.length; i++) {
			this.managedObjectContainers[i] = new ManagedObjectContainerImpl(
					managedObjectMetaData[i], this.getProcessLock());
		}

		// Administrators
		this.administratorContainers = new AdministratorContainer[administratorMetaData.length];
		for (int i = 0; i < this.administratorContainers.length; i++) {
			this.administratorContainers[i] = new AdministratorContainerImpl(
					administratorMetaData[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#getProcessLock()
	 */
	public Object getProcessLock() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#createThread()
	 */
	public <W extends Work> Flow createThread(FlowMetaData<W> flowMetaData) {
		// Create the new thread
		ThreadState threadState = new ThreadStateImpl(this, flowMetaData);

		// Increment the number of threads
		synchronized (this.getProcessLock()) {
			this.activeThreadCount++;
		}

		// Return the flow for the new thread
		return threadState.createFlow(flowMetaData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#threadComplete(net.officefloor.frame.internal.structure.ThreadState)
	 */
	public void threadComplete(ThreadState thread) {
		// Decrement the number of threads
		synchronized (this.getProcessLock()) {
			this.activeThreadCount--;

			// Determine if clean process
			if (this.activeThreadCount == 0) {
				// No further threads therefore unload managed objects
				for (int i = 0; i < this.managedObjectContainers.length; i++) {

					// Unload the managed objects
					this.managedObjectContainers[i].unloadManagedObject();

					// Notify process complete
					for (ProcessCompletionListener listener : this.completionListeners) {
						listener.processComplete();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#getManagedObjectContainer(int)
	 */
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjectContainers[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#getAdministratorContainer(int)
	 */
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		return this.administratorContainers[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ProcessState#registerProcessCompletionListener(net.officefloor.frame.internal.structure.ProcessCompletionListener)
	 */
	public void registerProcessCompletionListener(
			ProcessCompletionListener listener) {
		this.completionListeners.add(listener);
	}

}
