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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.administrator.AdministratorIndexImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionState;
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
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

import org.easymock.AbstractMatcher;

/**
 * Contains functionality for testing the {@link WorkContainerImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWorkContainerTest extends OfficeFrameTestCase {

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
	 * @return {@link ManagedObjectIndex}.
	 */
	protected ManagedObjectIndex addManagedObjectIndex(ManagedObjectScope scope) {

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
			return null;
		}

		// Create, add and return the managed object index
		ManagedObjectIndex index = new ManagedObjectIndexImpl(scope,
				scopeBoundIndex);
		return index;
	}

	/**
	 * Adds a {@link AdministratorIndex} for the next index to the
	 * {@link WorkMetaData}.
	 * 
	 * @param scope
	 *            {@link AdministratorScope} for the {@link AdministratorIndex}.
	 * @return {@link AdministratorIndex}.
	 */
	protected AdministratorIndex addAdministratorIndex(AdministratorScope scope) {

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
			return null;
		}

		// Create and return the administrator index
		return new AdministratorIndexImpl(scope, scopeBoundIndex);
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
	 * {@link FunctionState}.
	 */
	private final FunctionState jobNode = this.createMock(FunctionState.class);

	/**
	 * {@link ContainerContext}.
	 */
	private final ContainerContext containerContext = this
			.createMock(ContainerContext.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow jobSequence = this.createMock(Flow.class);

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
	 * {@link JobNodeActivateSet}.
	 */
	private final JobNodeActivateSet jobActivateSet = this
			.createMock(JobNodeActivateSet.class);

	/**
	 * {@link TeamIdentifier} for the current {@link Team}.
	 */
	private final TeamIdentifier currentTeam = this
			.createMock(TeamIdentifier.class);

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
	 *            {@link ManagedObjectIndex} instances specifying the
	 *            {@link ManagedObject} instances to load.
	 */
	protected void record_WorkContainer_loadManagedObjects(
			ManagedObjectIndex... managedObjectIndexes) {

		// Obtain the states
		this.recordReturn(this.jobNode, this.jobNode.getJobSequence(),
				this.jobSequence);
		this.recordReturn(this.jobSequence, this.jobSequence.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Load the managed objects
		for (ManagedObjectIndex managedObjectIndex : managedObjectIndexes) {

			// Record obtaining the managed object container
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, true);

			// Record loading the managed object
			managedObjectContainer.loadManagedObject(this.jobContext,
					this.jobNode, this.jobActivateSet, this.currentTeam,
					this.containerContext);
		}
	}

	/**
	 * Records the coordinating of the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances specifying the
	 *            {@link ManagedObject} instances to coordinate.
	 */
	protected void record_WorkContainer_coordinateManagedObjects(
			ManagedObjectIndex... managedObjectIndexes) {

		// Obtain the states
		this.recordReturn(this.jobNode, this.jobNode.getJobSequence(),
				this.jobSequence);
		this.recordReturn(this.jobSequence, this.jobSequence.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Load the managed objects
		for (ManagedObjectIndex managedObjectIndex : managedObjectIndexes) {

			// Record obtaining the managed object container
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, false);

			// Record loading the managed object
			this.recordReturn(managedObjectContainer, managedObjectContainer
					.coordinateManagedObject(null, this.jobContext,
							this.jobNode, this.jobActivateSet,
							this.containerContext), true);
			if (!this.coordinatedMangedObjectContainers
					.contains(managedObjectContainer)) {
				// Only set the matcher once
				this.control(managedObjectContainer).setMatcher(
						new TypeMatcher(WorkContainer.class, JobContext.class,
								FunctionState.class, JobNodeActivateSet.class,
								ContainerContext.class));
				this.coordinatedMangedObjectContainers
						.add(managedObjectContainer);
			}
		}
	}

	/**
	 * Flags if the {@link Matcher} has been loaded for the
	 * <code>isManagedObjectsReady</code> for the {@link ManagedObjectContainer}
	 * .
	 */
	private Set<ManagedObjectContainer> managedObjectContainersWithIsReadyMatcherProvided = new HashSet<ManagedObjectContainer>();

	/**
	 * Records whether {@link ManagedObject} instances are ready.
	 * 
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances specifying the
	 *            {@link ManagedObject} instances to check are ready.
	 */
	protected void record_WorkContainer_isManagedObjectsReady(
			ManagedObjectIndex... managedObjectIndexes) {

		// Obtain the states
		this.recordReturn(this.jobNode, this.jobNode.getJobSequence(),
				this.jobSequence);
		this.recordReturn(this.jobSequence, this.jobSequence.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Load the managed objects
		for (ManagedObjectIndex managedObjectIndex : managedObjectIndexes) {

			// Record obtaining the managed object container
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(managedObjectIndex, false);

			// Record indicating if managed object ready
			if (!this.managedObjectContainersWithIsReadyMatcherProvided
					.contains(managedObjectContainer)) {
				// Record with matcher
				this.recordReturn(managedObjectContainer,
						managedObjectContainer.isManagedObjectReady(null,
								this.jobContext, this.jobNode,
								this.jobActivateSet, this.containerContext),
						true, new AbstractMatcher() {
							@Override
							public boolean matches(Object[] expected,
									Object[] actual) {
								assertTrue("First should be WorkContainer",
										actual[0] instanceof WorkContainer<?>);
								for (int i = 1; i < expected.length; i++) {
									assertEquals("Incorrect parameter " + i,
											expected[i], actual[i]);
								}
								return true; // matches if here
							}
						});

				// Matcher now provided
				this.managedObjectContainersWithIsReadyMatcherProvided
						.add(managedObjectContainer);

			} else {
				// Record without matcher
				this.recordReturn(managedObjectContainer,
						managedObjectContainer.isManagedObjectReady(null,
								this.jobContext, this.jobNode,
								this.jobActivateSet, this.containerContext),
						true);
			}
		}
	}

	/**
	 * Records obtaining the Object.
	 * 
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} instance specifying the
	 *            {@link ManagedObject} to obtain.
	 * @param object
	 *            Object to return.
	 */
	protected void record_WorkContainer_getObject(
			ManagedObjectIndex managedObjectIndex, Object object) {

		// Obtain the process state
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Record obtaining the managed object container
		ManagedObjectContainer managedObjectContainer = this
				.record_getManagedObjectContainer(managedObjectIndex, false);

		// Record obtaining the object
		this.recordReturn(managedObjectContainer,
				managedObjectContainer.getObject(this.threadState), object);
	}

	/**
	 * Records administering the {@link ManagedObject} instances.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void record_WorkContainer_administerManagedObjects(
			AdministratorIndex adminIndex, ManagedObjectIndex... moIndexes) {

		// Record obtaining the states
		this.recordReturn(this.administratorContext,
				this.administratorContext.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);

		// Record obtaining the administrator index
		this.recordReturn(this.taskDutyAssociation,
				this.taskDutyAssociation.getAdministratorIndex(), adminIndex);

		// Record obtaining the process lock
		this.recordReturn(this.processState,
				this.processState.getProcessLock(), "Process Lock");

		// Record obtaining the administrator container
		AdministratorContainer administratorContainer;
		int scopeIndex = adminIndex.getIndexOfAdministratorWithinScope();
		switch (adminIndex.getAdministratorScope()) {
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
				this.recordReturn(adminMetaData,
						adminMetaData.createAdministratorContainer(),
						administratorContainer);
				this.isAdministratorAvailable[scopeIndex] = true;
			}
			break;

		case THREAD:
			administratorContainer = this.threadAdministratorContainers
					.get(scopeIndex);
			this.recordReturn(this.threadState,
					this.threadState.getAdministratorContainer(scopeIndex),
					administratorContainer);
			break;

		case PROCESS:
			administratorContainer = this.processAdministratorContainers
					.get(scopeIndex);
			this.recordReturn(this.processState,
					this.processState.getAdministratorContainer(scopeIndex),
					administratorContainer);
			break;

		default:
			fail("Unknown administrator scope "
					+ adminIndex.getAdministratorScope());
			return;
		}

		// Create the extension interface meta-data
		ExtensionInterfaceMetaData<?>[] eiMetaDatas = new ExtensionInterfaceMetaData[moIndexes.length];
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
			ManagedObjectIndex moIndex = moIndexes[i];

			// Record obtaining the managed object container
			this.recordReturn(eiMetaData, eiMetaData.getManagedObjectIndex(),
					moIndex);
			ManagedObjectContainer managedObjectContainer = this
					.record_getManagedObjectContainer(moIndex, false);

			// Record extracting the extension interface
			ExtensionInterfaceExtractor<?> eiExtractor = this
					.createMock(ExtensionInterfaceExtractor.class);
			this.recordReturn(eiMetaData,
					eiMetaData.getExtensionInterfaceExtractor(), eiExtractor);
			extensionInterfaces[i] = String.valueOf(i);
			this.recordReturn(managedObjectContainer, managedObjectContainer
					.extractExtensionInterface(eiExtractor),
					extensionInterfaces[i]);
		}

		try {
			// Record administering the managed objects
			administratorContainer.doDuty(this.taskDutyAssociation,
					Arrays.asList(extensionInterfaces),
					this.administratorContext, this.containerContext);
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
				this.workManagedObjectContainers.get(i).unloadManagedObject(
						this.jobActivateSet, this.currentTeam);
			}
		}
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
			this.recordReturn(this.threadState,
					this.threadState.getManagedObjectContainer(scopeIndex),
					managedObjectContainer);
			break;

		case PROCESS:
			managedObjectContainer = this.processManagedObjectContainers
					.get(scopeIndex);
			this.recordReturn(this.processState,
					this.processState.getManagedObjectContainer(scopeIndex),
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
	 *            {@link ManagedObjectIndex} instances specifying the
	 *            {@link ManagedObject} instances to load.
	 */
	protected void loadManagedObjects(WorkContainer<?> workContainer,
			ManagedObjectIndex... managedObjectIndexes) {
		workContainer.loadManagedObjects(managedObjectIndexes, this.jobContext,
				this.jobNode, this.jobActivateSet, this.currentTeam,
				this.containerContext);
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
	 *            {@link ManagedObjectIndex} instances specifying the
	 *            {@link ManagedObject} instances to check.
	 */
	protected void isManagedObjectsReady(WorkContainer<?> workContainer,
			boolean isExpectReady, ManagedObjectIndex... managedObjectIndexes) {
		boolean isReady = workContainer.isManagedObjectsReady(
				managedObjectIndexes, this.jobContext, this.jobNode,
				this.jobActivateSet, this.containerContext);
		assertEquals("Incorrect result of checking managed objects ready",
				isExpectReady, isReady);
	}

	/**
	 * Coordinates the {@link ManagedObject} instances.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param managedObjectIndexes
	 *            {@link ManagedObjectIndex} instances specifying the
	 *            {@link ManagedObject} instances to coordinate.
	 */
	protected void coordinateManagedObject(WorkContainer<?> workContainer,
			ManagedObjectIndex... managedObjectIndexes) {
		workContainer.coordinateManagedObjects(managedObjectIndexes,
				this.jobContext, this.jobNode, this.jobActivateSet,
				this.containerContext);
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
				this.administratorContext, this.containerContext);
	}

	/**
	 * Obtains the object of the {@link ManagedObject}.
	 * 
	 * @param work
	 *            {@link WorkContainer}.
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} instance specifying the
	 *            {@link ManagedObject}.
	 * @param expectedObject
	 *            Expected object to be returned.
	 */
	protected void getObject(WorkContainer<?> work,
			ManagedObjectIndex managedObjectIndex, Object expectedObject) {
		Object object = work.getObject(managedObjectIndex, this.threadState);
		assertEquals("Incorrect returned object", expectedObject, object);
	}

	/**
	 * Unloads the {@link WorkContainer}.
	 * 
	 * @param work
	 *            {@link WorkContainer}.
	 */
	protected void unloadWork(WorkContainer<?> work) {
		work.unloadWork(this.jobActivateSet, this.currentTeam);
	}

}