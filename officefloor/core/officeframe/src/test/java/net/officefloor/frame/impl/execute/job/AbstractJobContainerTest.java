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
package net.officefloor.frame.impl.execute.job;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.jobnode.AbstractManagedJobNodeContainer;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowAsset;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedJobNodeContext;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

/**
 * Contains functionality for testing the {@link AbstractManagedJobNodeContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJobContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link JobNodeActivatableSet}.
	 */
	private final JobNodeActivatableSet jobActivatableSet = this
			.createMock(JobNodeActivatableSet.class);

	/**
	 * {@link TeamIdentifier} of the current {@link Team}.
	 */
	private final TeamIdentifier currentTeam = this
			.createMock(TeamIdentifier.class);

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
	private final TaskMetaData<?, ?, ?> nextTaskMetaData = this
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
	private final TaskMetaData<?, ?, ?> sequentialTaskMetaData = this
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
	private final TaskMetaData<?, ?, ?> parallelTaskMetaData = this
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
	 * Asynchronous {@link AssetManager}.
	 */
	private final AssetManager asynchronousFlowAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * Asynchronous {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?> asynchronousTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * Asynchronous {@link Flow}.
	 */
	private final Flow asynchronousFlow = this
			.createMock(Flow.class);

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
	 * {@link EscalationFlow} {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?> escalationTaskMetaData = this
			.createMock(TaskMetaData.class);

	/**
	 * {@link EscalationFlow} {@link Flow}.
	 */
	private final Flow escalationFlow = this
			.createMock(Flow.class);

	/**
	 * {@link EscalationFlow} {@link Job}.
	 */
	private final EscalationJob escalationJob = this
			.createMock(EscalationJob.class);

	/**
	 * Records:
	 * <ol>
	 * <li>obtaining the {@link ThreadState}</li>
	 * <li>obtaining the {@link ProcessState}</li>
	 * <li>
	 * creating the {@link JobNodeActivatableSet}</li>
	 * <li>lock on the {@link ThreadState}</li>
	 * <li>checking the {@link ThreadState} failure</li>
	 * <li>obtaining required {@link ManagedObject} indexes, if no failure</li>
	 * </ol>
	 * <p>
	 * This is always the first steps of executing a
	 * {@link AbstractManagedJobNodeContainer}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param threadFailure
	 *            {@link ThreadState} failure.
	 */
	protected void record_JobContainer_initialSteps(Job job,
			Throwable threadFailure) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.jobContext, this.jobContext.getCurrentTeam(),
				this.currentTeam);
		this.recordReturn(this.jobMetaData,
				this.jobMetaData.createJobActivableSet(),
				this.jobActivatableSet);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.recordReturn(this.threadState, this.threadState.getThreadLock(),
				"Thread lock");
		this.threadState.profile(this.jobMetaData);
		this.recordReturn(this.threadState, this.threadState.getFailure(),
				threadFailure);
		if (threadFailure != null) {
			// Have failure, so will always clear thread state failure
			this.threadState.setFailure(null);
		} else {
			// No failure, so continue on to obtain managed object indexes
			if (functionalityJob.requiredManagedObjectIndexes.length > 0) {
				// Has managed objects, so lock on process to initiate them
				this.recordReturn(this.processState,
						this.processState.getProcessLock(), "Process Lock");
			}
		}
	}

	/**
	 * Records activating the {@link Governance}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param currentGovernanceState
	 *            Current state of {@link Governance} identifying which is
	 *            active.
	 */
	protected void record_JobContainer_activateGovernance(Job job,
			boolean... currentGovernanceState) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;

		// Run through governance
		for (int i = 0; i < functionalityJob.requiredGovernance.length; i++) {
			boolean isRequireGovernance = functionalityJob.requiredGovernance[i];

			// Determine current activation state (defaulty not active)
			boolean isCurrentlyActive = (i < currentGovernanceState.length ? currentGovernanceState[i]
					: false);

			// Record whether active
			this.recordReturn(this.threadState,
					this.threadState.isGovernanceActive(i), isCurrentlyActive);

			// Handle changing activation of Governance
			if (isRequireGovernance != isCurrentlyActive) {

				// Obtain the Governance Container
				final GovernanceContainer<?, ?> container = this
						.createMock(GovernanceContainer.class);
				this.recordReturn(this.threadState,
						this.threadState.getGovernanceContainer(i), container);

				// Change activation state of Governance
				if (isRequireGovernance) {
					// Activate Governance
					container.activateGovernance(functionalityJob);

				} else {
					// Deactivate Governance
					switch (functionalityJob.deactivationStrategy) {
					case ENFORCE:
						container.enforceGovernance(functionalityJob);
						break;
					case DISREGARD:
						container.disregardGovernance(functionalityJob);
						break;
					default:
						fail("Unknown governance deactivation strategy "
								+ functionalityJob.deactivationStrategy);
					}
				}
			}
		}
	}

	/**
	 * Records loading the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	protected void record_WorkContainer_loadManagedObjects(Job job) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.workContainer.loadManagedObjects(
				functionalityJob.requiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet, this.currentTeam,
				functionalityJob);
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
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.workContainer, this.workContainer
				.isManagedObjectsReady(
						functionalityJob.requiredManagedObjectIndexes,
						this.jobContext, functionalityJob,
						this.jobActivatableSet, functionalityJob), isReady);
	}

	/**
	 * Records governing the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isSetupTask
	 *            Indicates to add a setup {@link Task}.
	 * @param isGovernanceActivity
	 *            Indicates to add a {@link GovernanceActivity}.
	 */
	protected void record_WorkContainer_governManagedObjects(Job job,
			boolean isSetupTask, boolean isGovernanceActivity) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;

		// Record governing managed objects
		this.workContainer.governManagedObjects(
				functionalityJob.requiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet, functionalityJob);

		if (isSetupTask) {

			final TaskMetaData<?, ?, ?> setuptaskMetaData = this
					.createMock(TaskMetaData.class);
			final Object parameter = new Object();

			// Trigger setup task from govern managed objects
			this.control(this.workContainer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {

					// Ensure correct parameters
					for (int i = 0; i < expected.length; i++) {
						assertEquals("Incorrect parameter " + i, expected[i],
								actual[i]);
					}

					// Trigger setup task
					ContainerContext context = (ContainerContext) actual[4];
					context.addSetupTask(setuptaskMetaData, parameter);

					return true;
				}
			});

			// Record adding the setup task
			this.record_ContainerContext_addSetupTask(job, setuptaskMetaData,
					parameter);

		} else if (isGovernanceActivity) {

			final GovernanceActivity<?, ?> governanceActivity = this
					.createMock(GovernanceActivity.class);

			// Trigger governance activity from govern managed objects
			this.control(this.workContainer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {

					// Ensure correct parameters
					for (int i = 0; i < expected.length; i++) {
						assertEquals("Incorrect parameter " + i, expected[i],
								actual[i]);
					}

					// Trigger governance activity
					ContainerContext context = (ContainerContext) actual[4];
					context.addGovernanceActivity(governanceActivity);

					return true;
				}
			});

			// Record adding the governance activity
			this.record_ContainerContext_addGovernanceActivity(job,
					governanceActivity);
		}
	}

	/**
	 * List of results from coordinate {@link ManagedObject}.
	 */
	private List<Boolean> coordinateJobsToWait = null;

	/**
	 * Records coordinating the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isCoordinated
	 *            Indicates if coordinated.
	 */
	protected void record_WorkContainer_coordinateManagedObjects(Job job,
			final boolean isCoordinated) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.workContainer.coordinateManagedObjects(
				functionalityJob.requiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet, functionalityJob);

		// Ensure appropriately flags for job to wait
		if (this.coordinateJobsToWait == null) {
			this.coordinateJobsToWait = new LinkedList<Boolean>();
			this.control(this.workContainer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					for (int i = 0; i < expected.length; i++) {
						assertEquals("Incorrect argument " + i, expected[i],
								actual[i]);
					}
					Boolean isCoordinate = AbstractJobContainerTest.this.coordinateJobsToWait
							.remove(0);
					if (!isCoordinate.booleanValue()) {
						// Not coordinated so flag to wait
						functionalityJob.flagJobToWait();
					}
					return true;
				}
			});
		}
		this.coordinateJobsToWait.add(Boolean.valueOf(isCoordinated));
	}

	/**
	 * Records obtaining a {@link ManagedObject} object.
	 * 
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} of the {@link ManagedObject}.
	 * @param moObject
	 *            Object to return.
	 */
	protected void record_WorkContainer_getObject(
			ManagedObjectIndex managedObjectIndex, Object moObject) {
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
	 * @param isActivateFlow
	 *            Flag indicating if required to activate the first
	 *            {@link JobNode} of the instigated sequential
	 *            {@link Flow}.
	 */
	protected void record_doSequentialFlow(Job currentJob,
			Object sequentialFlowParameter, boolean isActivateFlow) {
		FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this.recordReturn(this.sequentialFlowMetaData,
				this.sequentialFlowMetaData.getInstigationStrategy(),
				FlowInstigationStrategyEnum.SEQUENTIAL);
		this.recordReturn(this.sequentialFlowMetaData,
				this.sequentialFlowMetaData.getInitialTaskMetaData(),
				this.sequentialTaskMetaData);
		this.recordReturn(this.flow, this.flow
				.createTaskNode(this.sequentialTaskMetaData,
						functionalityJob.parallelOwnerJob,
						sequentialFlowParameter,
						GovernanceDeactivationStrategy.ENFORCE),
				this.sequentialJob);
		if (isActivateFlow) {
			this.sequentialJob.activateJob(this.currentTeam);
		}
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
		this.recordReturn(this.parallelFlowMetaData,
				this.parallelFlowMetaData.getInstigationStrategy(),
				FlowInstigationStrategyEnum.PARALLEL);
		this.recordReturn(this.parallelFlowMetaData,
				this.parallelFlowMetaData.getInitialTaskMetaData(),
				this.parallelTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState,
				this.threadState.createJobSequence(), this.parallelFlow);
		this.recordReturn(this.parallelFlow, this.parallelFlow.createTaskNode(
				this.parallelTaskMetaData, functionalityJob,
				parallelFlowParameter, GovernanceDeactivationStrategy.ENFORCE),
				this.parallelJob);
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
		TaskMetaData<?, ?, ?> taskMetaData = (hasNextJob ? this.nextTaskMetaData
				: null);
		this.recordReturn(this.jobMetaData,
				this.jobMetaData.getNextTaskInFlow(), taskMetaData);
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
			final boolean isComplete) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this.parallelJob.activateJob(this.currentTeam);
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
			this.recordReturn(this.parallelJob,
					this.parallelJob.isJobNodeComplete(), isComplete);
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
		this.recordReturn(this.asynchronousFlowMetaData,
				this.asynchronousFlowMetaData.getFlowManager(),
				this.asynchronousFlowAssetManager);
		this.recordReturn(this.processState, this.processState
				.createThread(this.asynchronousFlowAssetManager),
				this.asynchronousFlow);
		this.recordReturn(this.asynchronousFlow, this.asynchronousFlow
				.createTaskNode(this.asynchronousTaskMetaData, null,
						asynchronousFlowParameter,
						GovernanceDeactivationStrategy.ENFORCE),
				this.asynchronousJob);
		this.asynchronousJob
				.activateJob(AbstractManagedJobNodeContainer.ASYNCHRONOUS_FLOW_TEAM_IDENTIFIER);
		this.recordReturn(this.asynchronousFlow,
				this.asynchronousFlow.getThreadState(),
				this.asynchronousThreadState);
	}

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure = this
			.createMock(EscalationProcedure.class);

	/**
	 * {@link EscalationFlow}.
	 */
	private final EscalationFlow escalation = this
			.createMock(EscalationFlow.class);

	/**
	 * Records handling an {@link EscalationFlow}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 * @param isHandled
	 *            Flag indicating if the {@link EscalationFlow} is handled.
	 */
	protected void record_JobContainer_handleEscalation(Job job,
			Throwable failure, boolean isHandled) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.threadState.escalationStart(functionalityJob,
				this.jobActivatableSet);
		this.record_completeJob(job);
		this.recordReturn(this.jobMetaData,
				this.jobMetaData.getEscalationProcedure(),
				this.escalationProcedure);
		EscalationFlow escalation = (isHandled ? this.escalation : null);
		if (functionalityJob.parallelOwnerJob == null) {
			// No parallel owner, so handle by job
			this.recordReturn(this.escalationProcedure,
					this.escalationProcedure.getEscalation(failure), escalation);
		} else {
			// Parallel owner, so handle by job
			this.recordReturn(this.escalationProcedure,
					this.escalationProcedure.getEscalation(failure), null);
			this.recordReturn(functionalityJob.parallelOwnerJob,
					functionalityJob.parallelOwnerJob.getEscalationProcedure(),
					this.escalationProcedure);
			this.recordReturn(this.escalationProcedure,
					this.escalationProcedure.getEscalation(failure), escalation);
			this.recordReturn(functionalityJob.parallelOwnerJob,
					functionalityJob.parallelOwnerJob.getParallelOwner(), null);
		}
		if (isHandled) {
			this.record_JobContainer_createEscalationJob(failure,
					functionalityJob.parallelOwnerJob);
		}
		this.threadState.escalationComplete(functionalityJob,
				this.jobActivatableSet);
		if (isHandled) {
			this.escalationJob.activateJob(this.currentTeam);
		}
	}

	/**
	 * Records handling an {@link EscalationFlow}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 * @param handledLevel
	 *            {@link EscalationLevel} that handles escalation.
	 */
	protected void record_JobContainer_globalEscalation(Job job,
			Throwable failure, EscalationLevel handledLevel) {
		this.recordReturn(this.threadState,
				this.threadState.getEscalationLevel(), EscalationLevel.FLOW);

		// Handled by the office escalation procedure
		this.recordReturn(this.processState,
				this.processState.getOfficeEscalationProcedure(),
				this.escalationProcedure);
		if (handledLevel == EscalationLevel.OFFICE) {
			this.recordReturn(this.escalationProcedure,
					this.escalationProcedure.getEscalation(failure),
					this.escalation);
			this.threadState.setEscalationLevel(EscalationLevel.OFFICE);
			this.record_JobContainer_createEscalationJob(failure, null);
			this.escalationJob.activateJob(this.currentTeam);
			return;
		}
		this.recordReturn(this.escalationProcedure,
				this.escalationProcedure.getEscalation(failure), null);

		// Handled by the managed object source escalation handler
		if (handledLevel == EscalationLevel.INVOCATION_HANDLER) {
			this.recordReturn(this.processState,
					this.processState.getInvocationEscalation(),
					this.escalation);
			this.threadState
					.setEscalationLevel(EscalationLevel.INVOCATION_HANDLER);
			this.record_JobContainer_createEscalationJob(failure, null);
			this.escalationJob.activateJob(this.currentTeam);
			return;
		}
		this.recordReturn(this.processState,
				this.processState.getInvocationEscalation(), null);

		// Handled by the office floor escalation
		this.recordReturn(this.processState,
				this.processState.getOfficeFloorEscalation(), this.escalation);
		this.threadState.setEscalationLevel(EscalationLevel.OFFICE_FLOOR);
		this.record_JobContainer_createEscalationJob(failure, null);
		this.escalationJob.activateJob(this.currentTeam);
	}

	/**
	 * Records the creation of the {@link EscalationFlow} {@link JobNode}.
	 * 
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 * @param parallelOwner
	 *            Parallel owner of {@link EscalationFlow} {@link JobNode}.
	 */
	private void record_JobContainer_createEscalationJob(Throwable failure,
			JobNode parallelOwner) {
		this.recordReturn(this.escalation, this.escalation.getTaskMetaData(),
				this.escalationTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState,
				this.threadState.createJobSequence(), this.escalationFlow);
		this.recordReturn(this.escalationFlow, this.escalationFlow
				.createTaskNode(this.escalationTaskMetaData, parallelOwner,
						failure, GovernanceDeactivationStrategy.DISREGARD),
				this.escalationJob);
	}

	/**
	 * Records waiting on a joined {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum} of the {@link Flow}
	 *            being joined on.
	 * @param timeout
	 *            Timeout in milliseconds for the {@link Flow} join.
	 * @param token
	 *            {@link Flow} join token.
	 */
	protected void record_JobContainer_waitOnFlow(Job currentJob,
			FlowInstigationStrategyEnum instigationStrategy, long timeout,
			Object token) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;

		// Obtain the flow instigated
		FlowAsset instigatedFlowAsset;
		boolean isWaitingOnFlow;
		switch (instigationStrategy) {
		case SEQUENTIAL:
			instigatedFlowAsset = this.flow;
			isWaitingOnFlow = false;
			break;
		case PARALLEL:
			instigatedFlowAsset = this.parallelFlow;
			isWaitingOnFlow = false;
			break;
		case ASYNCHRONOUS:
			instigatedFlowAsset = this.asynchronousThreadState;
			isWaitingOnFlow = true;
			break;
		default:
			fail("Unknown flow instigation strategy: " + instigationStrategy);
			return;
		}

		// Record waiting on the flow
		this.recordReturn(instigatedFlowAsset, instigatedFlowAsset.waitOnFlow(
				functionalityJob, timeout, token, this.jobActivatableSet),
				isWaitingOnFlow);

		// Determine if activate immediately as not waiting
		if (!isWaitingOnFlow) {
			this.jobActivatableSet.addJobNode(functionalityJob);
		}
	}

	/**
	 * Records not activating the {@link JobNode} when attempted.
	 */
	protected void record_JobContainer_notActivateJob() {
		// Record not activating the job node
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getThreadLock(),
				new Object());
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
		this.recordReturn(this.flow, this.flow.createTaskNode(
				this.nextTaskMetaData, functionalityJob.parallelOwnerJob,
				nextJobParameter, GovernanceDeactivationStrategy.ENFORCE),
				this.nextJob);
	}

	/**
	 * Records activating the next {@link Job}.
	 */
	protected void record_nextJob_activateJob() {
		this.nextJob.activateJob(this.currentTeam);
	}

	/**
	 * Records setting the parallel {@link JobNode} for the Parallel Owner.
	 */
	protected void record_ParallelOwner_unlinkAndActivate() {
		this.parallelOwnerJob.setParallelNode(null);
		this.parallelOwnerJob.activateJob(this.currentTeam);
	}

	/**
	 * Records {@link ContainerContext} adding a setup {@link Task}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param taskMetaData
	 *            {@link TaskMetaData}.
	 * @param parameter
	 *            Parameter.
	 */
	protected void record_ContainerContext_addSetupTask(Job job,
			TaskMetaData<?, ?, ?> taskMetaData, Object parameter) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;

		final Flow setupJobSequence = this.createMock(Flow.class);

		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState,
				this.threadState.createJobSequence(), setupJobSequence);
		this.recordReturn(setupJobSequence, setupJobSequence.createTaskNode(
				taskMetaData, functionalityJob, parameter,
				GovernanceDeactivationStrategy.ENFORCE), this.parallelJob);
		this.parallelJob.setParallelOwner(functionalityJob);
	}

	/**
	 * Records {@link ContainerContext} adding a {@link GovernanceActivity}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param activity
	 *            {@link GovernanceActivity}.
	 */
	protected void record_ContainerContext_addGovernanceActivity(Job job,
			GovernanceActivity<?, ?> activity) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;

		final Flow setupJobSequence = this.createMock(Flow.class);

		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState,
				this.threadState.createJobSequence(), setupJobSequence);
		this.recordReturn(setupJobSequence, setupJobSequence
				.createGovernanceNode(activity, functionalityJob),
				this.parallelJob);
		this.parallelJob.setParallelOwner(functionalityJob);
	}

	/**
	 * Records completing the {@link Job}.
	 * 
	 * @param job
	 *            {@link Job} being completed.
	 */
	protected void record_completeJob(Job job) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		// Clean up job
		this.workContainer.unloadWork(this.jobActivatableSet, this.currentTeam);
		this.flow.jobNodeComplete(functionalityJob, this.jobActivatableSet,
				this.currentTeam);
	}

	/**
	 * Records activating the {@link JobNodeActivatableSet}.
	 */
	protected void record_JobActivatableSet_activateJobs() {
		this.jobActivatableSet.activateJobNodes(this.currentTeam);
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
		return this.createJob(hasParallelOwnerJob, new ManagedObjectIndex[0],
				null, null, jobFunctionality);
	}

	/**
	 * Creates the {@link Job} for the {@link JobFunctionality}.
	 * 
	 * @param hasParallelOwnerJob
	 *            Flag indicating if to have a parallel owner {@link JobNode}.
	 * @param requiredManagedObjects
	 *            Required {@link ManagedObjectIndex} instances.
	 * @param requiredGovernance
	 *            {@link Governance} activation.
	 * @param deactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @param jobFunctionality
	 *            {@link JobFunctionality} instances.
	 * @return {@link Job}.
	 */
	protected FunctionalityJob createJob(boolean hasParallelOwnerJob,
			ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance,
			GovernanceDeactivationStrategy deactivationStrategy,
			JobFunctionality... jobFunctionality) {

		// Obtain the parallel owner job
		ParallelOwnerJob owner = (hasParallelOwnerJob ? this.parallelOwnerJob
				: null);

		// Ensure have required managed object indexes
		if (requiredManagedObjects == null) {
			requiredManagedObjects = new ManagedObjectIndex[0];
		}

		// Return the created functionality job
		return new FunctionalityJob(owner, requiredManagedObjects,
				requiredGovernance, deactivationStrategy, jobFunctionality);
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
			AbstractManagedJobNodeContainer<Work, JobMetaData> implements
			JobFunctionalityContext {

		/**
		 * {@link JobFunctionality}.
		 */
		private final JobFunctionality[] jobFunctionality;

		/**
		 * {@link ParallelOwnerJob} for this {@link Job}.
		 */
		public final JobNode parallelOwnerJob;

		/**
		 * Requires {@link ManagedObjectIndex} instances.
		 */
		public final ManagedObjectIndex[] requiredManagedObjectIndexes;

		/**
		 * Required {@link Governance}.
		 */
		public final boolean[] requiredGovernance;

		/**
		 * {@link GovernanceDeactivationStrategy}.
		 */
		public final GovernanceDeactivationStrategy deactivationStrategy;

		/**
		 * Flag indicating if the {@link Job} is executed.
		 */
		public boolean isJobExecuted = false;

		/**
		 * Initiate.
		 * 
		 * @param parallelOwnerJob
		 *            Parallel Owner {@link JobNode}.
		 * @param requiredManagedObjectIndexes
		 *            Required {@link ManagedObjectIndex} instances.
		 * @param requiredGovernance
		 *            {@link Governance} activation.
		 * @param deactivationStrategy
		 *            {@link GovernanceDeactivationStrategy}.
		 * @param jobFunctionality
		 *            {@link JobFunctionality}.
		 */
		public FunctionalityJob(JobNode parallelOwnerJob,
				ManagedObjectIndex[] requiredManagedObjectIndexes,
				boolean[] requiredGovernance,
				GovernanceDeactivationStrategy deactivationStrategy,
				JobFunctionality[] jobFunctionality) {
			super(AbstractJobContainerTest.this.flow,
					AbstractJobContainerTest.this.workContainer,
					AbstractJobContainerTest.this.jobMetaData,
					parallelOwnerJob, requiredManagedObjectIndexes,
					requiredGovernance, deactivationStrategy);
			this.jobFunctionality = jobFunctionality;
			this.parallelOwnerJob = parallelOwnerJob;
			this.requiredManagedObjectIndexes = requiredManagedObjectIndexes;
			this.requiredGovernance = requiredGovernance;
			this.deactivationStrategy = deactivationStrategy;
		}

		/*
		 * ==================== JobContainer ==============================
		 */

		@Override
		protected void loadJobName(StringBuilder message) {
			message.append("Mock ");
			message.append(this.getClass().getSimpleName());
		}

		@Override
		protected Object executeJob(ManagedJobNodeContext context,
				JobContext jobContext, JobNodeActivateSet activateSet)
				throws Throwable {
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

		@Override
		public Object getObject(ManagedObjectIndex managedObjectIndex) {
			return this.workContainer.getObject(managedObjectIndex,
					AbstractJobContainerTest.this.threadState);
		}

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

		@Override
		public void join(FlowFuture flowFuture, long timeout, Object token) {
			this.joinFlow(flowFuture, timeout, token);
		}

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
		 * Adds a setup {@link Task}.
		 * 
		 * @param taskMetaData
		 *            {@link TaskMetaData}.
		 * @param parameter
		 *            Parameter.
		 */
		void addSetupTask(TaskMetaData<?, ?, ?> taskMetaData, Object parameter);

		/**
		 * Adds a {@link GovernanceActivity}.
		 * 
		 * @param activity
		 *            {@link GovernanceActivity}.
		 */
		void addGovernanceActivity(GovernanceActivity<?, ?> activity);

		/**
		 * Obtains the {@link ManagedObject} instance's object.
		 * 
		 * @param managedObjectIndex
		 *            {@link ManagedObjectIndex} of the {@link ManagedObject}.
		 * @return Object of the {@link ManagedObject}.
		 */
		Object getObject(ManagedObjectIndex managedObjectIndex);

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
		 * Joins on the {@link FlowFuture}.
		 * 
		 * @param flowFuture
		 *            {@link FlowFuture}.
		 * @param timeout
		 *            The maximum time to wait in milliseconds for the
		 *            {@link Flow} to complete.
		 * @param token
		 *            A token identifying which {@link Flow} join timed
		 *            out. May be <code>null</code>.
		 */
		void join(FlowFuture flowFuture, long timeout, Object token);

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
	 * {@link EscalationFlow} {@link Job}.
	 */
	private static interface EscalationJob extends Job, JobNode {
	}
}
