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

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the methods of the {@link WorkContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkContainerTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link Work}.
	 */
	private final Work work = this.createMock(Work.class);

	/**
	 * Mock {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkMetaData<Work> workMetaData = this
			.createMock(WorkMetaData.class);

	/**
	 * Mock {@link ProcessState}.
	 */
	private final ProcessState processState = this
			.createMock(ProcessState.class);

	/**
	 * Flag for {@link MockWorkMetaData} to delegate to mock
	 * {@link WorkMetaData}.
	 */
	private boolean isTesting = false;

	/**
	 * {@link WorkContainer} to test.
	 */
	private final WorkContainer<Work> workContainer = new WorkContainerImpl<Work>(
			this.work, new MockWorkMetaData(), this.processState);

	/**
	 * Mock {@link JobContext}.
	 */
	private final JobContext jobContext = this.createMock(JobContext.class);

	/**
	 * Mock {@link FunctionState}.
	 */
	private final FunctionState jobNode = this.createMock(FunctionState.class);

	/**
	 * {@link ContainerContext}.
	 */
	private final ContainerContext containerContext = this
			.createMock(ContainerContext.class);

	/**
	 * Mock {@link Flow}.
	 */
	private final Flow jobSequence = this.createMock(Flow.class);

	/**
	 * Mock {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * Mock {@link JobNodeActivateSet}.
	 */
	private final JobNodeActivateSet activateSet = this
			.createMock(JobNodeActivateSet.class);

	/**
	 * Ensure can coordinate {@link ManagedObject} instances.
	 */
	public void testCoordinate() {

		// Create the indexes
		ManagedObjectIndex[] indexes = this.createMockManagedObjectIndexes(1);
		ManagedObjectContainer moContainer = this
				.createMock(ManagedObjectContainer.class);

		// Record coordinating
		this.record_WorkContainer_toProcessState();
		this.recordReturn(indexes[0],
				indexes[0].getIndexOfManagedObjectWithinScope(), 0);
		this.recordReturn(indexes[0], indexes[0].getManagedObjectScope(),
				ManagedObjectScope.THREAD);
		this.recordReturn(this.threadState,
				this.threadState.getManagedObjectContainer(0), moContainer);
		this.recordReturn(moContainer, moContainer.coordinateManagedObject(
				workContainer, this.jobContext, this.jobNode, this.activateSet,
				this.containerContext), true);

		// Replay mock
		this.replayMockObjects();

		// Do the coordination
		this.workContainer.coordinateManagedObjects(indexes, this.jobContext,
				this.jobNode, this.activateSet, this.containerContext);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure that on not coordinating a {@link ManagedObject} that returns
	 * immediately and does not coordinate further {@link ManagedObject}
	 * instances.
	 */
	public void testNotCoordinated() {

		// Create the indexes
		ManagedObjectIndex[] indexes = this.createMockManagedObjectIndexes(2);
		ManagedObjectContainer moContainer = this
				.createMock(ManagedObjectContainer.class);

		// Record coordinating
		this.record_WorkContainer_toProcessState();
		this.recordReturn(indexes[0],
				indexes[0].getIndexOfManagedObjectWithinScope(), 0);
		this.recordReturn(indexes[0], indexes[0].getManagedObjectScope(),
				ManagedObjectScope.THREAD);
		this.recordReturn(this.threadState,
				this.threadState.getManagedObjectContainer(0), moContainer);
		this.recordReturn(moContainer, moContainer.coordinateManagedObject(
				workContainer, this.jobContext, this.jobNode, this.activateSet,
				this.containerContext), false);

		// Replay mock
		this.replayMockObjects();

		// Do the coordination
		this.workContainer.coordinateManagedObjects(indexes, this.jobContext,
				this.jobNode, this.activateSet, this.containerContext);

		// Verify
		this.verifyMockObjects();
	}

	/*
	 * =========================== TestCase ============================
	 */

	@Override
	protected void setUp() throws Exception {
		// Flag that now testing
		this.isTesting = true;
	}

	/*
	 * =========================== Helper methods ============================
	 */

	/**
	 * Records obtaining the {@link ThreadState} and {@link ProcessState}.
	 */
	private void record_WorkContainer_toProcessState() {
		this.recordReturn(this.jobNode, this.jobNode.getJobSequence(),
				this.jobSequence);
		this.recordReturn(this.jobSequence, this.jobSequence.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
	}

	/**
	 * Creates an array of mock {@link ManagedObjectIndex} instances.
	 * 
	 * @param numberOfIndexes
	 *            Number of {@link ManagedObjectIndex} instances in the array.
	 * @return Array of mock {@link ManagedObjectIndex} instances.
	 */
	private ManagedObjectIndex[] createMockManagedObjectIndexes(
			int numberOfIndexes) {
		ManagedObjectIndex[] indexes = new ManagedObjectIndex[numberOfIndexes];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = this.createMock(ManagedObjectIndex.class);
		}
		return indexes;
	}

	/**
	 * Mock {@link WorkMetaData}.
	 */
	private class MockWorkMetaData implements WorkMetaData<Work> {

		/*
		 * ==================== WorkMetaData =================================
		 */

		@Override
		public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
			if (WorkContainerTest.this.isTesting) {
				return WorkContainerTest.this.workMetaData
						.getManagedObjectMetaData();
			} else {
				// Creating work container (length only required)
				return new ManagedObjectMetaData[10];
			}
		}

		@Override
		public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
			if (WorkContainerTest.this.isTesting) {
				return WorkContainerTest.this.workMetaData
						.getAdministratorMetaData();
			} else {
				// Creating work container (length only required)
				return new AdministratorMetaData[0];
			}
		}

		@Override
		public WorkContainer<Work> createWorkContainer(ProcessState processState) {
			return WorkContainerTest.this.workMetaData
					.createWorkContainer(processState);
		}

		@Override
		public FlowMetaData<Work> getInitialFlowMetaData() {
			return WorkContainerTest.this.workMetaData.getInitialFlowMetaData();
		}

		@Override
		public TaskMetaData<Work, ?, ?>[] getTaskMetaData() {
			return WorkContainerTest.this.workMetaData.getTaskMetaData();
		}

		@Override
		public WorkFactory<Work> getWorkFactory() {
			return WorkContainerTest.this.workMetaData.getWorkFactory();
		}

		@Override
		public String getWorkName() {
			return WorkContainerTest.this.workMetaData.getWorkName();
		}
	}

}