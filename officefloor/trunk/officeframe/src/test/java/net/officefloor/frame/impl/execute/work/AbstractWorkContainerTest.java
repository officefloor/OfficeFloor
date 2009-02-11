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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.administrator.AdministratorIndexImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

/**
 * Contains functionality for testing the {@link WorkContainerImpl}.
 * 
 * @author Daniel
 */
public abstract class AbstractWorkContainerTest extends OfficeFrameTestCase {

	/**
	 * Listing of {@link ManagedObjectIndex} instances for the
	 * {@link WorkMetaData}.
	 */
	private final List<ManagedObjectIndex> managedObjectIndexes = new ArrayList<ManagedObjectIndex>();

	/**
	 * {@link ManagedObjectScope#WORK} bound {@link ManagedObjectContainer}
	 * instances.
	 */
	private final List<ManagedObjectContainer> workManagedObjectContainers = new ArrayList<ManagedObjectContainer>();

	/**
	 * Listing of the {@link ManagedObjectMetaData} for the
	 * {@link ManagedObjectScope#WORK} bound {@link ManagedObject} instances.
	 */
	private final List<ManagedObjectMetaData<?>> managedObjectMetaData = new ArrayList<ManagedObjectMetaData<?>>();

	/**
	 * {@link ManagedObjectScope#THREAD} bound {@link ManagedObjectContainer}
	 * instances.
	 */
	private final List<ManagedObjectContainer> threadManagedObjectContainers = new ArrayList<ManagedObjectContainer>();

	/**
	 * {@link ManagedObjectScope#PROCESS} bound {@link ManagedObjectContainer}
	 * instances.
	 */
	private final List<ManagedObjectContainer> processManagedObjectContainers = new ArrayList<ManagedObjectContainer>();

	/**
	 * {@link ManagedObjectContainer} instances that are coordinated.
	 */
	private final Set<ManagedObjectContainer> coordinatedMangedObjectContainers = new HashSet<ManagedObjectContainer>();

	/**
	 * Flags indicating the particular {@link ManagedObjectContainer} is
	 * available.
	 */
	private boolean[] isManagedObjectAvailable = null;

	/**
	 * Listing of {@link AdministratorIndex} instances for the
	 * {@link WorkMetaData}.
	 */
	private final List<AdministratorIndex> administratorIndexes = new ArrayList<AdministratorIndex>();

	/**
	 * {@link AdministratorScope#WORK} bound {@link AdministratorContainer}
	 * instances.
	 */
	private final List<AdministratorContainer<?, ?>> workAdministratorContainers = new ArrayList<AdministratorContainer<?, ?>>();

	/**
	 * Listing of the {@link AdministratorMetaData} for the
	 * {@link AdministratorScope#WORK} bound {@link Administrator} instances.
	 */
	private final List<AdministratorMetaData<?, ?>> administratorMetaData = new ArrayList<AdministratorMetaData<?, ?>>();

	/**
	 * {@link AdministratorScope#THREAD} bound {@link AdministratorContainer}
	 * instances.
	 */
	private final List<AdministratorContainer<?, ?>> threadAdministratorContainers = new ArrayList<AdministratorContainer<?, ?>>();
	/**
	 * {@link AdministratorScope#PROCESS} bound {@link AdministratorContainer}
	 * instances.
	 */
	private final List<AdministratorContainer<?, ?>> processAdministratorContainers = new ArrayList<AdministratorContainer<?, ?>>();

	/**
	 * Flags indicating the particular {@link AdministratorContainer} is
	 * available.
	 */
	private boolean[] isAdministratorAvailable = null;

	/**
	 * Adds a {@link ManagedObjectIndex} for the next index to the
	 * {@link WorkMetaData}.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope} for the {@link ManagedObjectIndex}.
	 */
	protected void addManagedObjectIndex(ManagedObjectScope scope) {

		// Can not change meta-data after initialising
		if (this.isManagedObjectAvailable != null) {
			fail("Can not change meta-data after initialising");
		}

		// Obtain the index of managed object and increment for next.
		// Also create the meta-data for the work bound managed object.
		int scopeBoundIndex;
		switch (scope) {
		case WORK:
			// Index when added
			scopeBoundIndex = this.workManagedObjectContainers.size();

			// Register the work managed object
			this.managedObjectMetaData.add(this
					.createMock(ManagedObjectMetaData.class));
			this.workManagedObjectContainers.add(this
					.createMock(ManagedObjectContainer.class));
			break;

		case THREAD:
			// Index when added
			scopeBoundIndex = this.threadManagedObjectContainers.size();

			// Register the thread managed object
			this.threadManagedObjectContainers.add(this
					.createMock(ManagedObjectContainer.class));
			break;

		case PROCESS:
			// Index when added
			scopeBoundIndex = this.processManagedObjectContainers.size();

			// Register the process managed object
			this.processManagedObjectContainers.add(this
					.createMock(ManagedObjectContainer.class));
			break;

		default:
			fail("Unknown managed object scope " + scope);
			return;
		}

		// Create and add the managed object index
		ManagedObjectIndex index = new ManagedObjectIndexImpl(scope,
				scopeBoundIndex);
		this.managedObjectIndexes.add(index);
	}

