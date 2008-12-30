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

import java.util.Arrays;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Container of a {@link Work} instance.
 * 
 * @author Daniel
 */
public class WorkContainerImpl<W extends Work> implements WorkContainer<W> {

	/**
	 * {@link Work} being managed.
	 */
	protected final W work;

	/**
	 * {@link WorkMetaData} of the {@link Work} being managed.
	 */
	protected final WorkMetaData<W> workMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for the respective
	 * {@link ManagedObject} instances.
	 */
	protected final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link AdministratorContainer} instances for the respective
	 * {@link Administrator} instances.
	 */
	protected final AdministratorContainer<?, ?>[] administrators;

	/**
	 * Count of {@link ThreadState} actively using this {@link Work} of this
	 * {@link WorkContainer}.
	 */
	protected int activeThreadCount = 0;

	/**
	 * Initiate.
	 * 
	 * @param work
	 *            {@link Work} to be managed.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public WorkContainerImpl(W work, WorkMetaData<W> workMetaData,
			ProcessState processState) {
		this.work = work;
		this.workMetaData = workMetaData;

		// Create the managed object containers
		ManagedObjectMetaData moMetaDatas[] = workMetaData
				.getManagedObjectMetaData();
		this.managedObjects = new ManagedObjectContainer[moMetaDatas.length];
		for (int i = 0; i < moMetaDatas.length; i++) {

			// Obtain the current managed object meta-data
			ManagedObjectMetaData moMetaData = moMetaDatas[i];

			// Create the container for the Managed Object (locking on work)
			ManagedObjectContainer managedObjectContainer = moMetaData
					.createManagedObjectContainer(processState);

			// Register the managed object
			this.managedObjects[i] = managedObjectContainer;
		}

		// Create the administrator containers
		AdministratorMetaData adminMetaDatas[] = workMetaData
				.getAdministratorMetaData();
		this.administrators = new AdministratorContainer[adminMetaDatas.length];
		for (int i = 0; i < adminMetaDatas.length; i++) {

			// Obtain the current managed object meta-data
			AdministratorMetaData adminMetaData = adminMetaDatas[i];

			// Create the container for the Administrator
			AdministratorContainer administratorContainer = adminMetaData
					.createAdministratorContainer();

			// Register the administrator
			this.administrators[i] = administratorContainer;
		}
	}

	/*
	 * ================== WorkContainer ===================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkContainer#getWorkId()
	 */
	@Override
	public int getWorkId() {
		return this.workMetaData.getWorkId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.WorkContainer#getWork(net.
	 * officefloor.frame.internal.structure.ThreadState)
	 */
	@Override
	public W getWork(ThreadState threadState) {
		return this.work;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.WorkContainer#loadManagedObjects
	 * (int[], net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean loadManagedObjects(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {

		boolean isAllLoaded = true;

		// Skip over if no required managed objects
		if (managedObjectIndexes.length >= 0) {

			// Obtain the work managed object meta-data
			ManagedObjectMetaData<?> workMoMetaDatas[] = this.workMetaData
					.getManagedObjectMetaData();

			// Lock for loading the work scoped managed objects
			synchronized (this) {
				// Load the work scoped managed objects
				for (int moIndex : managedObjectIndexes) {
					// Determine if managed object is work scoped
					if (workMoMetaDatas[moIndex]
							.getProcessStateManagedObjectIndex() == ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Load the work scoped managed object
						isAllLoaded &= this.managedObjects[moIndex]
								.loadManagedObject(executionContext, jobNode,
										notifySet);
					}
				}
			}

			// Lock for loading the process coped managed objects
			synchronized (jobNode.getFlow().getThreadState().getProcessState()
					.getProcessLock()) {
				// Load the process scoped managed objects
				for (int moIndex : managedObjectIndexes) {
					// Determine if managed object is process scoped
					if (workMoMetaDatas[moIndex]
							.getProcessStateManagedObjectIndex() != ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Load the process scoped managed object
						isAllLoaded &= this.managedObjects[moIndex]
								.loadManagedObject(executionContext, jobNode,
										notifySet);
					}
				}
			}
		}

		// Return whether all loaded
		return isAllLoaded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.WorkContainer#
	 * coordinateManagedObjects(int[],
	 * net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public void coordinateManagedObjects(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {

		// Skip over if no required managed objects
		if (managedObjectIndexes.length >= 0) {

			// Obtain the work managed object meta-data
			ManagedObjectMetaData<?> workMoMetaDatas[] = this.workMetaData
					.getManagedObjectMetaData();

			// Lock for co-ordinating the work scoped managed objects
			synchronized (this) {
				// Co-ordinate the work scoped managed objects
				for (int moIndex : managedObjectIndexes) {
					// Determine if managed object is work scoped
					if (workMoMetaDatas[moIndex]
							.getProcessStateManagedObjectIndex() == ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Co-ordinate the work scoped managed object
						this.managedObjects[moIndex].coordinateManagedObject(
								this, executionContext, jobNode, notifySet);
					}
				}
			}

			// Lock for co-ordinating the process coped managed objects
			synchronized (jobNode.getFlow().getThreadState().getProcessState()
					.getProcessLock()) {
				// Co-ordinate the process scoped managed objects
				for (int moIndex : managedObjectIndexes) {
					// Determine if managed object is process scoped
					if (workMoMetaDatas[moIndex]
							.getProcessStateManagedObjectIndex() != ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Co-ordinate the process scoped managed object
						this.managedObjects[moIndex].coordinateManagedObject(
								this, executionContext, jobNode, notifySet);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.WorkContainer#isManagedObjectsReady
	 * (int[], net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean isManagedObjectsReady(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {

		// Skip over if no managed objects
		if (managedObjectIndexes.length >= 0) {

			// Obtain the work managed object meta-data
			ManagedObjectMetaData<?> workMoMetaDatas[] = this.workMetaData
					.getManagedObjectMetaData();

			// Lock for checking the work scoped managed objects
			synchronized (this) {
				// Check the work scoped managed objects
				for (int moIndex : managedObjectIndexes) {
					// Determine if managed object is work scoped
					if (workMoMetaDatas[moIndex]
							.getProcessStateManagedObjectIndex() == ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Check the work scoped managed object
						if (!this.managedObjects[moIndex].isManagedObjectReady(
								executionContext, jobNode, notifySet)) {
							// Waiting on managed object to be ready
							return false;
						}
					}
				}
			}

			// Lock for checking the process coped managed objects
			synchronized (jobNode.getFlow().getThreadState().getProcessState()
					.getProcessLock()) {
				// Check the process scoped managed objects
				for (int moIndex : managedObjectIndexes) {
					// Determine if managed object is process scoped
					if (workMoMetaDatas[moIndex]
							.getProcessStateManagedObjectIndex() != ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Check the process scoped managed object
						if (!this.managedObjects[moIndex].isManagedObjectReady(
								executionContext, jobNode, notifySet)) {
							// Waiting on managed object to be ready
							return false;
						}
					}
				}
			}
		}

		// As here, all managed objects are ready
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.WorkContainer#
	 * administerManagedObjects
	 * (net.officefloor.frame.internal.structure.TaskDutyAssociation,
	 * net.officefloor.frame.internal.structure.AdministratorContext)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext) throws Exception {

		// Obtain the thread state
		ThreadState threadState = adminContext.getThreadState();

		// Obtain the administrator container
		AdministratorContainer container = this.administrators[duty
				.getAdministratorIndex()];

		// Obtain the work managed object meta-data
		ManagedObjectMetaData workMoMetaDatas[] = this.workMetaData
				.getManagedObjectMetaData();

		// Obtain the extension interfaces to be managed
		ExtensionInterfaceMetaData<?>[] eiMetaDatas = container
				.getExtensionInterfaceMetaData(adminContext);
		Object[] ei = new Object[eiMetaDatas.length];

		// Obtain the work scoped extension interfaces
		synchronized (this) {
			for (int i = 0; i < eiMetaDatas.length; i++) {
				// Obtain current ei meta-data
				ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

				// Obtain the managed object index
				int moIndex = eiMetaData.getManagedObjectIndex();

				// Determine if managed object is work scoped
				if (workMoMetaDatas[moIndex]
						.getProcessStateManagedObjectIndex() == ManagedObjectMetaData.NON_PROCESS_INDEX) {
					// Obtain the managed object
					ManagedObject managedObject = this.managedObjects[moIndex]
							.getManagedObject(threadState);

					// Obtain the extension interface
					ei[i] = eiMetaData.getExtensionInterfaceFactory()
							.createExtensionInterface(managedObject);
				}
			}
		}

		// Obtain the process scoped extension interfaces
		synchronized (threadState.getProcessState().getProcessLock()) {
			for (int i = 0; i < eiMetaDatas.length; i++) {
				// Obtain current ei meta-data
				ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

				// Obtain the managed object index
				int moIndex = eiMetaData.getManagedObjectIndex();

				// Determine if managed object is process scoped
				if (workMoMetaDatas[moIndex]
						.getProcessStateManagedObjectIndex() != ManagedObjectMetaData.NON_PROCESS_INDEX) {
					// Obtain the managed object
					ManagedObject managedObject = this.managedObjects[moIndex]
							.getManagedObject(threadState);

					// Obtain the extension interface
					ei[i] = eiMetaData.getExtensionInterfaceFactory()
							.createExtensionInterface(managedObject);
				}
			}

			// Within the process lock administer the managed objects
			container.doDuty(duty, Arrays.asList(ei), adminContext);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.WorkContainer#getObject(int,
	 * net.officefloor.frame.internal.structure.ThreadState)
	 */
	@Override
	public Object getObject(int moIndex, ThreadState threadState) {
		return this.managedObjects[moIndex].getObject(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkContainer#unloadWork()
	 */
	@Override
	public void unloadWork() {
		// Obtain the work managed object meta-data
		ManagedObjectMetaData<?> workMoMetaDatas[] = this.workMetaData
				.getManagedObjectMetaData();

		// Unload the work scoped managed objects
		for (int i = 0; i < workMoMetaDatas.length; i++) {
			// Determine if managed object is work scoped
			if (workMoMetaDatas[i].getProcessStateManagedObjectIndex() == ManagedObjectMetaData.NON_PROCESS_INDEX) {
				// Unload the work scoped managed object
				this.managedObjects[i].unloadManagedObject();
			}
		}
	}

}
