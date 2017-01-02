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

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
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
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
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
 * Contains functionality for testing the {@link ManagedFunctionContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedFunctionContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this.createMock(ProcessState.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow flow = this.createMock(Flow.class);

	/**
	 * {@link ManagedFunctionLogicMetaData}.
	 */
	private final ManagedFunctionLogicMetaData functionMetaData = this.createMock(ManagedFunctionLogicMetaData.class);

	/**
	 * {@link ManagedFunctionContext}.
	 */
	private final ManagedFunctionContext<?, ?> functionContext = this.createMock(ManagedFunctionContext.class);

	/**
	 * Next {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> nextFunctionMetaData = this.createMock(ManagedFunctionMetaData.class);

	/**
	 * Next {@link ManagedFunction}.
	 */
	private final NextFunction nextFunction = this.createMock(NextFunction.class);

	/**
	 * Sequential {@link FlowMetaData}.
	 */
	private final FlowMetaData sequentialFlowMetaData = this.createMock(FlowMetaData.class);

	/**
	 * Sequential {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> sequentialFunctionMetaData = this
			.createMock(ManagedFunctionMetaData.class);

	/**
	 * Sequential {@link ManagedFunction}.
	 */
	private final SequentialFunction sequentialFunction = this.createMock(SequentialFunction.class);

	/**
	 * Parallel {@link FlowMetaData}.
	 */
	private final FlowMetaData parallelFlowMetaData = this.createMock(FlowMetaData.class);

	/**
	 * Parallel {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> parallelFunctionMetaData = this
			.createMock(ManagedFunctionMetaData.class);

	/**
	 * Parallel {@link Flow}.
	 */
	private final Flow parallelFlow = this.createMock(Flow.class);

	/**
	 * Parallel {@link ManagedFunction}.
	 */
	private final ParallelFunction parallelFunction = this.createMock(ParallelFunction.class);

	/**
	 * Spawn {@link FlowMetaData}.
	 */
	private final FlowMetaData spawnFlowMetaData = this.createMock(FlowMetaData.class);

	/**
	 * Spawn {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> spawnFunctionMetaData = this.createMock(ManagedFunctionMetaData.class);

	/**
	 * Spawn {@link Flow}.
	 */
	private final Flow spawnFlow = this.createMock(Flow.class);

	/**
	 * Spawn {@link FunctionState}.
	 */
	private final FunctionState spawnFunction = this.createMock(FunctionState.class);

	/**
	 * Spawn {@link ThreadState}.
	 */
	private final ThreadState spawnThreadState = this.createMock(ThreadState.class);

	/**
	 * {@link EscalationFlow} {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> escalationFunctionMetaData = this
			.createMock(ManagedFunctionMetaData.class);

	/**
	 * {@link EscalationFlow} {@link Flow}.
	 */
	private final Flow escalationFlow = this.createMock(Flow.class);

	/**
	 * {@link EscalationFlow} {@link ManagedFunction}.
	 */
	private final EscalationFunction escalationFunction = this.createMock(EscalationFunction.class);

	/**
	 * Setup {@link FunctionState}.
	 */
	protected FunctionState setupFunction = null;

	/**
	 * {@link ParallelOwner}.
	 */
	protected ParallelOwner parallelOwner = null;

	/**
	 * Required {@link ManagedObjectIndex}.
	 */
	protected ManagedObjectIndex[] requiredManagedObjects = new ManagedObjectIndex[0];

	/**
	 * {@link ManagedFunction} bound {@link ManagedObjectContainer} instances.
	 */
	protected ManagedObjectContainer[] functionBoundManagedObjects = new ManagedObjectContainer[0];

	/**
	 * Required {@link Governance}.
	 */
	protected boolean[] requiredGovernance = new boolean[0];

	/**
	 * Flags whether ensure {@link Governance}.
	 */
	protected boolean isEnforceGovernance = true;

	/**
	 * Flags whether to unload the {@link ManagedObject} instances.
	 */
	protected boolean isUnloadManagedObjects = true;

	/**
	 * Records:
	 * <ol>
	 * <li>obtaining the {@link ThreadState}</li>
	 * <li>obtaining the {@link ProcessState}</li>
	 * <li>obtaining required {@link ManagedObject} indexes, if no failure</li>
	 * </ol>
	 * <p>
	 * This is always the first steps of executing a
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @param function
	 *            {@link ManagedFunction}.
	 */
	protected void record_Container_initialSteps(ManagedFunctionLogic function) {
		final FunctionalityFunction functionalityJob = (FunctionalityFunction) function;
		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(), this.processState);
		this.threadState.profile(this.functionMetaData);
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
	protected void record_JobContainer_activateGovernance(Job job, boolean... currentGovernanceState) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;

		// Run through governance
		for (int i = 0; i < functionalityJob.requiredGovernance.length; i++) {
			boolean isRequireGovernance = functionalityJob.requiredGovernance[i];

			// Determine current activation state (defaulty not active)
			boolean isCurrentlyActive = (i < currentGovernanceState.length ? currentGovernanceState[i] : false);

			// Record whether active
			this.recordReturn(this.threadState, this.threadState.isGovernanceActive(i), isCurrentlyActive);

			// Handle changing activation of Governance
			if (isRequireGovernance != isCurrentlyActive) {

				// Obtain the Governance Container
				final GovernanceContainer<?, ?> container = this.createMock(GovernanceContainer.class);
				this.recordReturn(this.threadState, this.threadState.getGovernanceContainer(i), container);

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
						fail("Unknown governance deactivation strategy " + functionalityJob.deactivationStrategy);
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
		this.workContainer.loadManagedObjects(functionalityJob.requiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet, this.currentTeam, functionalityJob);
	}

	/**
	 * Records checking {@link ManagedObject} instances are ready.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isReady
	 *            Indicates if ready.
	 */
	protected void record_WorkContainer_isManagedObjectsReady(Job job, boolean isReady) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.recordReturn(this.workContainer,
				this.workContainer.isManagedObjectsReady(functionalityJob.requiredManagedObjectIndexes, this.jobContext,
						functionalityJob, this.jobActivatableSet, functionalityJob),
				isReady);
	}

	/**
	 * Records governing the {@link ManagedObject} instances.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param isSetupTask
	 *            Indicates to add a setup {@link ManagedFunction}.
	 * @param isGovernanceActivity
	 *            Indicates to add a {@link GovernanceActivity}.
	 */
	protected void record_WorkContainer_governManagedObjects(Job job, boolean isSetupTask,
			boolean isGovernanceActivity) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;

		// Record governing managed objects
		this.workContainer.governManagedObjects(functionalityJob.requiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet, functionalityJob);

		if (isSetupTask) {

			final ManagedFunctionMetaData<?, ?, ?> setuptaskMetaData = this.createMock(ManagedFunctionMetaData.class);
			final Object parameter = new Object();

			// Trigger setup task from govern managed objects
			this.control(this.workContainer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {

					// Ensure correct parameters
					for (int i = 0; i < expected.length; i++) {
						assertEquals("Incorrect parameter " + i, expected[i], actual[i]);
					}

					// Trigger setup task
					ContainerContext context = (ContainerContext) actual[4];
					context.addSetupTask(setuptaskMetaData, parameter);

					return true;
				}
			});

			// Record adding the setup task
			this.record_ContainerContext_addSetupTask(job, setuptaskMetaData, parameter);

		} else if (isGovernanceActivity) {

			final GovernanceActivity<?, ?> governanceActivity = this.createMock(GovernanceActivity.class);

			// Trigger governance activity from govern managed objects
			this.control(this.workContainer).setMatcher(new AbstractMatcher() {

				@Override
				public boolean matches(Object[] expected, Object[] actual) {

					// Ensure correct parameters
					for (int i = 0; i < expected.length; i++) {
						assertEquals("Incorrect parameter " + i, expected[i], actual[i]);
					}

					// Trigger governance activity
					ContainerContext context = (ContainerContext) actual[4];
					context.addGovernanceActivity(governanceActivity);

					return true;
				}
			});

			// Record adding the governance activity
			this.record_ContainerContext_addGovernanceActivity(job, governanceActivity);
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
	protected void record_WorkContainer_coordinateManagedObjects(Job job, final boolean isCoordinated) {
		final FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.workContainer.coordinateManagedObjects(functionalityJob.requiredManagedObjectIndexes, this.jobContext,
				functionalityJob, this.jobActivatableSet, functionalityJob);

		// Ensure appropriately flags for job to wait
		if (this.coordinateJobsToWait == null) {
			this.coordinateJobsToWait = new LinkedList<Boolean>();
			this.control(this.workContainer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					for (int i = 0; i < expected.length; i++) {
						assertEquals("Incorrect argument " + i, expected[i], actual[i]);
					}
					Boolean isCoordinate = AbstractManagedFunctionContainerTest.this.coordinateJobsToWait.remove(0);
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
	protected void record_WorkContainer_getObject(ManagedObjectIndex managedObjectIndex, Object moObject) {
		this.recordReturn(this.workContainer, this.workContainer.getObject(managedObjectIndex, this.threadState),
				moObject);
	}

	/**
	 * Records doing a sequential {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param sequentialFlowParameter
	 *            Sequential {@link Flow} parameter.
	 */
	protected void record_doSequentialFlow(ManagedFunctionLogic currentFunction, Object sequentialFlowParameter) {
		FunctionalityFunction functionalityFunction = (FunctionalityFunction) currentFunction;
		this.recordReturn(this.sequentialFlowMetaData, this.sequentialFlowMetaData.getInitialFunctionMetaData(),
				this.sequentialFunctionMetaData);
		this.recordReturn(this.flow, this.flow.createManagedFunction(sequentialFlowParameter,
				this.sequentialFunctionMetaData, true, functionalityFunction.parallelOwner), this.sequentialFunction);
	}

	/**
	 * Records doing a parallel {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param parallelFlowParameter
	 *            Parallel {@link Job} parameter.
	 */
	protected void record_doParallelFlow(Job currentJob, Object parallelFlowParameter) {
		FunctionalityJob functionalityJob = (FunctionalityJob) currentJob;
		this.recordReturn(this.parallelFlowMetaData, this.parallelFlowMetaData.getInstigationStrategy(),
				FlowInstigationStrategyEnum.PARALLEL);
		this.recordReturn(this.parallelFlowMetaData, this.parallelFlowMetaData.getInitialTaskMetaData(),
				this.parallelTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.createJobSequence(), this.parallelFlow);
		this.recordReturn(this.parallelFlow, this.parallelFlow.createTaskNode(this.parallelTaskMetaData,
				functionalityJob, parallelFlowParameter, GovernanceDeactivationStrategy.ENFORCE), this.parallelJob);
		this.parallelJob.setParallelOwner(functionalityJob);
	}

	/**
	 * Records obtaining the {@link ManagedFunctionMetaData} of the next
	 * {@link ManagedFunction} to execute.
	 * 
	 * @param hasNextJob
	 *            Flag indicating if have next {@link Job}.
	 */
	protected void record_JobMetaData_getNextTaskInFlow(boolean hasNextJob) {
		// Determine if next task
		ManagedFunctionMetaData<?, ?, ?> taskMetaData = (hasNextJob ? this.nextTaskMetaData : null);
		this.recordReturn(this.jobMetaData, this.jobMetaData.getNextManagedFunctionContainerMetaData(), taskMetaData);
	}

	/**
	 * Records obtaining the parallel {@link FunctionState} from the parallel
	 * {@link Job}.
	 * 
	 * @param parallelNode
	 *            Parallel's {@link Job} parallel {@link FunctionState}.
	 */
	protected void record_parallelJob_getParallelNode(FunctionState parallelNode) {
		this.recordReturn(this.parallelJob, this.parallelJob.getParallelNode(), parallelNode);
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
	protected void record_parallelJob_activateJob(Job currentJob, final boolean isComplete) {
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
			this.recordReturn(this.parallelJob, this.parallelJob.isJobNodeComplete(), isComplete);
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
	protected void record_doAsynchronousFlow(Job currentJob, Object asynchronousFlowParameter) {
		this.recordReturn(this.asynchronousFlowMetaData, this.asynchronousFlowMetaData.getInstigationStrategy(),
				FlowInstigationStrategyEnum.ASYNCHRONOUS);
		this.recordReturn(this.asynchronousFlowMetaData, this.asynchronousFlowMetaData.getInitialTaskMetaData(),
				this.asynchronousTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(), this.processState);
		this.recordReturn(this.asynchronousFlowMetaData, this.asynchronousFlowMetaData.getFlowManager(),
				this.asynchronousFlowAssetManager);
		this.recordReturn(this.processState, this.processState.createThread(this.asynchronousFlowAssetManager),
				this.asynchronousFlow);
		this.recordReturn(this.asynchronousFlow, this.asynchronousFlow.createTaskNode(this.asynchronousTaskMetaData,
				null, asynchronousFlowParameter, GovernanceDeactivationStrategy.ENFORCE), this.asynchronousJob);
		this.asynchronousJob.activateJob(ManagedFunctionContainerImpl.ASYNCHRONOUS_FLOW_TEAM_IDENTIFIER);
		this.recordReturn(this.asynchronousFlow, this.asynchronousFlow.getThreadState(), this.asynchronousThreadState);
	}

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure = this.createMock(EscalationProcedure.class);

	/**
	 * {@link EscalationFlow}.
	 */
	private final EscalationFlow escalation = this.createMock(EscalationFlow.class);

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
	protected void record_JobContainer_handleEscalation(Job job, Throwable failure, boolean isHandled) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;
		this.threadState.escalationStart(functionalityJob, this.jobActivatableSet);
		this.record_completeJob(job);
		this.recordReturn(this.jobMetaData, this.jobMetaData.getEscalationProcedure(), this.escalationProcedure);
		EscalationFlow escalation = (isHandled ? this.escalation : null);
		if (functionalityJob.parallelOwnerJob == null) {
			// No parallel owner, so handle by job
			this.recordReturn(this.escalationProcedure, this.escalationProcedure.getEscalation(failure), escalation);
		} else {
			// Parallel owner, so handle by job
			this.recordReturn(this.escalationProcedure, this.escalationProcedure.getEscalation(failure), null);
			this.recordReturn(functionalityJob.parallelOwnerJob,
					functionalityJob.parallelOwnerJob.getEscalationProcedure(), this.escalationProcedure);
			this.recordReturn(this.escalationProcedure, this.escalationProcedure.getEscalation(failure), escalation);
			this.recordReturn(functionalityJob.parallelOwnerJob, functionalityJob.parallelOwnerJob.getParallelOwner(),
					null);
		}
		if (isHandled) {
			this.record_JobContainer_createEscalationJob(failure, functionalityJob.parallelOwnerJob);
		}
		this.threadState.escalationComplete(functionalityJob, this.jobActivatableSet);
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
	protected void record_JobContainer_globalEscalation(Job job, Throwable failure, EscalationLevel handledLevel) {
		this.recordReturn(this.threadState, this.threadState.getEscalationLevel(), EscalationLevel.FLOW);

		// Handled by the office escalation procedure
		this.recordReturn(this.processState, this.processState.getOfficeEscalationProcedure(),
				this.escalationProcedure);
		if (handledLevel == EscalationLevel.OFFICE) {
			this.recordReturn(this.escalationProcedure, this.escalationProcedure.getEscalation(failure),
					this.escalation);
			this.threadState.setEscalationLevel(EscalationLevel.OFFICE);
			this.record_JobContainer_createEscalationJob(failure, null);
			this.escalationJob.activateJob(this.currentTeam);
			return;
		}
		this.recordReturn(this.escalationProcedure, this.escalationProcedure.getEscalation(failure), null);

		// Handled by the managed object source escalation handler
		if (handledLevel == EscalationLevel.INVOCATION_HANDLER) {
			this.recordReturn(this.processState, this.processState.getInvocationEscalation(), this.escalation);
			this.threadState.setEscalationLevel(EscalationLevel.INVOCATION_HANDLER);
			this.record_JobContainer_createEscalationJob(failure, null);
			this.escalationJob.activateJob(this.currentTeam);
			return;
		}
		this.recordReturn(this.processState, this.processState.getInvocationEscalation(), null);

		// Handled by the office floor escalation
		this.recordReturn(this.processState, this.processState.getOfficeFloorEscalation(), this.escalation);
		this.threadState.setEscalationLevel(EscalationLevel.OFFICE_FLOOR);
		this.record_JobContainer_createEscalationJob(failure, null);
		this.escalationJob.activateJob(this.currentTeam);
	}

	/**
	 * Records the creation of the {@link EscalationFlow} {@link FunctionState}.
	 * 
	 * @param failure
	 *            {@link Throwable} causing escalation.
	 * @param parallelOwner
	 *            Parallel owner of {@link EscalationFlow}
	 *            {@link FunctionState}.
	 */
	private void record_JobContainer_createEscalationJob(Throwable failure, FunctionState parallelOwner) {
		this.recordReturn(this.escalation, this.escalation.getTaskMetaData(), this.escalationTaskMetaData);
		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.createJobSequence(), this.escalationFlow);
		this.recordReturn(this.escalationFlow, this.escalationFlow.createTaskNode(this.escalationTaskMetaData,
				parallelOwner, failure, GovernanceDeactivationStrategy.DISREGARD), this.escalationJob);
	}

	/**
	 * Records waiting on a joined {@link Flow}.
	 * 
	 * @param currentJob
	 *            Current {@link Job}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum} of the {@link Flow} being
	 *            joined on.
	 * @param timeout
	 *            Timeout in milliseconds for the {@link Flow} join.
	 * @param token
	 *            {@link Flow} join token.
	 */
	protected void record_JobContainer_waitOnFlow(Job currentJob, FlowInstigationStrategyEnum instigationStrategy,
			long timeout, Object token) {
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
		this.recordReturn(instigatedFlowAsset,
				instigatedFlowAsset.waitOnFlow(functionalityJob, timeout, token, this.jobActivatableSet),
				isWaitingOnFlow);

		// Determine if activate immediately as not waiting
		if (!isWaitingOnFlow) {
			this.jobActivatableSet.addJobNode(functionalityJob);
		}
	}

	/**
	 * Records not activating the {@link FunctionState} when attempted.
	 */
	protected void record_JobContainer_notActivateJob() {
		// Record not activating the job node
		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.getThreadLock(), new Object());
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
		this.recordReturn(this.flow, this.flow.createTaskNode(this.nextTaskMetaData, functionalityJob.parallelOwnerJob,
				nextJobParameter, GovernanceDeactivationStrategy.ENFORCE), this.nextJob);
	}

	/**
	 * Records activating the next {@link Job}.
	 */
	protected void record_nextJob_activateJob() {
		this.nextJob.activateJob(this.currentTeam);
	}

	/**
	 * Records setting the parallel {@link FunctionState} for the Parallel
	 * Owner.
	 */
	protected void record_ParallelOwner_unlinkAndActivate() {
		this.parallelOwnerJob.setParallelNode(null);
		this.parallelOwnerJob.activateJob(this.currentTeam);
	}

	/**
	 * Records {@link ContainerContext} adding a setup {@link ManagedFunction}.
	 * 
	 * @param job
	 *            {@link Job}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param parameter
	 *            Parameter.
	 */
	protected void record_ContainerContext_addSetupTask(Job job, ManagedFunctionMetaData<?, ?, ?> taskMetaData,
			Object parameter) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;

		final Flow setupJobSequence = this.createMock(Flow.class);

		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.createJobSequence(), setupJobSequence);
		this.recordReturn(setupJobSequence, setupJobSequence.createTaskNode(taskMetaData, functionalityJob, parameter,
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
	protected void record_ContainerContext_addGovernanceActivity(Job job, GovernanceActivity<?, ?> activity) {
		FunctionalityJob functionalityJob = (FunctionalityJob) job;

		final Flow setupJobSequence = this.createMock(Flow.class);

		this.recordReturn(this.flow, this.flow.getThreadState(), this.threadState);
		this.recordReturn(this.threadState, this.threadState.createJobSequence(), setupJobSequence);
		this.recordReturn(setupJobSequence, setupJobSequence.createGovernanceFunction(activity, functionalityJob),
				this.parallelJob);
		this.parallelJob.setParallelOwner(functionalityJob);
	}

	/**
	 * Records completing the {@link Job}.
	 * 
	 * @param function
	 *            {@link ManagedFunctionLogic} being completed.
	 */
	protected void record_completeFunction(ManagedFunctionLogic function) {
		FunctionalityFunction functionalityFunction = (FunctionalityFunction) function;
		this.flow.managedFunctionComplete(functionalityJob, this.jobActivatableSet, this.currentTeam);
	}

	/**
	 * Records activating the {@link JobNodeActivatableSet}.
	 */
	protected void record_JobActivatableSet_activateJobs() {
		this.jobActivatableSet.activateJobNodes(this.currentTeam);
	}

	/**
	 * Creates the {@link FunctionalityFunction} for the
	 * {@link FunctionFunctionality}.
	 * 
	 * @param functionFunctionality
	 *            {@link FunctionFunctionality} instances.
	 * @return {@link ManagedFunctionLogic}.
	 */
	protected ManagedFunctionLogic createFunction(FunctionFunctionality... functionFunctionality) {

		// Obtain the parallel owner
		ParallelOwner owner = (this.isParallelOwner ? this.parallelOwner : null);

		// Managed object containers
		ManagedObjectMetaData<?>[] boundManagedObjectMetaData = new ManagedObjectMetaData<?>[0];

		// Return the created functionality function
		return new FunctionalityFunction(functionFunctionality, owner, setupFunction, this.requiredManagedObjects,
				this.functionBoundManagedObjects, this.requiredGovernance, this.isEnforceGovernance,
				this.isUnloadManagedObjects);
	}

	/**
	 * Executes the {@link FunctionalityFunction}.
	 * 
	 * @param function
	 *            {@link FunctionalityFunction}.
	 * @return {@link FunctionState}.
	 */
	protected FunctionState doFunction(ManagedFunctionLogic function) {
		try {
			FunctionalityFunction functionalityFunction = (FunctionalityFunction) function;
			return functionalityFunction.container.execute();
		} catch (Throwable ex) {
			throw this.fail(ex);
		}
	}

	/**
	 * Asserts that the {@link ManagedFunctionLogic} was executed.
	 * 
	 * @param function
	 *            {@link ManagedFunctionLogic} to assert was executed.
	 */
	protected static void assertFunctionExecuted(ManagedFunctionLogic function) {
		FunctionalityFunction functionalityFunction = (FunctionalityFunction) function;
		assertTrue("Function is expected to be executed", functionalityFunction.isFunctionExecuted);
	}

	/**
	 * Asserts that the {@link Job} is not executed.
	 * 
	 * @param job
	 *            {@link Job} to assert is not executed.
	 */
	protected static void assertJobNotExecuted(Job job) {
		FunctionalityFunction functionalityFunction = (FunctionalityFunction) job;
		assertFalse("Job is expected to not be executed", functionalityJob.isJobExecuted);
	}

	/**
	 * {@link ManagedFunctionLogic} that delegates to a
	 * {@link FunctionFunctionality} to provide functionality.
	 */
	protected class FunctionalityFunction implements ManagedFunctionLogic {

		/**
		 * {@link FunctionFunctionality}.
		 */
		private final FunctionFunctionality[] functionFunctionality;

		/**
		 * {@link ParallelOwner}.
		 */
		private final ParallelOwner parallelOwner;

		/**
		 * {@link ManagedFunctionContainer}.
		 */
		private final ManagedFunctionContainer container;

		/**
		 * Flag indicating if the {@link ManagedFunction} is executed.
		 */
		public boolean isFunctionExecuted = false;

		/**
		 * Initiate.
		 * 
		 * @param functionFunctionality
		 *            {@link FunctionFunctionality}.
		 * @param parallelOwner
		 *            {@link ParallelOwner}.
		 */
		public FunctionalityFunction(FunctionFunctionality[] functionFunctionality, ParallelOwner parallelOwner,
				FunctionState setupFunction, ManagedObjectIndex[] requiredManagedObjects,
				ManagedObjectContainer[] functionBoundManagedObjects, boolean[] requiredGovernance,
				boolean isEnforceGovernance, boolean isUnloadManagedObjects) {
			this.functionFunctionality = functionFunctionality;
			this.parallelOwner = parallelOwner;

			// Create the managed function container
			this.container = new ManagedFunctionContainerImpl<ManagedFunctionLogicMetaData>(setupFunction, this,
					functionBoundManagedObjects, requiredManagedObjects, requiredGovernance, isEnforceGovernance,
					AbstractManagedFunctionContainerTest.this.functionMetaData, parallelOwner,
					AbstractManagedFunctionContainerTest.this.flow, isUnloadManagedObjects);
		}

		/*
		 * ==================== ManagedFunctionLogic ====================
		 */

		@Override
		public Object execute(ManagedFunctionLogicContext context) throws Throwable {
			// Indicate the function is executed
			this.isFunctionExecuted = true;

			// Execute the functionality (using result of last as parameter)
			Object parameter = null;
			for (FunctionFunctionality functionality : this.functionFunctionality) {
				parameter = functionality.executeFunctionality(new FunctionFunctionalityContext() {

					@Override
					public void addSetupFunction(ManagedFunctionMetaData<?, ?> functionMetaData, Object parameter) {
						// TODO implement
						// AbstractManagedFunctionContainerTest.FunctionalityFunction.$local$.addSetupFunction
						throw new UnsupportedOperationException(
								"TODO implement AbstractManagedFunctionContainerTest.FunctionalityFunction.$local$.addSetupFunction");

					}

					@Override
					public void addGovernanceActivity(GovernanceActivity<?> activity) {
						// TODO implement
						// AbstractManagedFunctionContainerTest.FunctionalityFunction.$local$.addGovernanceActivity
						throw new UnsupportedOperationException(
								"TODO implement AbstractManagedFunctionContainerTest.FunctionalityFunction.$local$.addGovernanceActivity");

					}

					@Override
					public Object getObject(ManagedObjectIndex managedObjectIndex) {
						context.getObject(managedObjectIndex);
					}

					@Override
					public void doFlow(int flowIndex, Object parameter, FlowCallback callback,
							boolean isSpawnThreadState) {

						// Obtain the Flow meta-data
						FlowMetaData flowMetaData;
						if (isSpawnThreadState) {
							flowMetaData = AbstractManagedFunctionContainerTest.this.spawnFlowMetaData;
						} else if (callback != null) {
							flowMetaData = AbstractManagedFunctionContainerTest.this.parallelFlowMetaData;
						} else {
							flowMetaData = AbstractManagedFunctionContainerTest.this.sequentialFlowMetaData;
						}

						// Do the flow
						context.doFlow(flowMetaData, parameter, callback);
					}
				});
			}

			// Return the parameter
			return parameter;
		}
	}

	/**
	 * Functionality for testing the {@link ManagedFunctionLogic}.
	 */
	protected static interface FunctionFunctionality {

		/**
		 * Executes the functionality for the {@link ManagedFunction}.
		 * 
		 * @param context
		 *            {@link FunctionFunctionalityContext}.
		 * @return Result of functionality, that is used as a parameter to the
		 *         next {@link ManagedFunction}.
		 * @throws Throwable
		 *             If failure of functionality.
		 */
		Object executeFunctionality(FunctionFunctionalityContext context) throws Throwable;
	}

	/**
	 * Context for the {@link FunctionFunctionality}.
	 */
	protected static interface FunctionFunctionalityContext {

		/**
		 * Adds a setup {@link ManagedFunction}.
		 * 
		 * @param functionMetaData
		 *            {@link ManagedFunctionMetaData}.
		 * @param parameter
		 *            Parameter.
		 */
		void addSetupFunction(ManagedFunctionMetaData<?, ?> functionMetaData, Object parameter);

		/**
		 * Adds a {@link GovernanceActivity}.
		 * 
		 * @param activity
		 *            {@link GovernanceActivity}.
		 */
		void addGovernanceActivity(GovernanceActivity<?> activity);

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
		 * @param parameter
		 *            Parameter for the first {@link Job} of the invoked
		 *            {@link Flow}.
		 * @param callback
		 *            {@link FlowCallback}.
		 * @param isSpawnThreadState
		 *            Flags whether to spawn a {@link ThreadState}
		 * @return {@link FlowFuture} for the invoked {@link Flow}.
		 */
		void doFlow(int flowIndex, Object parameter, FlowCallback callback, boolean isSpawnThreadState);

	}

	/**
	 * Parallel owner {@link ManagedFunctionContainer}.
	 */
	private static interface ParallelOwner extends ManagedFunctionContainer {
	}

	/**
	 * Next {@link ManagedFunctionContainer}.
	 */
	private static interface NextFunction extends ManagedFunctionContainer {
	}

	/**
	 * Sequential {@link ManagedFunctionContainer}.
	 */
	private static interface SequentialFunction extends ManagedFunctionContainer {
	}

	/**
	 * Parallel {@link ManagedFunctionContainer}.
	 */
	private static interface ParallelFunction extends ManagedFunctionContainer {
	}

	/**
	 * {@link EscalationFlow} {@link ManagedFunctionContainer}.
	 */
	private static interface EscalationFunction extends ManagedFunctionContainer {
	}

}