	/**
	 * Adds a {@link AdministratorIndex} for the next index to the
	 * {@link WorkMetaData}.
	 * 
	 * @param scope
	 *            {@link AdministratorScope} for the {@link AdministratorIndex}.
	 */
	protected void addAdministratorIndex(AdministratorScope scope) {

		// Can not change meta-data after initialising
		if (this.isAdministratorAvailable != null) {
			fail("Can not change meta-data after initialising");
		}

		// Obtain the index of administrator and increment for next.
		// Also create the meta-data for the work bound administrator.
		int scopeBoundIndex;
		switch (scope) {
		case WORK:
			// Index when added
			scopeBoundIndex = this.workAdministratorContainers.size();

			// Register the work administrator
			this.administratorMetaData.add(this
					.createMock(AdministratorMetaData.class));
			this.workAdministratorContainers.add(this
					.createMock(AdministratorContainer.class));
			break;

		case THREAD:
			// Index when added
			scopeBoundIndex = this.threadAdministratorContainers.size();

			// Register the thread administrator
			this.threadAdministratorContainers.add(this
					.createMock(AdministratorContainer.class));
			break;

		case PROCESS:
			// Index when added
			scopeBoundIndex = this.processAdministratorContainers.size();

			// Register the process administrator
			this.processAdministratorContainers.add(this
					.createMock(AdministratorContainer.class));
			break;

		default:
			fail("Unknown administrator scope " + scope);
			return;
		}

		// Create and add the administrator index
		AdministratorIndex index = new AdministratorIndexImpl(scope,
				scopeBoundIndex);
		this.administratorIndexes.add(index);
	}

	/**
	 * {@link Work}.
	 */
	private final Work work = this.createMock(Work.class);

	/**
	 * {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkMetaData<Work> workMetaData = this
			.createMock(WorkMetaData.class);

	/**
	 * {@link JobContext}.
	 */
	private final JobContext jobContext = this.createMock(JobContext.class);

