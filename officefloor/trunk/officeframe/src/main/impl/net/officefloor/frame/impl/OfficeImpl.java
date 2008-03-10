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
package net.officefloor.frame.impl;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.execute.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.ProcessStateImpl;
import net.officefloor.frame.impl.execute.ThreadWorkLinkImpl;
import net.officefloor.frame.impl.execute.WorkContainerImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.spi.team.Team;

/**
 * Office.
 * 
 * @author Daniel
 */
public class OfficeImpl implements Office {

	/**
	 * Registry of {@link WorkManager} by its name.
	 */
	protected final Map<String, WorkManager> workRegistry;

	/**
	 * Registry of {@link ManagedObjectSource} instances by their name.
	 */
	protected final Map<String, ManagedObjectSource<?, ?>> managedObjectSources;

	/**
	 * {@link ManagedObjectMetaData} instances for the {@link ProcessState}
	 * within this Office.
	 */
	protected final ManagedObjectMetaData<?>[] processStateManagedObjectMetaData;

	/**
	 * {@link AdministratorMetaData} instances for the {@link ProcessState}
	 * within this Office.
	 */
	protected final AdministratorMetaData<?, ?>[] processStateAdministratorMetaData;

	/**
	 * {@link Flow} instances to invoke on start up of the Office.
	 */
	protected final FlowMetaData<?>[] startupFlows;

	/**
	 * Initiate.
	 * 
	 * @param teams
	 *            Set of {@link Team} instances.
	 * @param workMetaData
	 *            Registry of {@link WorkMetaData} by its name.
	 * @param managedObjectSources
	 *            Registry of {@link ManagedObjectSource} by its name.
	 * @param processStateManagedObjectMetaData
	 *            {@link ManagedObjectMetaData} instances for the
	 *            {@link ProcessState} within this Office.
	 * @param processStateAdministratorMetaData
	 *            {@link AdministratorMetaData} instances for the
	 *            {@link ProcessState} within this Office.
	 * @param startupFlows
	 *            {@link Flow} instances to invoke on start up of the Office.
	 */
	@SuppressWarnings("unchecked")
	public OfficeImpl(Map<String, WorkMetaData<?>> workMetaData,
			Map<String, ManagedObjectSource<?, ?>> managedObjectSources,
			ManagedObjectMetaData[] processStateManagedObjectMetaData,
			AdministratorMetaData[] processStateAdministratorMetaData,
			FlowMetaData[] startupFlows) {
		// Store state
		this.managedObjectSources = managedObjectSources;
		this.processStateManagedObjectMetaData = processStateManagedObjectMetaData;
		this.processStateAdministratorMetaData = processStateAdministratorMetaData;
		this.startupFlows = startupFlows;

		// Create the Work Registry
		this.workRegistry = new HashMap<String, WorkManager>();
		for (String name : workMetaData.keySet()) {
			// Register the Work Manager
			this.workRegistry.put(name, new WorkManagerImpl(workMetaData
					.get(name), this));
		}
	}

