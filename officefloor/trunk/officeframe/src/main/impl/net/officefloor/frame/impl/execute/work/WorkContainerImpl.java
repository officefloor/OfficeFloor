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
package net.officefloor.frame.impl.execute.work;

import java.util.Arrays;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
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

		// Create array to reference the managed objects
		ManagedObjectMetaData moMetaDatas[] = workMetaData
				.getManagedObjectMetaData();
		this.managedObjects = new ManagedObjectContainer[moMetaDatas.length];

		// Create array to referent the administrators
		AdministratorMetaData adminMetaDatas[] = workMetaData
				.getAdministratorMetaData();
		this.administrators = new AdministratorContainer[adminMetaDatas.length];

		// ---------------- TODO remove below --------------------------
		if (true)
			return;

		// Create the administrator containers
		for (int i = 0; i < adminMetaDatas.length; i++) {

			// Obtain the current managed object meta-data
			AdministratorMetaData adminMetaData = adminMetaDatas[i];

			// Create the container for the Administrator
			AdministratorContainer administratorContainer = adminMetaData
					.createAdministratorContainer();

			// Register the administrator
			this.administrators[i] = administratorContainer;
		}

		// Create the managed object containers
		// this.managedObjects = new ManagedObjectContainer[moMetaDatas.length];
		for (int i = 0; i < moMetaDatas.length; i++) {

			// Obtain the current managed object meta-data
			ManagedObjectMetaData moMetaData = moMetaDatas[i];

			// Create the container for the Managed Object (locking on work)
			ManagedObjectContainer managedObjectContainer = moMetaData
					.createManagedObjectContainer(processState);

			// Register the managed object
			this.managedObjects[i] = managedObjectContainer;
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
	@SuppressWarnings("deprecation")
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
			JobContext jobContext, JobNode jobNode, JobActivateSet notifySet) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		boolean isAllLoaded = true;

		// Obtain the states
		ThreadState threadState = jobNode.getFlow().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Load the managed objects
		ManagedObjectIndex[] indexes = this.workMetaData
				.getManagedObjectIndexes();
		for (int moIndex : managedObjectIndexes) {

			// Obtain the index of managed object within scope
			ManagedObjectIndex index = indexes[moIndex];
			int scopeIndex = index.getIndexOfManagedObjectWithinScope();

			// Obtain the managed object container
			ManagedObjectContainer container;
			switch (index.getManagedObjectScope()) {
			case WORK:
				// Lazy load the container
				container = this.managedObjects[scopeIndex];
				if (container == null) {
					container = this.workMetaData.getManagedObjectMetaData()[scopeIndex]
							.createManagedObjectContainer(processState);
					this.managedObjects[scopeIndex] = container;
				}
				break;

			case THREAD:
				// Obtain the container from the thread state
				container = threadState.getManagedObjectContainer(scopeIndex);
				break;

			case PROCESS:
				// Obtain the container from the process state
				container = processState.getManagedObjectContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown managed object scope "
						+ index.getManagedObjectScope());
			}

			// Load the managed object
			isAllLoaded &= container.loadManagedObject(jobContext, jobNode,
					notifySet);
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
			JobContext jobContext, JobNode jobNode, JobActivateSet notifySet) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getFlow().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Coordinate the managed objects
		ManagedObjectIndex[] indexes = this.workMetaData
				.getManagedObjectIndexes();
		for (int moIndex : managedObjectIndexes) {

			// Obtain the index of managed object within scope
			ManagedObjectIndex index = indexes[moIndex];
			int scopeIndex = index.getIndexOfManagedObjectWithinScope();

			// Obtain the managed object container
			ManagedObjectContainer container;
			switch (index.getManagedObjectScope()) {
			case WORK:
				// Always available by loadManagedObjects
				container = this.managedObjects[scopeIndex];
				break;

			case THREAD:
				container = threadState.getManagedObjectContainer(scopeIndex);
				break;

			case PROCESS:
				container = processState.getManagedObjectContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown managed object scope "
						+ index.getManagedObjectScope());
			}

			// Coordinate the managed object
			container.coordinateManagedObject(this, jobContext, jobNode,
					notifySet);
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
			JobContext jobContext, JobNode jobNode, JobActivateSet notifySet) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getFlow().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Coordinate the managed objects
		ManagedObjectIndex[] indexes = this.workMetaData
				.getManagedObjectIndexes();
		for (int moIndex : managedObjectIndexes) {

			// Obtain the index of managed object within scope
			ManagedObjectIndex index = indexes[moIndex];
			int scopeIndex = index.getIndexOfManagedObjectWithinScope();

			// Obtain the managed object container
			ManagedObjectContainer container;
			switch (index.getManagedObjectScope()) {
			case WORK:
				// Always available by loadManagedObjects
				container = this.managedObjects[scopeIndex];
				break;

			case THREAD:
				container = threadState.getManagedObjectContainer(scopeIndex);
				break;

			case PROCESS:
				container = processState.getManagedObjectContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown managed object scope "
						+ index.getManagedObjectScope());
			}

			// Indicate not ready on first managed object not ready
			if (!container.isManagedObjectReady(jobContext, jobNode, notifySet)) {
				return false;
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
			AdministratorContext adminContext) throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Obtain the states
		ThreadState threadState = adminContext.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Obtain the administrator container
		AdministratorIndex adminIndex = this.workMetaData
				.getAdministratorIndexes()[duty.getAdministratorIndex()];
		int adminScopeIndex = adminIndex.getIndexOfAdministratorWithinScope();
		AdministratorContainer adminContainer;
		switch (adminIndex.getAdministratorScope()) {
		case WORK:
			// Lazy create the administrator container. This is the safe to lazy
			// create as work containers are not shared between threads and this
			// operates within the thread lock.
			adminContainer = this.administrators[adminScopeIndex];
			if (adminContainer == null) {
				adminContainer = this.workMetaData.getAdministratorMetaData()[adminScopeIndex]
						.createAdministratorContainer();
				this.administrators[adminScopeIndex] = adminContainer;
			}
			break;

		case THREAD:
			adminContainer = threadState
					.getAdministratorContainer(adminScopeIndex);
			break;

		case PROCESS:
			adminContainer = processState
					.getAdministratorContainer(adminScopeIndex);
			break;

		default:
			throw new IllegalStateException("Unknown managed object scope "
					+ adminIndex.getAdministratorScope());
		}

		// Obtain the managed object indexes
		ManagedObjectIndex[] indexes = this.workMetaData
				.getManagedObjectIndexes();

		// Obtain the extension interfaces to be managed
		ExtensionInterfaceMetaData<?>[] eiMetaDatas = adminContainer
				.getExtensionInterfaceMetaData(adminContext);
		Object[] ei = new Object[eiMetaDatas.length];
		for (int i = 0; i < eiMetaDatas.length; i++) {
			ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

			// Obtain the index of managed object within scope
			ManagedObjectIndex moIndex = indexes[eiMetaData
					.getManagedObjectIndex()];
			int moScopeIndex = moIndex.getIndexOfManagedObjectWithinScope();

			// Obtain the managed object container
			ManagedObjectContainer container;
			switch (moIndex.getManagedObjectScope()) {
			case WORK:
				// Always available by loadManagedObjects
				container = this.managedObjects[moScopeIndex];
				break;

			case THREAD:
				container = threadState.getManagedObjectContainer(moScopeIndex);
				break;

			case PROCESS:
				container = processState
						.getManagedObjectContainer(moScopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown managed object scope "
						+ moIndex.getManagedObjectScope());
			}

			// Obtain the managed object
			ManagedObject managedObject = container
					.getManagedObject(threadState);

			// Obtain and load the extension interface
			ei[i] = eiMetaData.getExtensionInterfaceFactory()
					.createExtensionInterface(managedObject);
		}

		// Administer the managed objects
		adminContainer.doDuty(duty, Arrays.asList(ei), adminContext);
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

		// Access Point: Job
		// Locks: ThreadState

		// Obtain the states
		ProcessState processState = threadState.getProcessState();

		// Obtain the index of managed object within scope
		ManagedObjectIndex index = this.workMetaData.getManagedObjectIndexes()[moIndex];
		int scopeIndex = index.getIndexOfManagedObjectWithinScope();

		// Obtain the managed object container
		ManagedObjectContainer container;
		switch (index.getManagedObjectScope()) {
		case WORK:
			// Always available by loadManagedObjects
			container = this.managedObjects[scopeIndex];
			break;

		case THREAD:
			container = threadState.getManagedObjectContainer(scopeIndex);
			break;

		case PROCESS:
			container = processState.getManagedObjectContainer(scopeIndex);
			break;

		default:
			throw new IllegalStateException("Unknown managed object scope "
					+ index.getManagedObjectScope());
		}

		// Return the object of the managed object
		return container.getObject(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkContainer#unloadWork()
	 */
	@Override
	public void unloadWork() {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Unload the work bound managed objects
		for (ManagedObjectContainer container : this.managedObjects) {
			if (container != null) {
				container.unloadManagedObject();
			}
		}
	}

}