	/**
	 * {@link JobNode}.
	 */
	private final JobNode jobNode = this.createMock(JobNode.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow flow = this.createMock(Flow.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this
			.createMock(ProcessState.class);

	/**
	 * {@link JobActivateSet}.
	 */
	private final JobActivateSet jobActivateSet = this
			.createMock(JobActivateSet.class);

	/**
	 * {@link TaskDutyAssociation}.
	 */
	private final TaskDutyAssociation<?> taskDutyAssociation = this
			.createMock(TaskDutyAssociation.class);

	/**
	 * {@link AdministratorContext}.
	 */
	private final AdministratorContext administratorContext = this
			.createMock(AdministratorContext.class);

	/**
	 * Records initialising the {@link WorkContainer}.
	 */
	protected void record_WorkContainer_init() {

		// Create the flags indicating whether managed objects loaded
		this.isManagedObjectAvailable = new boolean[this.managedObjectMetaData
				.size()];
		Arrays.fill(this.isManagedObjectAvailable, false);

		// Create the flags indicating whether administrators loaded
		this.isAdministratorAvailable = new boolean[this.administratorMetaData
				.size()];
		Arrays.fill(this.isAdministratorAvailable, false);

		// Obtain the work bound managed object meta-data
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectMetaData(), this.managedObjectMetaData
				.toArray(new ManagedObjectMetaData[0]));

		// Obtain the work bound administrator meta-data
		this.recordReturn(this.workMetaData, this.workMetaData
				.getAdministratorMetaData(), this.administratorMetaData
				.toArray(new AdministratorMetaData[0]));
	}

	/**
	 * Records the loading of the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes of the {@link ManagedObject} instances to load.
	 */
	protected void record_WorkContainer_loadManagedObjects(
			int... managedObjectIndexes) {

		// Obtain the states
		this.recordReturn(this.jobNode, this.jobNode.getFlow(), this.flow);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Obtain the managed object indexes
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectIndexes(), this.managedObjectIndexes
				.toArray(new ManagedObjectIndex[0]));

		// Load the managed objects
		for (int managedObjectIndex : managedObjectIndexes) {

			// Record obtaining the managed object container
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, true);

			// Record loading the managed object
			this.recordReturn(managedObjectContainer, managedObjectContainer
					.loadManagedObject(this.jobContext, this.jobNode,
							this.jobActivateSet), true);
		}
	}

	/**
	 * Records the coordinating of the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes of the {@link ManagedObject} instances to coordinate.
	 */
	protected void record_WorkContainer_coordinateManagedObjects(
			int... managedObjectIndexes) {

		// Obtain the states
		this.recordReturn(this.jobNode, this.jobNode.getFlow(), this.flow);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Obtain the managed object indexes
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectIndexes(), this.managedObjectIndexes
				.toArray(new ManagedObjectIndex[0]));

		// Load the managed objects
		for (int managedObjectIndex : managedObjectIndexes) {

			// Record obtaining the managed object container
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, false);

			// Record loading the managed object
			managedObjectContainer.coordinateManagedObject(null,
					this.jobContext, this.jobNode, this.jobActivateSet);
			if (!this.coordinatedMangedObjectContainers
					.contains(managedObjectContainer)) {
				// Only set the matcher once
				this.control(managedObjectContainer).setMatcher(
						new TypeMatcher(WorkContainer.class, JobContext.class,
								JobNode.class, JobActivateSet.class));
				this.coordinatedMangedObjectContainers
						.add(managedObjectContainer);
			}
		}
	}

	/**
	 * Records whether {@link ManagedObject} instances are ready.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes of the {@link ManagedObject} instances to check are
	 *            ready.
	 */
	protected void record_WorkContainer_isManagedObjectsReady(
			int... managedObjectIndexes) {

		// Obtain the states
		this.recordReturn(this.jobNode, this.jobNode.getFlow(), this.flow);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Obtain the managed object indexes
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectIndexes(), this.managedObjectIndexes
				.toArray(new ManagedObjectIndex[0]));

		// Load the managed objects
		for (int managedObjectIndex : managedObjectIndexes) {

			// Record obtaining the managed object container
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, false);

			// Record indicating if managed object ready
			this.recordReturn(managedObjectContainer, managedObjectContainer
					.isManagedObjectReady(this.jobContext, this.jobNode,
							this.jobActivateSet), true);
		}
	}

	/**
	 * Records obtaining the Object.
	 * 
	 * @param managedObjectIndex
	 *            Index of the {@link ManagedObject} to obtain.
	 * @param object
	 *            Object to return.
	 */
	protected void record_WorkContainer_getObject(int managedObjectIndex,
			Object object) {

		// Obtain the process state
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Obtain the managed object indexes
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectIndexes(), this.managedObjectIndexes
				.toArray(new ManagedObjectIndex[0]));

		// Record obtaining the managed object container
		ManagedObjectContainer managedObjectContainer = this
				.record_getManagedObjectContainer(managedObjectIndex, false);

		// Record obtaining the object
		this.recordReturn(managedObjectContainer, managedObjectContainer
				.getObject(this.threadState), object);
	}

	/**
	 * Records administering the {@link ManagedObject} instances.
	 */
	@SuppressWarnings("unchecked")
	protected void record_WorkContainer_administerManagedObjects(
			int administratorIndex, int... managedObjectIndexes) {

		// Record obtaining the states
		this.recordReturn(this.administratorContext, this.administratorContext
				.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Record obtaining the administrator index
		this.recordReturn(this.taskDutyAssociation, this.taskDutyAssociation
				.getAdministratorIndex(), administratorIndex);

		// Record obtaining the administrator indexes
		this.recordReturn(this.workMetaData, this.workMetaData
				.getAdministratorIndexes(), this.administratorIndexes
				.toArray(new AdministratorIndex[0]));

		// Record obtaining the administrator container
		AdministratorContainer administratorContainer;
		AdministratorIndex index = this.administratorIndexes
				.get(administratorIndex);
		int scopeIndex = index.getIndexOfAdministratorWithinScope();
		switch (index.getAdministratorScope()) {
		case WORK:
			administratorContainer = this.workAdministratorContainers
					.get(scopeIndex);
			if (!this.isAdministratorAvailable[scopeIndex]) {
				// Lazy load the administrator
				this.recordReturn(this.workMetaData, this.workMetaData
						.getAdministratorMetaData(), this.administratorMetaData
						.toArray(new AdministratorMetaData[0]));
				AdministratorMetaData<?, ?> adminMetaData = this.administratorMetaData
						.get(scopeIndex);
				this
						.recordReturn(adminMetaData, adminMetaData
								.createAdministratorContainer(),
								administratorContainer);
				this.isAdministratorAvailable[scopeIndex] = true;
			}
			break;

		case THREAD:
			administratorContainer = this.threadAdministratorContainers
					.get(scopeIndex);
			this.recordReturn(this.threadState, this.threadState
					.getAdministratorContainer(scopeIndex),
					administratorContainer);
			break;

		case PROCESS:
			administratorContainer = this.processAdministratorContainers
					.get(scopeIndex);
			this.recordReturn(this.processState, this.processState
					.getAdministratorContainer(scopeIndex),
					administratorContainer);
			break;

		default:
			fail("Unknown administrator scope " + index.getAdministratorScope());
			return;
		}

		// Obtain the managed object indexes
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectIndexes(), this.managedObjectIndexes
				.toArray(new ManagedObjectIndex[0]));

		// Create the extension interface meta-data
		ExtensionInterfaceMetaData<?>[] eiMetaDatas = new ExtensionInterfaceMetaData[managedObjectIndexes.length];
		for (int i = 0; i < eiMetaDatas.length; i++) {
			eiMetaDatas[i] = this.createMock(ExtensionInterfaceMetaData.class);
		}
		this.recordReturn(administratorContainer, administratorContainer
				.getExtensionInterfaceMetaData(this.administratorContext),
				eiMetaDatas);

		// Record obtaining the extension interfaces for the managed objects
		Object[] extensionInterfaces = new Object[eiMetaDatas.length];
		for (int i = 0; i < eiMetaDatas.length; i++) {
			ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];
			int managedObjectIndex = managedObjectIndexes[i];

			// Record obtaining the managed object
			this.recordReturn(eiMetaData, eiMetaData.getManagedObjectIndex(),
					managedObjectIndex);
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, false);
			ManagedObject managedObject = this.createMock(ManagedObject.class);
			this.recordReturn(managedObjectContainer, managedObjectContainer
					.getManagedObject(this.threadState), managedObject);

			// Record obtaining the extension interface
			ExtensionInterfaceFactory<?> eiFactory = this
					.createMock(ExtensionInterfaceFactory.class);
			this.recordReturn(eiMetaData, eiMetaData
					.getExtensionInterfaceFactory(), eiFactory);
			extensionInterfaces[i] = String.valueOf(i);
			this.recordReturn(eiFactory, eiFactory
					.createExtensionInterface(managedObject),
					extensionInterfaces[i]);
		}

		try {
			// Record administering the managed objects
			administratorContainer.doDuty(this.taskDutyAssociation, Arrays
					.asList(extensionInterfaces), this.administratorContext);
		} catch (Throwable ex) {
			fail("Recording should not throw exception");
		}
	}

	/**
	 * Records unloading the {@link WorkContainer}.
	 */
	protected void record_WorkContainer_unloadWork() {
		// Only unload work managed objects that were loaded
		for (int i = 0; i < this.isManagedObjectAvailable.length; i++) {
			if (this.isManagedObjectAvailable[i]) {
				this.workManagedObjectContainers.get(i).unloadManagedObject();
			}
		}
	}

	/**
	 * Records obtaining the {@link ManagedObjectContainer} for the
	 * {@link ManagedObjectIndex}.
	 * 
	 * @param managedObjectIndex
	 *            Index of the {@link ManagedObject}.
	 * @param isLazyLoad
	 *            Flag indicating if may lazy load the
	 *            {@link ManagedObjectScope#WORK} bound
	 *            {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer}.
	 */
	private ManagedObjectContainer record_getManagedObjectContainer(
			int managedObjectIndex, boolean isLazyLoad) {
		ManagedObjectIndex index = this.managedObjectIndexes
				.get(managedObjectIndex);
		return this.record_getManagedObjectContainer(index, true);
	}

	/**
	 * Records obtaining the {@link ManagedObjectContainer} for the
	 * {@link ManagedObjectIndex}.
	 * 
	 * @param index
	 *            {@link ManagedObjectIndex}.
	 * @param isLazyLoad
	 *            Flag indicating if may lazy load the
	 *            {@link ManagedObjectScope#WORK} bound
	 *            {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer}.
	 */
	private ManagedObjectContainer record_getManagedObjectContainer(
			ManagedObjectIndex index, boolean isLazyLoad) {

		// Obtain the managed object container
		ManagedObjectContainer managedObjectContainer;
		int scopeIndex = index.getIndexOfManagedObjectWithinScope();
		switch (index.getManagedObjectScope()) {
		case WORK:
			// Ensure lazy loaded managed object container
			managedObjectContainer = this.workManagedObjectContainers
					.get(scopeIndex);
			if (isLazyLoad) {
				// Allowed to lazy load the managed object container
				if (!this.isManagedObjectAvailable[scopeIndex]) {
					// Record loading managed object
					this.recordReturn(this.workMetaData, this.workMetaData
							.getManagedObjectMetaData(),
							this.managedObjectMetaData
									.toArray(new ManagedObjectMetaData[0]));
					ManagedObjectMetaData<?> metaData = this.managedObjectMetaData
							.get(scopeIndex);
					this.recordReturn(metaData, metaData
							.createManagedObjectContainer(this.processState),
							managedObjectContainer);
					this.isManagedObjectAvailable[scopeIndex] = true;
				}
			}
			break;

		case THREAD:
			managedObjectContainer = this.threadManagedObjectContainers
					.get(scopeIndex);
			this.recordReturn(this.threadState, this.threadState
					.getManagedObjectContainer(scopeIndex),
					managedObjectContainer);
			break;

		case PROCESS:
			managedObjectContainer = this.processManagedObjectContainers
					.get(scopeIndex);
			this.recordReturn(this.processState, this.processState
					.getManagedObjectContainer(scopeIndex),
					managedObjectContainer);
			break;

		default:
			fail("Unknown managed object scope "
					+ index.getManagedObjectScope());
			return null;
		}

		// Return the managed object container
		return managedObjectContainer;
	}

	/**
	 * Creates the {@link WorkContainer}.
	 * 
	 * @return {@link WorkContainer}.
	 */
	protected WorkContainer<?> createWorkContainer() {
		return new WorkContainerImpl<Work>(this.work, this.workMetaData,
				this.processState);
	}

	/**
	 * Loads the {@link ManagedObject} instances.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param managedObjectIndexes
	 *            Indexes of the {@link ManagedObject} instances to load.
	 */
	protected void loadManagedObjects(WorkContainer<?> workContainer,
			boolean isExpectAllLoaded, int... managedObjectIndexes) {
		boolean isAllLoaded = workContainer.loadManagedObjects(
				managedObjectIndexes, this.jobContext, this.jobNode,
				this.jobActivateSet);
		assertEquals("Incorrect result of loading managed objects",
				isExpectAllLoaded, isAllLoaded);
	}

	/**
	 * Checks if {@link ManagedObject} instances are ready.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param isExpectReady
	 *            Flag indicating if expect {@link ManagedObject} instances to
	 *            be ready.
	 * @param managedObjectIndexes
	 *            Indexes of the {@link ManagedObject} instances to check.
	 */
	protected void isManagedObjectsReady(WorkContainer<?> workContainer,
			boolean isExpectReady, int... managedObjectIndexes) {
		boolean isReady = workContainer.isManagedObjectsReady(
				managedObjectIndexes, this.jobContext, this.jobNode,
				this.jobActivateSet);
		assertEquals("Incorrect result of checking managed objects ready",
				isExpectReady, isReady);
	}

	/**
	 * Coordinates the {@link ManagedObject} instances.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param managedObjectIndexes
	 *            Indexes of the {@link ManagedObject} instances to coordinate.
	 */
	protected void coordinateManagedObject(WorkContainer<?> workContainer,
			int... managedObjectIndexes) {
		workContainer.coordinateManagedObjects(managedObjectIndexes,
				this.jobContext, this.jobNode, this.jobActivateSet);
	}

	/**
	 * Administers the {@link ManagedObject} instances.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @throws Exception
	 *             If fails to administer the {@link ManagedObject} instances.
	 */
	protected void administerManagedObjects(WorkContainer<?> workContainer)
			throws Throwable {
		workContainer.administerManagedObjects(this.taskDutyAssociation,
				this.administratorContext);
	}

	/**
	 * Obtains the object of the {@link ManagedObject}.
	 * 
	 * @param work
	 *            {@link WorkContainer}.
	 * @param managedObjectIndex
	 *            Index of the {@link ManagedObject}.
	 * @param expectedObject
	 *            Expected object to be returned.
	 */
	protected void getObject(WorkContainer<?> work, int managedObjectIndex,
			Object expectedObject) {
		Object object = work.getObject(managedObjectIndex, this.threadState);
		assertEquals("Incorrect returned object", expectedObject, object);
	}

}
