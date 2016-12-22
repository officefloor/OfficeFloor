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
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.Promise;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

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
	 * {@link ThreadState} owning this {@link WorkContainer}.
	 */
	private final ThreadState threadState;

	/**
	 * Index of next {@link ManagedObject} to load.
	 */
	private int loadIndex = 0;

	/**
	 * Initiate.
	 * 
	 * @param work
	 *            {@link Work} to be managed.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param threadState
	 *            {@link ThreadState}.
	 */
	@SuppressWarnings("rawtypes")
	public WorkContainerImpl(W work, WorkMetaData<W> workMetaData, ThreadState threadState) {
		this.work = work;
		this.workMetaData = workMetaData;
		this.threadState = threadState;

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
	public FunctionState loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			ManagedFunctionContainer managedJobNode) {

		// Load the managed objects
		if (this.loadIndex < managedObjectIndexes.length) {
			ManagedObjectIndex index = managedObjectIndexes[this.loadIndex++];

			// Obtain the managed object container
			ManagedObjectContainer container = this.getManagedObjectContainer(index);

			// Trigger loading the managed object
			return container.loadManagedObject(managedJobNode, this);
		}

		// Managed Objects are loaded
		return null;
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(ManagedObjectIndex managedObjectIndex) {

		// Obtain the index of managed object within scope
		int scopeIndex = managedObjectIndex.getIndexOfManagedObjectWithinScope();

		// Obtain the managed object container
		ManagedObjectContainer container;
		switch (managedObjectIndex.getManagedObjectScope()) {
		case WORK:
			// Lazy load the container
			container = this.managedObjects[scopeIndex];
			if (container == null) {
				container = this.workMetaData.getManagedObjectMetaData()[scopeIndex]
						.createManagedObjectContainer(this.threadState);
				this.managedObjects[scopeIndex] = container;
			}
			break;

		case THREAD:
			// Obtain the container from the thread state
			container = this.threadState.getManagedObjectContainer(scopeIndex);
			break;

		case PROCESS:
			// Obtain the container from the process state
			container = this.threadState.getProcessState().getManagedObjectContainer(scopeIndex);
			break;

		default:
			throw new IllegalStateException(
					"Unknown managed object scope " + managedObjectIndex.getManagedObjectScope());
		}

		// Return the container
		return container;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FunctionState administerManagedObjects(TaskDutyAssociation<?> duty, AdministratorContext adminContext)
			throws Throwable {

		// Obtain the index identifying the administrator
		AdministratorIndex adminIndex = duty.getAdministratorIndex();
		AdministratorScope adminScope = adminIndex.getAdministratorScope();
		int adminScopeIndex = adminIndex.getIndexOfAdministratorWithinScope();

		// Obtain the administrator container
		AdministratorContainer adminContainer;
		switch (adminScope) {
		case WORK:
			// Lazy create the administrator container. This is safe to lazy
			// create as work containers are not shared between threads and
			// this operates within the thread and process lock.
			adminContainer = this.administrators[adminScopeIndex];
			if (adminContainer == null) {
				adminContainer = this.workMetaData.getAdministratorMetaData()[adminScopeIndex]
						.createAdministratorContainer(adminContext.getThreadState());
				this.administrators[adminScopeIndex] = adminContainer;
			}
			break;

		case THREAD:
			adminContainer = this.threadState.getAdministratorContainer(adminScopeIndex);
			break;

		case PROCESS:
			adminContainer = this.threadState.getProcessState().getAdministratorContainer(adminScopeIndex);
			break;

		default:
			throw new IllegalStateException("Unknown administrator scope " + adminIndex.getAdministratorScope());
		}

		// Obtain the extension interfaces to be managed
		ExtensionInterfaceMetaData<?>[] eiMetaDatas = adminContainer.getExtensionInterfaceMetaData(adminContext);
		List extensionInterfaces = new ArrayList(eiMetaDatas.length);
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
				container = this.threadState.getManagedObjectContainer(moScopeIndex);
				break;

			case PROCESS:
				container = this.threadState.getProcessState().getManagedObjectContainer(moScopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown managed object scope " + moIndex.getManagedObjectScope());
			}

			// Extract the extension interface
			Object extensionInterface = container
					.extractExtensionInterface(eiMetaData.getExtensionInterfaceExtractor());

			// Load the extension interface for administration
			extensionInterfaces.add(extensionInterface);
		}

		// Administer the managed objects
		return adminContainer.doDuty(duty, extensionInterfaces, adminContext);
	}

	@Override
	public Object getObject(ManagedObjectIndex index) {

		// Obtain the managed object container
		ManagedObjectContainer container = this.getManagedObjectContainer(index);

		// Return the object of the managed object
		return container.getObject();
	}

	@Override
	public FunctionState unloadWork() {

		// Unload the work bound managed objects
		FunctionState unloadJobNode = null;
		for (int i = this.managedObjects.length - 1; i >= 0; i--) {
			unloadJobNode = Promise.then(this.managedObjects[i].unloadManagedObject(), unloadJobNode);
		}
		return unloadJobNode;
	}

}