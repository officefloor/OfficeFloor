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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.JobActivateSetImpl;
import net.officefloor.frame.impl.execute.JobExecuteContext;
import net.officefloor.frame.impl.execute.JobContainer;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.ParameterMatcher;

/**
 * Tests the {@link JobContainer}.
 * 
 * @author Daniel
 */
public class JobContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link ThreadState}.
	 */
	private ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link Flow}.
	 */
	private Flow flow = this.createMock(Flow.class);

	/**
	 * {@link ThreadWorkLink}.
	 */
	@SuppressWarnings("unchecked")
	private ThreadWorkLink<Work> workLink = this
			.createMock(ThreadWorkLink.class);

	/**
	 * {@link JobMetaData}.
	 */
	private JobMetaData jobMetaData = this.createMock(JobMetaData.class);

	/**
	 * Parallel owning {@link JobNode}.
	 */
	private ParallelOwnerJob parallelOwner = this
			.createMock(ParallelOwnerJob.class);

	/**
	 * {@link JobContext}.
	 */
	private JobContext jobContext = this.createMock(JobContext.class);

	/**
	 * Lock for {@link ThreadState}.
	 */
	private Object threadLock = new Object();

	/**
	 * Required {@link ManagedObject} indexes.
	 */
	private int[] requiredManagedObjects = new int[] { 1, 2 };

	/**
	 * {@link ProcessState}.
	 */
	private ProcessState processState = this.createMock(ProcessState.class);

	/**
	 * Lock for {@link ProcessState}.
	 */
	private Object processLock = new Object();

	/**
	 * {@link WorkContainer}.
	 */
	@SuppressWarnings("unchecked")
	private WorkContainer<Work> workContainer = this
			.createMock(WorkContainer.class);

	/**
	 * Ensures execution of {@link Job}.
	 */
	public void testExecuteJob() {

		// Create the job to test
		Job job = new MockJobImpl(this.threadState, this.flow, this.workLink,
				this.jobMetaData, null);

		// Record execution
		this.record_getThreadLock();
		this.record_failureEscalation(null);
		this.record_getRequiredManagedObjects();
		this.record_getProcessLock();
		this.record_loadManagedObjects(job, true);
		this.record_isManagedObjectsReady(job, true);
		this.record_coordinateManagedObjects(job);
		this.record_isManagedObjectsReady(job, true);
		// (execute job occurs)
		this.record_getNextTaskInFlow(null);
		this.record_completeJob(job);

		// Test execution of job
		this.replayMockObjects();
		assertTrue(job.doJob(this.jobContext));
		this.verifyMockObjects();
	}

	/**
	 * Ensures execution of {@link Job} invokes parallel owner.
	 */
	public void testExecuteJobWithParallelOwner() {

		// Create the job to test
		Job job = new MockJobImpl(this.threadState, this.flow, this.workLink,
				this.jobMetaData, this.parallelOwner);

		// Record execution
		this.record_getThreadLock();
		this.record_failureEscalation(null);
		this.record_getRequiredManagedObjects();
		this.record_getProcessLock();
		this.record_loadManagedObjects(job, true);
		this.record_isManagedObjectsReady(job, true);
		this.record_coordinateManagedObjects(job);
		this.record_isManagedObjectsReady(job, true);
		// (execute job occurs)
		this.record_getNextTaskInFlow(null);
		this.record_activateParallelOwner();
		this.record_completeJob(job);

		// Test execution of job
		this.replayMockObjects();
		assertTrue(job.doJob(this.jobContext));
		this.verifyMockObjects();
	}

	/*
	 * ==================================================================================
	 * Helper methods and classes for testing
	 * ==================================================================================
	 */

	/**
	 * Records obtaining the {@link ThreadState} lock.
	 */
	private void record_getThreadLock() {
		this.recordReturn(this.threadState, this.threadState.getThreadLock(),
				this.threadLock);
	}

	/**
	 * Records escalation of failure.
	 * 
	 * @param failure
	 *            Failure or <code>null</code> if no failure.
	 */
	private void record_failureEscalation(Throwable failure) {
		this.recordReturn(this.threadState, this.threadState.getFailure(),
				failure);
	}

	/**
	 * Records obtaining the required {@link ManagedObject} indexes.
	 */
	private void record_getRequiredManagedObjects() {
		this.recordReturn(this.jobMetaData, this.jobMetaData
				.getRequiredManagedObjects(), this.requiredManagedObjects);
	}

	/**
	 * Records obtaining the {@link ProcessState} lock.
	 */
	private void record_getProcessLock() {
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.recordReturn(this.processState,
				this.processState.getProcessLock(), this.processLock);
	}

	/**
	 * Records obtaining the {@link WorkContainer}.
	 */
	private void record_getWorkContainer() {
		this.recordReturn(this.workLink, this.workLink.getWorkContainer(),
				this.workContainer);
	}

	/**
	 * Records loading the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isLoadedReturn
	 *            Return from method.
	 */
	private void record_loadManagedObjects(Job job, boolean isLoadedReturn) {
		this.record_getWorkContainer();
		this.recordReturn(this.workContainer, this.workContainer
				.loadManagedObjects(this.requiredManagedObjects,
						this.jobContext, job, new JobActivateSetImpl()),
				isLoadedReturn, new ParameterMatcher(ParameterMatcher.equals,
						ParameterMatcher.equals, ParameterMatcher.equals,
						ParameterMatcher.type));
	}

	/**
	 * Records co-ordinating the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	private void record_coordinateManagedObjects(Job job) {
		this.record_getWorkContainer();
		this.workContainer.coordinateManagedObjects(
				this.requiredManagedObjects, this.jobContext, job,
				new JobActivateSetImpl());
		this.control(this.workContainer).setMatcher(
				new ParameterMatcher(ParameterMatcher.equals,
						ParameterMatcher.equals, ParameterMatcher.equals,
						ParameterMatcher.type));
	}

	/**
	 * May only set the matcher once.
	 */
	private boolean isManagedObjectsReadyMatcherSet = false;

	/**
	 * Record is ready on {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isReadyReturn
	 *            Return from method.
	 */
	private void record_isManagedObjectsReady(Job job, boolean isReadyReturn) {
		this.record_getWorkContainer();
		this.workContainer.isManagedObjectsReady(this.requiredManagedObjects,
				this.jobContext, job, new JobActivateSetImpl());
		if (!this.isManagedObjectsReadyMatcherSet) {
			this.control(this.workContainer).setMatcher(
					new ParameterMatcher(ParameterMatcher.equals,
							ParameterMatcher.equals, ParameterMatcher.equals,
							ParameterMatcher.type));
			this.isManagedObjectsReadyMatcherSet = true;
		}
		this.control(this.workContainer).setReturnValue(isReadyReturn);
	}

	/**
	 * Records getting the next {@link Task} in {@link Flow}.
	 */
	private void record_getNextTaskInFlow(TaskMetaData<?, ?, ?, ?> taskMetaData) {
		this.recordReturn(this.jobMetaData, this.jobMetaData
				.getNextTaskInFlow(), taskMetaData);
	}

	/**
	 * Records activating the parallel owner {@link Job}.
	 */
	private void record_activateParallelOwner() {
		this.parallelOwner.setParallelNode(null); // unlink from owner
		this.parallelOwner.activateJob();
	}

	/**
	 * Records completing the {@link Job}.
	 */
	private void record_completeJob(Job job) {
		this.flow.jobComplete(job, new JobActivateSetImpl());
		this.control(this.flow).setMatcher(
				new ParameterMatcher(ParameterMatcher.equals,
						ParameterMatcher.type));
		this.workLink.unregisterJob(job);
	}

	/**
	 * Mock {@link JobContainer}.
	 */
	private class MockJobImpl extends JobContainer<Work, JobMetaData> {

		/**
		 * Initiate.
		 * 
		 * @param threadState
		 *            {@link ThreadState}.
		 * @param flow
		 *            {@link Flow}.
		 * @param workLink
		 *            {@link ThreadWorkLink}.
		 * @param nodeMetaData
		 *            {@link JobMetaData}.
		 * @param parallelOwner
		 *            Parallel owning {@link JobNode}.
		 */
		public MockJobImpl(ThreadState threadState, Flow flow,
				ThreadWorkLink<Work> workLink, JobMetaData nodeMetaData,
				JobNode parallelOwner) {
			super(threadState, flow, workLink, nodeMetaData, parallelOwner);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.impl.execute.JobImpl#executeJob(net.officefloor.frame.impl.execute.JobExecuteContext)
		 */
		@Override
		protected Object executeJob(JobExecuteContext context) {

			// TODO test execute job
			System.err.println("TODO test execute job");

			return null;
		}
	}

	/**
	 * Parallel owner.
	 */
	private interface ParallelOwnerJob extends Job, JobNode {
	}
}
