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
import net.officefloor.frame.impl.execute.jobnode.WaitJobNode;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
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
import net.officefloor.frame.spi.managedobject.source.CriticalSection;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Container of a {@link Work} instance.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // move functionality into AbstractJobContainer (with managed
			// objects bound to Task)
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
	public WorkContainerImpl(W work, WorkMetaData<W> workMetaData, ProcessState processState) {
		this.work = work;
		this.workMetaData = workMetaData;

		// Create array to reference the managed objects
		ManagedObjectMetaData moMetaDatas[] = workMetaData.getManagedObjectMetaData();
		this.managedObjects = new ManagedObjectContainer[moMetaDatas.length];

		// Create array to reference the administrators
		AdministratorMetaData adminMetaDatas[] = workMetaData.getAdministratorMetaData();
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
	public JobNode loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Load the managed objects
		boolean isRequireWaiting = false;
		for (int i = 0; i < managedObjectIndexes.length; i++) {
			ManagedObjectIndex index = managedObjectIndexes[i];

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
				throw new IllegalStateException("Unknown managed object scope " + index.getManagedObjectScope());
			}

			// Trigger loading the managed object
			isRequireWaiting |= container.loadManagedObject(jobContext, jobNode);
		}

		// Determine if must wait for loading
		if (isRequireWaiting) {
			// Wait to be loaded
			return new WaitJobNode(jobNode);
		}

		// Managed Objects are loaded
		return null;
	}

	@Override
	public JobNode governManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getThreadState();
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
				throw new IllegalStateException("Unknown managed object scope " + index.getManagedObjectScope());
			}

			// Govern the managed object
			JobNode governJobNode = container.governManagedObject(this, jobContext, jobNode);
			if (governJobNode != null) {
				return governJobNode;
			}
		}

		// Managed objects are governed
		return null;
	}

	@Override
	public JobNode coordinateManagedObjects(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getThreadState();
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
				throw new IllegalStateException("Unknown managed object scope " + index.getManagedObjectScope());
			}

			// Do not continue onto next unless coordinated
			JobNode coordinateJobNode = container.coordinateManagedObject(this, jobContext, jobNode);
			if (coordinateJobNode != null) {
				return coordinateJobNode;
			}
		}

		// Managed objects are co-ordinated
		return null;
	}

	@Override
	public boolean isManagedObjectsReady(ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Obtain the states
		ThreadState threadState = jobNode.getThreadState();
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
				throw new IllegalStateException("Unknown managed object scope " + index.getManagedObjectScope());
			}

			// Indicate not ready on first managed object not ready
			if (!container.isManagedObjectReady(this, jobContext, jobNode)) {
				return false;
			}
		}

		// As here, all managed objects are ready
		return true;
	}

	/**
	 * State for {@link CriticalSection} to administer the {@link ManagedObject}
	 * instances.
	 */
	private static class AdministerManagedObjectsState {

		/**
		 * {@link TaskDutyAssociation}.
		 */
		private final TaskDutyAssociation<?> duty;

		/**
		 * {@link AdministratorContext}.
		 */
		private final AdministratorContext adminContext;

		/**
		 * {@link WorkContainerImpl}.
		 */
		private final WorkContainerImpl<?> workContainer;

		/**
		 * {@link AdministratorContainer}.
		 */
		private AdministratorContainer adminContainer;

		/**
		 * Extension interfaces.
		 */
		private List extensionInterfaces;

		/**
		 * Instantiate.
		 * 
		 * @param duty
		 *            {@link TaskDutyAssociation}.
		 * @param adminContext
		 *            {@link AdministratorContext}.
		 * @param workContainer
		 *            {@link WorkContainer}.
		 */
		public AdministerManagedObjectsState(TaskDutyAssociation<?> duty, AdministratorContext adminContext,
				WorkContainerImpl<?> workContainer) {
			this.duty = duty;
			this.adminContext = adminContext;
			this.workContainer = workContainer;
		}
	}

	/**
	 * {@link CriticalSection} to administer the {@link ManagedObject}
	 * instances.
	 */
	private static final CriticalSection<AdministerManagedObjectsState, AdministerManagedObjectsState, Throwable> administerManagedObjectsCriticalSection = new CriticalSection<AdministerManagedObjectsState, WorkContainerImpl.AdministerManagedObjectsState, Throwable>() {
		@Override
		public AdministerManagedObjectsState doCriticalSection(AdministerManagedObjectsState state) throws Throwable {

			// Obtain the states
			ThreadState threadState = state.adminContext.getThreadState();
			ProcessState processState = threadState.getProcessState();

			// Obtain the index identifying the administrator
			AdministratorIndex adminIndex = state.duty.getAdministratorIndex();
			AdministratorScope adminScope = adminIndex.getAdministratorScope();
			int adminScopeIndex = adminIndex.getIndexOfAdministratorWithinScope();

			// Obtain the administrator container
			switch (adminScope) {
			case WORK:
				// Lazy create the administrator container. This is safe to lazy
				// create as work containers are not shared between threads and
				// this operates within the thread and process lock.
				state.adminContainer = state.workContainer.administrators[adminScopeIndex];
				if (state.adminContainer == null) {
					state.adminContainer = state.workContainer.workMetaData.getAdministratorMetaData()[adminScopeIndex]
							.createAdministratorContainer();
					state.workContainer.administrators[adminScopeIndex] = state.adminContainer;
				}
				break;

			case THREAD:
				state.adminContainer = threadState.getAdministratorContainer(adminScopeIndex);
				break;

			case PROCESS:
				state.adminContainer = processState.getAdministratorContainer(adminScopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown administrator scope " + adminIndex.getAdministratorScope());
			}

			// Obtain the extension interfaces to be managed
			ExtensionInterfaceMetaData<?>[] eiMetaDatas = state.adminContainer
					.getExtensionInterfaceMetaData(state.adminContext);
			state.extensionInterfaces = new ArrayList(eiMetaDatas.length);
			for (int i = 0; i < eiMetaDatas.length; i++) {
				ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

				// Obtain the index of managed object to administer
				ManagedObjectIndex moIndex = eiMetaData.getManagedObjectIndex();

				// Obtain the managed object container
				ManagedObjectContainer container;
				int moScopeIndex = moIndex.getIndexOfManagedObjectWithinScope();
				switch (moIndex.getManagedObjectScope()) {
				case WORK:
					container = state.workContainer.managedObjects[moScopeIndex];
					break;

				case THREAD:
					container = threadState.getManagedObjectContainer(moScopeIndex);
					break;

				case PROCESS:
					container = processState.getManagedObjectContainer(moScopeIndex);
					break;

				default:
					throw new IllegalStateException("Unknown managed object scope " + moIndex.getManagedObjectScope());
				}

				// Extract the extension interface
				Object extensionInterface = container
						.extractExtensionInterface(eiMetaData.getExtensionInterfaceExtractor());

				// Load the extension interface for administration
				state.extensionInterfaces.add(extensionInterface);
			}

			// Return the state
			return state;
		}
	};

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JobNode administerManagedObjects(TaskDutyAssociation<?> duty, AdministratorContext adminContext)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Administer the managed objects
		AdministerManagedObjectsState state = adminContext.getThreadState().doProcessCriticalSection(
				new AdministerManagedObjectsState(duty, adminContext, this), administerManagedObjectsCriticalSection);
		return state.adminContainer.doDuty(duty, state.extensionInterfaces, adminContext);
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
			throw new IllegalStateException("Unknown managed object scope " + index.getManagedObjectScope());
		}

		// Return the object of the managed object
		return container.getObject(threadState);
	}

	@Override
	public JobNode unloadWork(JobNode continueJobNode) {

		// Access Point: Job
		// Locks: ThreadState -> ProcessState

		// Unload the work bound managed objects
		for (ManagedObjectContainer container : this.managedObjects) {
			if (container != null) {
				JobNode unloadJobNode = container.unloadManagedObject(continueJobNode);
				if (unloadJobNode != null) {
					return unloadJobNode;
				}
			}
		}

		// Work unloaded
		return null;
	}

}