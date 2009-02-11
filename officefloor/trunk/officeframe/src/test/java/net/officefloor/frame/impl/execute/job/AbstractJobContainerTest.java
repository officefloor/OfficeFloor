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

import org.easymock.internal.AlwaysMatcher;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobActivatableSet;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Contains functionality for testing the {@link AbstractJobContainer}.
 * 
 * @author Daniel
 */
public abstract class AbstractJobContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link JobActivatableSet}.
	 */
	private final JobActivatableSet jobActivatableSet = this
			.createMock(JobActivatableSet.class);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this
			.createMock(ProcessState.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link WorkContainer}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkContainer<Work> workContainer = this
			.createMock(WorkContainer.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow flow = this.createMock(Flow.class);

	/**
	 * {@link JobMetaData}.
	 */
	private final JobMetaData jobMetaData = this.createMock(JobMetaData.class);

	/**
	 * {@link ParallelOwnerJob}.
	 */
	private final ParallelOwnerJob parallelOwnerJob = this
			.createMock(ParallelOwnerJob.class);

	/**
	 * {@link JobContext}.
	 */
	private final JobContext jobContext = this.createMock(JobContext.class);

	/**
	 * Next {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?, ?> nextTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * Next {@link Job}.
	 */
	private final NextJob nextJob = this.createMock(NextJob.class);

	/**
	 * Sequential {@link FlowMetaData}.
	 */
	private final FlowMetaData<?> sequentialFlowMetaData = this
			.createMock(FlowMetaData.class);

	/**
	 * Sequential {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?, ?> sequentialTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * Sequential {@link Job}.
	 */
	private final SequentialJob sequentialJob = this
			.createMock(SequentialJob.class);

	/**
	 * Parallel {@link FlowMetaData}.
	 */
	private final FlowMetaData<?> parallelFlowMetaData = this
			.createMock(FlowMetaData.class);

	/**
	 * Parallel {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?, ?> parallelTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * Parallel {@link Flow}.
	 */
	private final Flow parallelFlow = this.createMock(Flow.class);

	/**
	 * Parallel {@link Job}.
	 */
	private final ParallelJob parallelJob = this.createMock(ParallelJob.class);

	/**
	 * Asynchronous {@link FlowMetaData}.
	 */
	private final FlowMetaData<?> asynchronousFlowMetaData = this
			.createMock(FlowMetaData.class);

	/**
	 * Asynchronous {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?, ?> asynchronousTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * Asynchronous {@link Flow}.
	 */
	private final Flow asynchronousFlow = this.createMock(Flow.class);

	/**
	 * Asynchronous {@link JobNode}.
	 */
	private final JobNode asynchronousJob = this.createMock(JobNode.class);

	/**
	 * Asynchronous {@link ThreadState}.
	 */
	private final ThreadState asynchronousThreadState = this
			.createMock(ThreadState.class);

	/**
	 * {@link Escalation} {@link FlowMetaData}.
	 */
	private final FlowMetaData<?> escalationFlowMetaData = this
			.createMock(FlowMetaData.class);

	/**
	 * {@link Escalation} {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?, ?> escalationTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * {@link Escalation} {@link Flow}.
	 */
	private final Flow escalationFlow = this.createMock(Flow.class);

	/**
	 * {@link Escalation} {@link Job}.
	 */
	private final EscalationJob escalationJob = this
			.createMock(EscalationJob.class);

	/**
	 * Required {@link ManagedObject} indexes.
	 */
	protected int[] lastRequiredManagedObjectIndexes = null;

	/**
	 * Records:
	 * <ol>
	 * <li>obtaining the {@link ThreadState}</li>
	 * <li>obtaining the {@link ProcessState}</li>
	 * <li>
	 * creating the {@link JobActivatableSet}</li>
	 * <li>lock on the {@link ThreadState}</li>
	 * <li>checking the {@link ThreadState} failure</li>
	 * <li>obtaining required {@link ManagedObject} indexes, if no failure</li>
	 * </ol>
	 * <p>
	 * This is always the first steps of executing a
	 * {@link AbstractJobContainer}.
	 * 
	 * @param threadFailure
	 *            {@link ThreadState} failure.
	 * @param requiredManagedObjectIndexes
	 *            Required {@link ManagedObject} indexes.
	 */
	protected void record_JobContainer_initialSteps(Throwable threadFailure,
			int... requiredManagedObjectIndexes) {
		this.recordReturn(this.jobMetaData, this.jobMetaData
				.createJobActivableSet(), this.jobActivatableSet);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.recordReturn(this.threadState, this.threadState.getThreadLock(),
				"Thread lock");
		this.recordReturn(this.threadState, this.threadState.getFailure(),
				threadFailure);
		if (threadFailure != null) {
			// Have failure, so will always clear thread state failure
			this.threadState.setFailure(null);
		} else {
			// No failure, so continue on to obtain managed object indexes
			this.lastRequiredManagedObjectIndexes = requiredManagedObjectIndexes;
			this.recordReturn(this.jobMetaData, this.jobMetaData
					.getRequiredManagedObjects(),
					this.lastRequiredManagedObjectIndexes);
			if (this.lastRequiredManagedObjectIndexes.length > 0) {
				// Has managed objects, so lock on process to initiate them
				this.recordReturn(this.processState, this.processState
						.getProcessLock(), "Process Lock");
			}
		}
	}

	/**
	 * Records loading the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isLoaded
	 *            Indicates if loaded.
	 */
	protected void record_WorkContainer_loadManagedObjects(Job job,
			boolean isLoaded) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.workContainer, this.workContainer
				.loadManagedObjects(this.lastRequiredManagedObjectIndexes,
						this.jobContext, functionalityJob,
						this.jobActivatableSet), isLoaded);
	}

	/**
	 * Records checking {@link ManagedObject} instances are ready.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isReady
	 *            Indicates if ready.
	 */
	protected void record_WorkContainer_isManagedObjectsReady(Job job,
			boolean isReady) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.workContainer, this.workContainer
				.isManagedObjectsReady(this.lastRequiredManagedObjectIndexes,
						this.jobContext, functionalityJob,
						this.jobActivatableSet), isReady);
	}

	/**
	 * Records co-ordinating the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	protected void record_WorkContainer_coordinateManagedObjects(Job job) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.workContainer.coordinateManagedObjects(
				this.lastRequiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet);
	}

	/**
	 * Records obtaining a {@link ManagedObject} object.
	 * 
	 * @param managedObjectIndex
	 *            Index of the {@link ManagedObject}.
	 * @param moObject
	 *            Object to return.
	 */
	protected void record_WorkContainer_getObject(int managedObjectIndex,
			Object moObject) {
		this.recordReturn(this.workContainer, this.workContainer.getObject(
				managedObjectIndex, this.threadState), moObject);
	}

	/**
	 * Records doing a sequential {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param sequentialFlowParameter
	 *            Sequential {@link Flow} parameter.
	 */
	protected void record_doSequentialFlow(Job currentJob,
			Object sequentialFlowParameter) {
		FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this.recordReturn(this.sequentialFlowMetaData,
				this.sequentialFlowMetaData.getInstigationStrategy(),
				FlowInstigationStrategyEnum.SEQUENTIAL);
		this.recordReturn(this.sequentialFlowMetaData,
				this.sequentialFlowMetaData.getInitialTaskMetaData(),
				this.sequentialTaskMetaData);
		this.recordReturn(this.flow, this.flow.createJobNode(
				this.sequentialTaskMetaData, functionalityJob.parallelOwnerJob,
				sequentialFlowParameter), this.sequentialJob);
		this.sequentialJob.activateJob();
	}

	/**
	 * Records doing a parallel {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param parallelFlowParameter
	 *            Parallel {@link Job} parameter.
	 */
	protected void record_doParallelFlow(Job currentJob,
			Object parallelFlowParameter) {
		FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this
				.recordReturn(this.parallelFlowMetaData,
						this.parallelFlowMetaData.getInstigationStrategy(),
						FlowInstigationStrategyEnum.PARALLEL);
		this.recordReturn(this.parallelFlowMetaData, this.parallelFlowMetaData
				.getInitialTaskMetaData(), this.parallelTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState
				.createFlow(this.parallelFlowMetaData), this.parallelFlow);
		this.recordReturn(this.parallelFlow, this.parallelFlow.createJobNode(
				this.parallelTaskMetaData, functionalityJob,
				parallelFlowParameter), this.parallelJob);
		this.parallelJob.setParallelOwner(functionalityJob);
	}

	/**
	 * Records obtaining the {@link TaskMetaData} of the next {@link Task} to
	 * execute.
	 * 
	 * @param hasNextJob
	 *            Flag indicating if have next {@link Job}.
	 */
	protected void record_JobMetaData_getNextTaskInFlow(boolean hasNextJob) {
		// Determine if next task
		TaskMetaData<?, ?, ?, ?> taskMetaData = (hasNextJob ? this.nextTaskMetaData
				: null);
		this.recordReturn(this.jobMetaData, this.jobMetaData
				.getNextTaskInFlow(), taskMetaData);
	}

	/**
	 * Records obtaining the parallel {@link JobNode} from the parallel
	 * {@link Job}.
	 * 
	 * @param parallelNode
	 *            Parallel's {@link Job} parallel {@link JobNode}.
	 */
	protected void record_parallelJob_getParallelNode(JobNode parallelNode) {
		this.recordReturn(this.parallelJob, this.parallelJob.getParallelNode(),
				parallelNode);
	}

	/**
	 * Records activating the parallel {@link Job}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param isComplete
	 *            Flag indicating if the parallel {@link Job} was completed. May
	 *            have been completed by a passive team.
	 * @param nextJob
	 *            Next {@link Job} of the parallel {@link Job}.
	 * @param parallelJob
	 *            Parallel {@link Job} of the parallel {@link Job}.
	 */
	protected void record_parallelJob_activateJob(Job currentJob,
			final boolean isComplete, Job nextJob, Job parallelJob) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this.parallelJob.activateJob();
		this.control(this.parallelJob).setMatcher(new AlwaysMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Completing parallel job will unset parallel node
				if (isComplete) {
					functionalityJob.setParallelNode(null);
				}
				return true;
			}
		});
		if (!isComplete) {
			// Not complete, so will be still linked
			this.recordReturn(this.parallelJob, this.parallelJob
					.isJobNodeComplete(), isComplete);
		}
	}

	/**
	 * Records doing an asynchronous {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param asynchronousFlowParameter
	 *            Asynchronous {@link Job} parameter.
	 */
	protected void record_doAsynchronousFlow(Job currentJob,
			Object asynchronousFlowParameter) {
		this.recordReturn(this.asynchronousFlowMetaData,
				this.asynchronousFlowMetaData.getInstigationStrategy(),
				FlowInstigationStrategyEnum.ASYNCHRONOUS);
		this.recordReturn(this.asynchronousFlowMetaData,
				this.asynchronousFlowMetaData.getInitialTaskMetaData(),
				this.asynchronousTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.recordReturn(this.processState, this.processState
				.createThread(this.asynchronousFlowMetaData),
				this.asynchronousFlow);
		this.recordReturn(this.asynchronousFlow, this.asynchronousFlow
				.createJobNode(this.asynchronousTaskMetaData, null,
						asynchronousFlowParameter), this.asynchronousJob);
		this.asynchronousJob.activateJob();
		this.recordReturn(this.asynchronousFlow, this.asynchronousFlow
				.getThreadState(), this.asynchronousThreadState);
	}

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure = this
			.createMock(EscalationProcedure.class);

	/**
	 * {@link Escalation}.
	 */
	private final Escalation escalation = this.createMock(Escalation.class);

	/**
	 * Records getting the {@link Escalation}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 * @param isSpecificallyHandled
	 *            Flag indicating if handled specifically.
	 */
	protected void record_JobContainer_getEscalation(Job job,
			Throwable failure, boolean isSpecificallyHandled) {
		this.recordReturn(this.jobMetaData, this.jobMetaData
				.getEscalationProcedure(), this.escalationProcedure);
		this.recordReturn(this.escalationProcedure, this.escalationProcedure
				.getEscalation(failure),
				(isSpecificallyHandled ? this.escalation : null));
		if (!isSpecificallyHandled) {
			// No specifically handled, so must use catch all escalation
			this.recordReturn(this.processState, this.processState
					.getCatchAllEscalation(), this.escalation);
		}
	}

	/**
	 * Records handling an {@link Escalation} that resets the
	 * {@link ThreadState}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 */
	protected void record_JobContainer_handleResetEscalation(Job job,
			Throwable failure) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.escalation,
				this.escalation.isResetThreadState(), true);
		this.threadState.escalationStart(functionalityJob, true,
				this.jobActivatableSet);
		this.recordReturn(this.escalation, this.escalation.getFlowMetaData(),
				this.escalationFlowMetaData);
		this.recordReturn(this.escalationFlowMetaData,
				this.escalationFlowMetaData.getInitialTaskMetaData(),
				this.escalationTaskMetaData);
		this
				.recordReturn(this.flow, this.flow.createJobNode(
						this.escalationTaskMetaData, null, failure),
						this.escalationJob);
		this.threadState.escalationComplete(functionalityJob,
				this.jobActivatableSet);
		this.escalationJob.activateJob();
	}

	/**
	 * Records handling an {@link Escalation} that does not reset the
	 * {@link ThreadState}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 * @param isEscalationComplate
	 *            Flags if the {@link Escalation} parallel {@link Job} completes
	 *            passively.
	 */
	protected void record_JobContainer_handleNotResetEscalation(Job job,
			Throwable failure, final boolean isEscalationComplate) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.escalation,
				this.escalation.isResetThreadState(), false);
		this.threadState.escalationStart(functionalityJob, false,
				this.jobActivatableSet);
		this.recordReturn(this.escalation, this.escalation.getFlowMetaData(),
				this.escalationFlowMetaData);
		this.recordReturn(this.escalationFlowMetaData,
				this.escalationFlowMetaData.getInitialTaskMetaData(),
				this.escalationTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState
				.createFlow(this.escalationFlowMetaData), this.escalationFlow);
		this.recordReturn(this.escalationFlow, this.escalationFlow
				.createJobNode(this.escalationTaskMetaData, functionalityJob,
						failure), this.escalationJob);
		this.escalationJob.setParallelOwner(functionalityJob);
		this.recordReturn(this.escalationJob, this.escalationJob
				.getParallelNode(), null);
		this.escalationJob.activateJob();
		this.control(this.escalationJob).setMatcher(new AlwaysMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Clears parallel node on job if completes
				if (isEscalationComplate) {
					functionalityJob.setParallelNode(null);
				}
				return true;
			}
		});
		if (!isEscalationComplate) {
			// Not complete, so will still be linked
			this.recordReturn(this.escalationJob, this.escalationJob
					.isJobNodeComplete(), isEscalationComplate);
		}
		this.threadState.escalationComplete(functionalityJob,
				this.jobActivatableSet);
	}

	/**
	 * Records creating a {@link Job}.
	 * 
	 * @param currentJob
	 *            Current {@link Job} creating the new {@link Job}.
	 * @param nextJobParameter
	 *            Parameter for next {@link Job}.
	 */
	protected void record_Flow_createJob(Job currentJob, Object nextJobParameter) {
		FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this.recordReturn(this.flow, this.flow.createJobNode(
				this.nextTaskMetaData, functionalityJob.parallelOwnerJob,
				nextJobParameter), this.nextJob);
	}

	/**
	 * Records activating the next {@link Job}.
	 */
	protected void record_nextJob_activateJob() {
		this.nextJob.activateJob();
	}

	/**
	 * Records setting the parallel {@link JobNode} for the Parallel Owner.
	 */
	protected void record_ParallelOwner_unlinkAndActivate() {
		this.parallelOwnerJob.setParallelNode(null);
		this.parallelOwnerJob.activateJob();
	}

	/**
	 * Records completing the {@link Job}.
	 * 
	 * @param job
	 *            {@link Job} being completed.
	 */
	protected void record_completeJob(Job job) {
		// Obtain process lock as clean up may interact with ProcessState
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.recordReturn(this.processState,
				this.processState.getProcessLock(), "Process Lock");

		// Clean up job
		this.workContainer.unloadWork();
		this.flow.jobComplete(job, this.jobActivatableSet);
	}

	/**
	 * Records activating the {@link JobActivatableSet}.
	 */
	protected void record_JobActivatableSet_activateJobs() {
		this.jobActivatableSet.activateJobs();
	}

	/**
	 * Creates the {@link Job} for the {@link JobFunctionality}.
	 * 
	 * @param hasParallelOwnerJob
	 *            Flag indicating if to have a parallel owner {@link JobNode}.
	 * @param jobFunctionality
	 *            {@link JobFunctionality} instances.
	 * @return {@link Job}.
	 */
	protected FunctionalityJob createJob(boolean hasParallelOwnerJob,
			JobFunctionality... jobFunctionality) {
		// Obtain the parallel owner job
		ParallelOwnerJob owner = (hasParallelOwnerJob ? this.parallelOwnerJob
				: null);

		// Return the created functionality job
		return new FunctionalityJob(owner, jobFunctionality);
	}

	/**
	 * Executes the {@link FunctionalityJob}.
	 * 
	 * @param job
	 *            {@link FunctionalityJob}.
	 * @param isExpectedComplete
	 *            If {@link Job} should be complete.
	 */
	protected void doJob(Job job, boolean isExpectedComplete) {
		boolean isComplete = job.doJob(this.jobContext);
		assertEquals("Incorrect return state of doJob", isExpectedComplete,
				isComplete);
	}

	/**
	 * Asserts that the {@link Job} was executed.
	 * 
	 * @param job
	 *            {@link Job} to assert was executed.
	 */
	protected static void assertJobExecuted(Job job) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		assertTrue("Job is expected to be executed",
				functionalityJob.isJobExecuted);
	}

	/**
	 * Asserts that the {@link Job} is not executed.
	 * 
	 * @param job
	 *            {@link Job} to assert is not executed.
	 */
	protected static void assertJobNotExecuted(Job job) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		assertFalse("Job is expected to not be executed",
				functionalityJob.isJobExecuted);
	}

	/**
	 * {@link Job} that delegates to a {@link JobFunctionality} to provide
	 * functionality.
	 */
	protected class FunctionalityJob extends
			AbstractJobContainer<Work, JobMetaData> implements
			JobFunctionalityContext {

		/**
		 * {@link JobFunctionality}.
		 */
		private final JobFunctionality[] jobFunctionality;

		/**
		 * {@link ParallelOwnerJob} for this {@link Job}.
		 */
		private final JobNode parallelOwnerJob;

		/**
		 * Flag indicating if the {@link Job} is executed.
		 */
		private boolean isJobExecuted = false;

		/**
		 * Initiate.
		 * 
		 * @param parallelOwnerJob
		 *            Parallel Owner {@link JobNode}.
		 * @param jobFunctionality
		 *            {@link JobFunctionality}.
		 */
		public FunctionalityJob(JobNode parallelOwnerJob,
				JobFunctionality... jobFunctionality) {
			super(AbstractJobContainerTest.this.flow,
					AbstractJobContainerTest.this.workContainer,
					AbstractJobContainerTest.this.jobMetaData, parallelOwnerJob);
			this.parallelOwnerJob = parallelOwnerJob;
			this.jobFunctionality = jobFunctionality;
		}

		/*
		 * ==================== JobContainer ==============================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.impl.execute.JobContainer#executeJob(net.
		 * officefloor.frame.impl.execute.JobExecuteContext)
		 */
		@Override
		protected Object executeJob(JobExecuteContext context) throws Throwable {
			// Indicate the job is executed
			this.isJobExecuted = true;

			// Execute the functionality (using result of last as parameter)
			Object parameter = null;
			for (JobFunctionality functionality : this.jobFunctionality) {
				parameter = functionality.executeFunctionality(this);
			}

			// Return the parameter
			return parameter;
		}

		/*
		 * ==================== JobFunctionalityContext ===================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.impl.execute.job.JobFunctionalityContext#getObject
		 * (int)
		 */
		@Override
		public Object getObject(int managedObjectIndex) {
			return this.workContainer.getObject(managedObjectIndex,
					AbstractJobContainerTest.this.threadState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.impl.execute.job.JobFunctionalityContext#doFlow
		 * (int,
		 * net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum,
		 * java.lang.Object)
		 */
		@Override
		public FlowFuture doFlow(int flowIndex,
				FlowInstigationStrategyEnum instigationStrategy,
				Object parameter) {

			// Obtain the Flow meta-data
			FlowMetaData<?> flowMetaData;
			switch (instigationStrategy) {
			case SEQUENTIAL:
				flowMetaData = AbstractJobContainerTest.this.sequentialFlowMetaData;
				break;
			case PARALLEL:
				flowMetaData = AbstractJobContainerTest.this.parallelFlowMetaData;
				break;
			case ASYNCHRONOUS:
				flowMetaData = AbstractJobContainerTest.this.asynchronousFlowMetaData;
				break;
			default:
				fail("Unknown instigation strategy " + instigationStrategy);
				return null;
			}

			// Do the flow
			return this.doFlow(flowMetaData, parameter);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.impl.execute.job.AbstractJobContainerTest.
		 * JobFunctionalityContext#setComplete(boolean)
		 */
		@Override
		public void setComplete(boolean isComplete) {
			this.setJobComplete(isComplete);
		}
	}

	/**
	 * Functionality for testing the {@link Job}.
	 */
	protected static interface JobFunctionality {

		/**
		 * Executes the functionality for the {@link Job}.
		 * 
		 * @param context
		 *            {@link JobFunctionalityContext}.
		 * @return Result of functionality, that is used as a parameter to the
		 *         next {@link Job}.
		 * @throws Throwable
		 *             If failure of functionality.
		 */
		Object executeFunctionality(JobFunctionalityContext context)
				throws Throwable;
	}

	/**
	 * Context for the {@link JobFunctionality}.
	 */
	protected static interface JobFunctionalityContext {

		/**
		 * Obtains the {@link ManagedObject} instance's object.
		 * 
		 * @param managedObjectIndex
		 *            Index of the {@link ManagedObject} on the {@link Job}.
		 * @return Object of the {@link ManagedObject}.
		 */
		Object getObject(int managedObjectIndex);

		/**
		 * Invokes a {@link Flow}.
		 * 
		 * @param flowIndex
		 *            Index of the {@link Flow} to invoke.
		 * @param instigationStrategy
		 *            {@link FlowInstigationStrategyEnum}.
		 * @param parameter
		 *            Parameter for the first {@link Job} of the invoked
		 *            {@link Flow}.
		 * @return {@link FlowFuture} for the invoked {@link Flow}.
		 */
		FlowFuture doFlow(int flowIndex,
				FlowInstigationStrategyEnum instigationStrategy,
				Object parameter);

		/**
		 * Flags whether the {@link Job} is complete, or requires re-invoking.
		 * 
		 * @param isComplete
		 *            <code>true</code> if complete.
		 */
		void setComplete(boolean isComplete);
	}

	/**
	 * Parallel owner {@link Job}.
	 */
	private static interface ParallelOwnerJob extends Job, JobNode {
	}

	/**
	 * Next {@link Job}.
	 */
	private static interface NextJob extends Job, JobNode {
	}

	/**
	 * Sequential {@link Job}.
	 */
	private static interface SequentialJob extends Job, JobNode {
	}

	/**
	 * Parallel {@link Job}.
	 */
	private static interface ParallelJob extends Job, JobNode {
	}

	/**
	 * {@link Escalation} {@link Job}.
	 */
	private static interface EscalationJob extends Job, JobNode {
	}
}
