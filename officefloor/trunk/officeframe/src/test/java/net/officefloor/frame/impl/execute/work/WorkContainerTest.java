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

import org.easymock.internal.AlwaysMatcher;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.WorkContainerImpl;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WorkContainer}.
 * 
 * @author Daniel
 */
public class WorkContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link WorkContainer} being tested.
	 */
	private WorkContainerImpl<Work> workContainer;

	/**
	 * Mock {@link Work}.
	 */
	private Work work = this.createMock(Work.class);

	/**
	 * Mock {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private WorkMetaData<Work> workMetaData = this
			.createMock(WorkMetaData.class);

	/**
	 * Mock {@link ProcessState}.
	 */
	private ProcessState processState = this.createMock(ProcessState.class);

	/**
	 * Mock work bound {@link ManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedObjectMetaData<Indexed> workMoMetaData = this
			.createMock(ManagedObjectMetaData.class);

	/**
	 * Mock work bound {@link ManagedObjectContainer}.
	 */
	private ManagedObjectContainer workMoContainer = this
			.createMock(ManagedObjectContainer.class);

	/**
	 * Mock process bound {@link ManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedObjectMetaData<Indexed> processMoMetaData = this
			.createMock(ManagedObjectMetaData.class);

	/**
	 * Mock process bound {@link ManagedObjectContainer}.
	 */
	private ManagedObjectContainer processMoContainer = this
			.createMock(ManagedObjectContainer.class);

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private ManagedObjectMetaData<?>[] moMetaData = new ManagedObjectMetaData[] {
			this.workMoMetaData, this.processMoMetaData };

	/**
	 * Mock work bound {@link AdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private AdministratorMetaData<?, ?> workAdminMetaData = this
			.createMock(AdministratorMetaData.class);

	/**
	 * Mock work bound {@link AdministratorContainer}.
	 */
	@SuppressWarnings("unchecked")
	private AdministratorContainer workAdminContainer = this
			.createMock(AdministratorContainer.class);

	/**
	 * Mock process bound {@link AdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private AdministratorMetaData<?, ?> processAdminMetaData = this
			.createMock(AdministratorMetaData.class);

	/**
	 * Mock process bound {@link AdministratorContainer}.
	 */
	@SuppressWarnings("unchecked")
	private AdministratorContainer processAdminContainer = this
			.createMock(AdministratorContainer.class);

	/**
	 * {@link AdministratorMetaData} instances.
	 */
	private AdministratorMetaData<?, ?>[] adminMetaData = new AdministratorMetaData<?, ?>[] {
			this.workAdminMetaData, this.processAdminMetaData };

	/**
	 * Mock {@link JobContext}.
	 */
	private JobContext executionContext = this
			.createMock(JobContext.class);

	/**
	 * Mock {@link Job}.
	 */
	private Job taskContainer = this.createMock(Job.class);

	/**
	 * Mock {@link JobActivateSet}.
	 */
	private JobActivateSet assetNotifySet = this
			.createMock(JobActivateSet.class);

	/**
	 * Mock {@link TaskDutyConfiguration}.
	 */
	private TaskDutyAssociation<?> taskDutyAssociation = this
			.createMock(TaskDutyAssociation.class);

	/**
	 * Mock {@link AdministratorContext}.
	 */
	private AdministratorContext adminContext = this
			.createMock(AdministratorContext.class);

	/**
	 * Mock {@link ThreadState}.
	 */
	private ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link ProcessState} lock.
	 */
	private Object processLock = new Object();

	/**
	 * Mock work {@link ExtensionInterfaceMetaData}.
	 */
	private ExtensionInterfaceMetaData<?> workEiMetaData = this
			.createMock(ExtensionInterfaceMetaData.class);

	/**
	 * Mock process {@link ExtensionInterfaceMetaData}.
	 */
	private ExtensionInterfaceMetaData<?> processEiMetaData = this
			.createMock(ExtensionInterfaceMetaData.class);

	/**
	 * {@link ExtensionInterfaceMetaData} instances.
	 */
	private ExtensionInterfaceMetaData<?>[] eiMetaData = new ExtensionInterfaceMetaData[] {
			this.workEiMetaData, this.processEiMetaData };

	/**
	 * Mock work {@link ManagedObject}.
	 */
	private ManagedObject workManagedObject = this
			.createMock(ManagedObject.class);

	/**
	 * Mock process {@link ManagedObject}.
	 */
	private ManagedObject processManagedObject = this
			.createMock(ManagedObject.class);

	/**
	 * Mock work {@link ExtensionInterfaceFactory}.
	 */
	private ExtensionInterfaceFactory<?> workEiFactory = this
			.createMock(ExtensionInterfaceFactory.class);

	/**
	 * Mock process {@link ExtensionInterfaceFactory}.
	 */
	private ExtensionInterfaceFactory<?> processEiFactory = this
			.createMock(ExtensionInterfaceFactory.class);

	/**
	 * Extension interfaces.
	 */
	private Object[] extensionInterfaces = new Object[] { new Object(),
			new Object() };

	/**
	 * Ensure handles happy day scenario.
	 */
	@SuppressWarnings("unchecked")
	public void testHappyDayScenario() throws Exception {

		final int[] managedObjectIndexes = new int[] { 0, 1 };

		// Record creating work container
		this.recordWorkContainerCreation();

		// Record load
		this.recordGetManagedObjectMetaData();
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordReturn(this.workMoContainer, this.workMoContainer
				.loadManagedObject(this.executionContext, this.taskContainer,
						this.assetNotifySet), true);
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordGetProcessLock();
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordReturn(this.processMoContainer, this.processMoContainer
				.loadManagedObject(this.executionContext, this.taskContainer,
						this.assetNotifySet), true);

		// Record ready
		this.recordIsManagedObjectReady();

		// Record co-ordinating
		this.recordGetManagedObjectMetaData();
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.workMoContainer.coordinateManagedObject(this.workContainer,
				this.executionContext, this.taskContainer, this.assetNotifySet);
		this.control(this.workMoContainer).setMatcher(new AlwaysMatcher());
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordGetProcessLock();
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.processMoContainer.coordinateManagedObject(this.workContainer,
				this.executionContext, this.taskContainer, this.assetNotifySet);
		this.control(this.processMoContainer).setMatcher(new AlwaysMatcher());

		// Record ready
		this.recordIsManagedObjectReady();

		// Record pre-administer
		this.recordReturn(this.adminContext,
				this.adminContext.getThreadState(), this.threadState);
		this.recordReturn(this.taskDutyAssociation, this.taskDutyAssociation
				.getAdministratorIndex(), 0);
		this.recordGetManagedObjectMetaData();
		this.recordReturn(this.workAdminContainer, this.workAdminContainer
				.getExtensionInterfaceMetaData(this.adminContext),
				this.eiMetaData);
		// Work lock processing
		this.recordReturn(this.workEiMetaData, this.workEiMetaData
				.getManagedObjectIndex(), 0);
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordReturn(this.workMoContainer, this.workMoContainer
				.getManagedObject(this.threadState), this.workManagedObject);
		this.recordReturn(this.workEiMetaData, this.workEiMetaData
				.getExtensionInterfaceFactory(), this.workEiFactory);
		this.recordReturn(this.workEiFactory, this.workEiFactory
				.createExtensionInterface(this.workManagedObject),
				this.extensionInterfaces[0]);
		this.recordReturn(this.processEiMetaData, this.processEiMetaData
				.getManagedObjectIndex(), 1);
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		// Process lock processing
		this.recordReturn(this.processState,
				this.processState.getProcessLock(), this.processLock);
		this.recordReturn(this.workEiMetaData, this.workEiMetaData
				.getManagedObjectIndex(), 0);
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordReturn(this.processEiMetaData, this.processEiMetaData
				.getManagedObjectIndex(), 1);
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordReturn(this.processMoContainer, this.processMoContainer
				.getManagedObject(this.threadState), this.processManagedObject);
		this.recordReturn(this.processEiMetaData, this.processEiMetaData
				.getExtensionInterfaceFactory(), this.processEiFactory);
		this.recordReturn(this.processEiFactory, this.processEiFactory
				.createExtensionInterface(this.processManagedObject),
				this.extensionInterfaces[1]);
		// Record doing the duty
		this.workAdminContainer.doDuty(this.taskDutyAssociation, Arrays
				.asList(this.extensionInterfaces), this.adminContext);

		// Replay
		this.replayMockObjects();

		// Load, ready, co-ordinate, ready, administer
		// (remaining: ready, doTask, ready, administer)
		this.createWorkCtontainer();
		this.workContainer.loadManagedObjects(managedObjectIndexes,
				this.executionContext, this.taskContainer, this.assetNotifySet);
		this.workContainer.isManagedObjectsReady(managedObjectIndexes,
				this.executionContext, this.taskContainer, this.assetNotifySet);
		this.workContainer.coordinateManagedObjects(managedObjectIndexes,
				this.executionContext, this.taskContainer, this.assetNotifySet);
		this.workContainer.isManagedObjectsReady(managedObjectIndexes,
				this.executionContext, this.taskContainer, this.assetNotifySet);
		this.workContainer.administerManagedObjects(this.taskDutyAssociation,
				this.adminContext);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Record obtaining the {@link ManagedObjectMetaData} instances.
	 */
	private void recordGetManagedObjectMetaData() {
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectMetaData(), this.moMetaData);
	}

	/**
	 * Record obtaining the {@link ProcessState} {@link ManagedObject} index for
	 * the {@link Work} bound {@link ManagedObjectContainer}.
	 */
	private void recordWorkGetProcessStateManagedObjectIndex() {
		this.recordReturn(this.workMoMetaData, this.workMoMetaData
				.getProcessStateManagedObjectIndex(),
				ManagedObjectMetaData.NON_PROCESS_INDEX);
	}

	/**
	 * Record obtaining the {@link ProcessState} {@link ManagedObject} index for
	 * the {@link ProcessState} bound {@link ManagedObjectContainer}.
	 */
	private void recordProcessGetProcessStateManagedObjectIndex() {
		this.recordReturn(this.processMoMetaData, this.processMoMetaData
				.getProcessStateManagedObjectIndex(), 0);
	}

	/**
	 * Records obtaining the {@link ProcessState} lock from the
	 * {@link Job}.
	 */
	private void recordGetProcessLock() {
		this.recordReturn(this.taskContainer, this.taskContainer
				.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.recordReturn(this.processState,
				this.processState.getProcessLock(), this.processLock);
	}

	/**
	 * Records
	 * {@link WorkContainer#isManagedObjectsReady(int[], JobContext, Job, JobActivateSet)}.
	 */
	private void recordIsManagedObjectReady() {
		this.recordGetManagedObjectMetaData();
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordReturn(this.workMoContainer, this.workMoContainer
				.isManagedObjectReady(this.executionContext,
						this.taskContainer, this.assetNotifySet), true);
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordGetProcessLock();
		this.recordWorkGetProcessStateManagedObjectIndex();
		this.recordProcessGetProcessStateManagedObjectIndex();
		this.recordReturn(this.processMoContainer, this.processMoContainer
				.isManagedObjectReady(this.executionContext,
						this.taskContainer, this.assetNotifySet), true);
	}

	/**
	 * Records the creation of the {@link WorkContainer}.
	 */
	private void recordWorkContainerCreation() {

		// Record creating the managed object containers
		this.recordReturn(this.workMetaData, this.workMetaData
				.getManagedObjectMetaData(), this.moMetaData);
		this.recordReturn(this.workMoMetaData, this.workMoMetaData
				.createManagedObjectContainer(this.workContainer),
				this.workMoContainer, new AlwaysMatcher());
		this.recordReturn(this.processMoMetaData, this.processMoMetaData
				.createManagedObjectContainer(this.workContainer),
				this.processMoContainer, new AlwaysMatcher());

		// Record creating the administrator
		this.recordReturn(this.workMetaData, this.workMetaData
				.getAdministratorMetaData(), this.adminMetaData);
		this.recordReturn(this.workAdminMetaData, this.workAdminMetaData
				.createAdministratorContainer(), this.workAdminContainer);
		this.recordReturn(this.processAdminMetaData, this.processAdminMetaData
				.createAdministratorContainer(), this.processAdminContainer);
	}

	/**
	 * Creates the {@link WorkContainer}.
	 */
	private void createWorkCtontainer() {
		this.workContainer = new WorkContainerImpl<Work>(this.work,
				this.workMetaData, this.processState);
	}
}
