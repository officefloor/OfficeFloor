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
package net.officefloor.frame.impl.execute.work;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
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
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Container of a {@link Work} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkContainerImpl<W extends Work> implements WorkContainer<W> {

	/**
	 * {@link Work} being managed.
	 */
	private final W work;

	/**
	 * {@link WorkMetaData} of the {@link Work} being managed.
	 */
	private final WorkMetaData<W> workMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for the respective
	 * {@link ManagedObject} instances bound to this {@link Work}.
	 */
	private final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link AdministratorContainer} instances for the respective
	 * {@link Administrator} instances bound to this {@link Work}.
	 */
	private final AdministratorContainer<?, ?>[] administrators;

	/**
	 * Initiate.
	 * 
	 * @param work
	 *            {@link Work} to be managed.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param processState
	 *            {@link ProcessState}.
	 */
	@SuppressWarnings("rawtypes")
	public WorkContainerImpl(W work, WorkMetaData<W> workMetaData,
			ProcessState processState) {
		this.work = work;
		this.workMetaData = workMetaData;

		// Create array to reference the managed objects
		ManagedObjectMetaData moMetaDatas[] = workMetaData
				.getManagedObjectMetaData();
		this.managedObjects = new ManagedObjectContainer[moMetaDatas.length];

		// Create array to reference the administrators
		AdministratorMetaData adminMetaDatas[] = workMetaData
				.getAdministratorMetaData();
		this.administrators = new AdministratorContainer[adminMetaDatas.length];
	}

	/*
	 * ================== WorkContainer ===================================
	 */

	@Override
	public W getWork(ThreadState threadState) {
		return this.work;
	}

	@Override
	public void loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet notifySet, TeamIdentifier currentTeam,
			ContainerContext context) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getJobSequence().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Load the managed objects
		for (ManagedObjectIndex index : managedObjectIndexes) {

			// Obtain the index of managed object within scope
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

			// Trigger loading the managed object
			container.loadManagedObject(jobContext, jobNode, notifySet,
					currentTeam, context);
		}
	}

	@Override
	public void governManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getJobSequence().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Govern the managed objects
		for (ManagedObjectIndex index : managedObjectIndexes) {

			// Obtain the index of managed object within scope
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

			// Govern the managed object
			if (!container.governManagedObject(this, jobContext, jobNode,
					activateSet, context)) {
				return; // must wait on current managed object
			}
		}
	}

	@Override
	public void coordinateManagedObjects(
			ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet notifySet,
			ContainerContext context) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getJobSequence().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Coordinate the managed objects
		for (ManagedObjectIndex index : managedObjectIndexes) {

			// Obtain the index of managed object within scope
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

			// Do not continue onto next unless coordinated
			if (!container.coordinateManagedObject(this, jobContext, jobNode,
					notifySet, context)) {
				return; // must wait on current managed object
			}
		}
	}

	@Override
	public boolean isManagedObjectsReady(
			ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet notifySet,
			ContainerContext context) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getJobSequence().getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Coordinate the managed objects
		for (ManagedObjectIndex index : managedObjectIndexes) {

			// Obtain the index of managed object within scope
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
			if (!container.isManagedObjectReady(this, jobContext, jobNode,
					notifySet, context)) {
				return false;
			}
		}

		// As here, all managed objects are ready
		return true;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext, ContainerContext containerContext)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Obtain the states
		ThreadState threadState = adminContext.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Obtain the index identifying the administrator
		AdministratorIndex adminIndex = duty.getAdministratorIndex();
		AdministratorScope adminScope = adminIndex.getAdministratorScope();
		int adminScopeIndex = adminIndex.getIndexOfAdministratorWithinScope();

		// Obtain the administrator container.
		// Must be done within process lock as may be changing state of process.
		AdministratorContainer adminContainer;
		List ei;
		synchronized (processState.getProcessLock()) {
			switch (adminScope) {
			case WORK:
				// Lazy create the administrator container. This is safe to lazy
				// create as work containers are not shared between threads and
				// this operates within the thread and process lock.
				adminContainer = this.administrators[adminScopeIndex];
				if (adminContainer == null) {
					adminContainer = this.workMetaData
							.getAdministratorMetaData()[adminScopeIndex]
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
				throw new IllegalStateException("Unknown administrator scope "
						+ adminIndex.getAdministratorScope());
			}

			// Obtain the extension interfaces to be managed
			ExtensionInterfaceMetaData<?>[] eiMetaDatas = adminContainer
					.getExtensionInterfaceMetaData(adminContext);
			ei = new ArrayList(eiMetaDatas.length); // create to size
			for (int i = 0; i < eiMetaDatas.length; i++) {
				ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

				// Obtain the index of managed object to administer
				ManagedObjectIndex moIndex = eiMetaData.getManagedObjectIndex();

				// Obtain the managed object container
				ManagedObjectContainer container;
				int moScopeIndex = moIndex.getIndexOfManagedObjectWithinScope();
				switch (moIndex.getManagedObjectScope()) {
				case WORK:
					container = this.managedObjects[moScopeIndex];
					break;

				case THREAD:
					container = threadState
							.getManagedObjectContainer(moScopeIndex);
					break;

				case PROCESS:
					container = processState
							.getManagedObjectContainer(moScopeIndex);
					break;

				default:
					throw new IllegalStateException(
							"Unknown managed object scope "
									+ moIndex.getManagedObjectScope());
				}

				// Extract the extension interface
				Object extensionInterface = container
						.extractExtensionInterface(eiMetaData
								.getExtensionInterfaceExtractor());

				// Load the extension interface for administration
				ei.add(extensionInterface);
			}
		}

		// Administer the managed objects
		adminContainer.doDuty(duty, ei, adminContext, containerContext);
	}

	@Override
	public Object getObject(ManagedObjectIndex index, ThreadState threadState) {

		// Access Point: Job
		// Locks: ThreadState

		// Obtain the states
		ProcessState processState = threadState.getProcessState();

		// Obtain the index of managed object within scope
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

	@Override
	public void unloadWork(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Unload the work bound managed objects
		for (ManagedObjectContainer container : this.managedObjects) {
			if (container != null) {
				container.unloadManagedObject(activateSet, currentTeam);
			}
		}
	}

}