	/**
	 * Creates a {@link TaskContainer} within a new {@link ProcessState} for
	 * this Office.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} to instigate in a new
	 *            {@link ProcessState}.
	 * @param parameter
	 *            Parameter for the initial
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param managedObject
	 *            {@link ManagedObject} invoking the {@link ProcessState}. May
	 *            be <code>null</code>.
	 * @param processMoIndex
	 *            Index of the input {@link ManagedObject} on the
	 *            {@link ProcessState}. This value is only used if provided a
	 *            {@link ManagedObject}.
	 * @return {@link TaskContainer} within a new {@link ProcessState} for this
	 *         Office.
	 */
	public <W extends Work> TaskContainer createProcess(
			FlowMetaData<W> flowMetaData, Object parameter,
			ManagedObject managedObject, int processMoIndex) {

		// Create the Process State
		ProcessState processState = new ProcessStateImpl(
				this.processStateManagedObjectMetaData,
				this.processStateAdministratorMetaData);

		// Determine if require loading the managed object
		if (managedObject != null) {
			// Obtain the container for the managed object
			ManagedObjectContainerImpl moc = (ManagedObjectContainerImpl) processState
					.getManagedObjectContainer(processMoIndex);

			// Load the managed object
			moc.loadManagedObject(managedObject);
		}

		// Create the Flow
		Flow flow = processState.createThread(flowMetaData);

		// Obtain the task meta-data
		TaskMetaData<?, W, ?, ?> taskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Obtain the work meta-data
		WorkMetaData<W> workMetaData = taskMetaData.getWorkMetaData();

		// Create the work to invoke
		W work = workMetaData.getWorkFactory().createWork();

		// Create the Work Container for the Work (not attached to a process)
		WorkContainerImpl<W> workContainer = new WorkContainerImpl<W>(work,
				workMetaData, processState);

		// Obtain the ThreadState
		ThreadState threadState = flow.getThreadState();

		try {
			// Specify the context for the Work
			work.setWorkContext(workContainer);
		} catch (Exception ex) {
			// Specify failure to be handled
			threadState.setFailure(ex);
		}

		// Create the thread work link
		ThreadWorkLink<W> workLink = new ThreadWorkLinkImpl<W>(threadState,
				workContainer);

		// Create the Task Container for the initial Task
		TaskContainer taskContainer = flow.createTaskContainer(taskMetaData,
				null, parameter, workLink);

		// Return the Task Container
		return taskContainer;
	}

	/**
	 * Opens this {@link Office}.
	 */
	void openOffice() {
		// Invoke the start up flows
		for (FlowMetaData<?> flowMetaData : this.startupFlows) {
			this.createProcess(flowMetaData, null, null, 0).activateTask();
		}
	}

	/*
	 * ====================================================================
	 * OfficeManager
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeManager#getWorkManager(java.lang.String)
	 */
	public WorkManager getWorkManager(String name) {
		WorkManager workManager = this.workRegistry.get(name);
		if (workManager == null) {
			throw new NullPointerException("Unknown work manager '" + name
					+ "'");
		}
		return workManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeManager#getManagedObject(java.lang.String)
	 */
	public ManagedObject getManagedObject(String managedObjectId)
			throws Exception {

		// References to load Managed Object
		final ManagedObject[] loadedManagedObject = new ManagedObject[1];
		final Throwable[] loadFailureCause = new Throwable[1];

		// Obtain the managed object source
		ManagedObjectSource<?, ?> source = this.managedObjectSources
				.get(managedObjectId);
		if (source == null) {
			throw new Exception("Unknown managed object '" + managedObjectId
					+ "'");
		}

		// Attempt to load the Managed Object
		source.sourceManagedObject(new ManagedObjectUser() {
			public void setManagedObject(ManagedObject managedObject) {
				synchronized (loadedManagedObject) {
					// Load the Managed Object
					loadedManagedObject[0] = managedObject;

					// Notify loaded
					loadedManagedObject.notify();
				}
			}

			public void setFailure(Throwable cause) {
				synchronized (loadedManagedObject) {
					// Flag the failure cause
					loadFailureCause[0] = cause;

					// Notify loaded
					loadedManagedObject.notify();
				}
			}
		});

		// Wait for Managed Object to be loaded
		for (;;) {
			// Return if loaded
			synchronized (loadedManagedObject) {

				// Check if obtained managed object
				if (loadedManagedObject[0] != null) {
					return loadedManagedObject[0];
				}

				// Propagate if failed to obtain
				if (loadFailureCause[0] != null) {
					throw new Error(loadFailureCause[0]);
				}

				// Wait to be loaded
				try {
					loadedManagedObject.wait();
				} catch (InterruptedException ex) {
					// Interrupted, therefore return null
					return null;
				}
			}
		}
	}

}
