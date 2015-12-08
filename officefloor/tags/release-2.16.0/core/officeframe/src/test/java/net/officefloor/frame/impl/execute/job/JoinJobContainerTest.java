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

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * Tests the {@link AbstractJobContainer} invoking asynchronous {@link JobSequence}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class JoinJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures join on sequential {@link JobSequence}.
	 */
	public void testJoinSequentialFlow() {
		this.doJoinFlowTest(FlowInstigationStrategyEnum.SEQUENTIAL);
	}

	/**
	 * Ensures join on parallel {@link JobSequence}.
	 */
	public void testJoinParallelFlow() {
		this.doJoinFlowTest(FlowInstigationStrategyEnum.PARALLEL);
	}

	/**
	 * Ensures join on asynchronous {@link JobSequence}.
	 */
	public void testJoinAsynchronousFlow() {
		this.doJoinFlowTest(FlowInstigationStrategyEnum.ASYNCHRONOUS);
	}

	/**
	 * Does a single join on {@link JobSequence} test.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum} of the {@link JobSequence} being
	 *            joined on.
	 */
	private void doJoinFlowTest(
			final FlowInstigationStrategyEnum instigationStrategy) {

		final Object parameter = "FLOW PARAMETER";
		final long timeout = 1000;
		final Object token = "FLOW JOIN TOKEN";

		// Create a job invoking a flow and joining on it
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				FlowFuture flowFuture = context.doFlow(0, instigationStrategy,
						parameter);
				context.join(flowFuture, timeout, token);
				return null;
			}
		});

		// Record invoking the flow and joining on the flow
		this.record_JobContainer_initialSteps(job, null);
		switch (instigationStrategy) {
		case SEQUENTIAL:
			this.record_doSequentialFlow(job, parameter, false);
			break;
		case PARALLEL:
			this.record_doParallelFlow(job, parameter);
			break;
		case ASYNCHRONOUS:
			this.record_doAsynchronousFlow(job, parameter);
			break;
		default:
			fail("Unknown instigation strategy: " + instigationStrategy);
		}
		this.record_JobContainer_waitOnFlow(job, instigationStrategy, timeout,
				token);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute to invoke the flow and join on it
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

	/**
	 * Tests joining multiple {@link JobSequence} instances.
	 */
	public void testJoinMultipleFlows() {

		// Create a job invoking many flows and joining on them
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				FlowFuture sequential = context.doFlow(0,
						FlowInstigationStrategyEnum.SEQUENTIAL, "SEQUENTIAL");
				FlowFuture parallel = context.doFlow(0,
						FlowInstigationStrategyEnum.PARALLEL, "PARALLEL");
				context.join(sequential, 1, "SEQUENTIAL"); // mix up
				FlowFuture asynchronous = context.doFlow(0,
						FlowInstigationStrategyEnum.ASYNCHRONOUS,
						"ASYNCHRONOUS");
				context.join(parallel, 2, "PARALLEL");
				context.join(asynchronous, 3, "ASYNCHRONOUS");
				return null;
			}
		});

		// Record invoking the flow and joining on the flow
		this.record_JobContainer_initialSteps(job, null);
		this.record_doSequentialFlow(job, "SEQUENTIAL", false);
		this.record_doParallelFlow(job, "PARALLEL");
		this.record_doAsynchronousFlow(job, "ASYNCHRONOUS");
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.SEQUENTIAL, 1, "SEQUENTIAL");
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.PARALLEL, 2, "PARALLEL");
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.ASYNCHRONOUS, 3, "ASYNCHRONOUS");
		this.record_JobActivatableSet_activateJobs();

		// Execute to invoke the flows and join on them
		this.replayMockObjects();
		this.doJob(job, true);
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

	/**
	 * Tests joining the same {@link JobSequence} multiple times which should result on
	 * waiting on the {@link JobSequence} only once.
	 */
	public void testJoinSameFlowMultipleTimes() {

		// Create a job invoking a flow and joining on it multiple times
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				FlowFuture sameFlow = context.doFlow(0,
						FlowInstigationStrategyEnum.ASYNCHRONOUS, "SAME FLOW");
				context.join(sameFlow, 1000, "REGISTERED");
				context.join(sameFlow, 2000, "NOT REGISTERED");
				context.join(sameFlow, 3000, "NOT REGISTERED AGAIN");
				return null;
			}
		});

		// Record invoking the flow, joining on it many times and waiting once
		this.record_JobContainer_initialSteps(job, null);
		this.record_doAsynchronousFlow(job, "SAME FLOW");
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.ASYNCHRONOUS, 1000, "REGISTERED");
		this.record_JobActivatableSet_activateJobs();

		// Execute to join on invoked flows and wait only once
		this.replayMockObjects();
		this.doJob(job, true);
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

}