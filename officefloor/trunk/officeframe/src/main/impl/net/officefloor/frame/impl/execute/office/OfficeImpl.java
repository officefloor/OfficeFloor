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
package net.officefloor.frame.impl.execute.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.impl.execute.work.WorkManagerImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link Office} implementation.
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
	 * {@link ProcessMetaData} of the {@link ProcessState} instances created
	 * within this {@link Office}.
	 */
	protected final ProcessMetaData processMetaData;

	/**
	 * Catch all {@link EscalationHandler} for this {@link Office}. May be
	 * <code>null</code>.
	 */
	protected final EscalationHandler officeEscalationHandler;

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
	 * @param processMetaData
	 *            {@link ProcessMetaData}.
	 * @param officeEscalationHandler
	 *            Catch all {@link EscalationHandler} for this {@link Office}.
	 * @param startupFlows
	 *            {@link Flow} instances to invoke on start up of the Office.
	 */
	@SuppressWarnings("unchecked")
	public OfficeImpl(Map<String, WorkMetaData<?>> workMetaData,
			Map<String, ManagedObjectSource<?, ?>> managedObjectSources,
			ProcessMetaData processMetaData,
			EscalationHandler officeEscalationHandler,
			FlowMetaData[] startupFlows) {
		this.managedObjectSources = managedObjectSources;
		this.processMetaData = processMetaData;
		this.officeEscalationHandler = officeEscalationHandler;
		this.startupFlows = startupFlows;

		// Create the Work Registry
		this.workRegistry = new HashMap<String, WorkManager>();
		for (String name : workMetaData.keySet()) {
			// Register the Work Manager
			this.workRegistry.put(name, new WorkManagerImpl(name, workMetaData
					.get(name), this));
		}
	}

	/**
	 * Creates a {@link Job} within a new {@link ProcessState} for this Office.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} to instigate in a new
	 *            {@link ProcessState}.
	 * @param parameter
	 *            Parameter for the initial {@link Task}.
	 * @param managedObject
	 *            {@link ManagedObject} invoking the {@link ProcessState}. May
	 *            be <code>null</code>.
	 * @param processMoIndex
	 *            Index of the input {@link ManagedObject} on the
	 *            {@link ProcessState}. This value is only used if provided a
	 *            {@link ManagedObject}.
	 * @param managedObjectEscalationHandler
	 *            {@link EscalationHandler} provided by the
	 *            {@link ManagedObjectSource}.
	 * @return {@link JobNode} within a new {@link ProcessState} for this
	 *         {@link Office}.
	 */
	public <W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter, ManagedObject managedObject, int processMoIndex,
			EscalationHandler managedObjectEscalationHandler) {

		// Create the Process State
		ProcessState processState = new ProcessStateImpl(this.processMetaData,
				managedObjectEscalationHandler, this.officeEscalationHandler);

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

		// Create the Job Node for the initial job
		JobNode jobNode = flow.createJobNode(taskMetaData, null, parameter);

		// Return the Job Node
		return jobNode;
	}

	/**
	 * Opens this {@link Office}.
	 */
	void openOffice() {
		// Invoke the start up flows
		for (FlowMetaData<?> flowMetaData : this.startupFlows) {
			this.createProcess(flowMetaData, null, null, 0, null).activateJob();
		}
	}

	/*
	 * ====================== Office ==================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.manage.OfficeManager#getWorkManager(java.lang
	 * .String)
	 */
	public WorkManager getWorkManager(String name) throws UnknownWorkException {
		WorkManager workManager = this.workRegistry.get(name);
		if (workManager == null) {
			throw new UnknownWorkException(name);
		}
		return workManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.manage.OfficeManager#getManagedObject(java.
	 * lang.String)
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
