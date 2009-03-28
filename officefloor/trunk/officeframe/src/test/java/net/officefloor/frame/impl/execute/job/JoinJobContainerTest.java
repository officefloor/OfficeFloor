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

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * Tests the {@link AbstractJobContainer} invoking asynchronous {@link Flow}
 * instances.
 * 
 * @author Daniel
 */
public class JoinJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures join on sequential {@link Flow}.
	 */
	public void testJoinSequentialFlow() {
		this.doJoinFlowTest(FlowInstigationStrategyEnum.SEQUENTIAL);
	}

	/**
	 * Ensures join on parallel {@link Flow}.
	 */
	public void testJoinParallelFlow() {
		this.doJoinFlowTest(FlowInstigationStrategyEnum.PARALLEL);
	}

	/**
	 * Ensures join on asynchronous {@link Flow}.
	 */
	public void testJoinAsynchronousFlow() {
		this.doJoinFlowTest(FlowInstigationStrategyEnum.ASYNCHRONOUS);
	}

	/**
	 * Does a single join on {@link Flow} test.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum} of the {@link Flow} being
	 *            joined on.
	 */
	private void doJoinFlowTest(
			final FlowInstigationStrategyEnum instigationStrategy) {

		final Object parameter = "FLOW PARAMETER";

		// Create a job invoking a flow and joining on it
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				FlowFuture flowFuture = context.doFlow(0, instigationStrategy,
						parameter);
				context.join(flowFuture);
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
		this.record_JobContainer_waitOnFlow(job, instigationStrategy);
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
	 * Tests joining multiple {@link Flow} instances.
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
				context.join(sequential); // mix up doing flows and joining
				FlowFuture asynchronous = context.doFlow(0,
						FlowInstigationStrategyEnum.ASYNCHRONOUS,
						"ASYNCHRONOUS");
				context.join(parallel);
				context.join(asynchronous);
				return null;
			}
		});

		// Record invoking the flow and joining on the flow
		this.record_JobContainer_initialSteps(job, null);
		this.record_doSequentialFlow(job, "SEQUENTIAL", false);
		this.record_doParallelFlow(job, "PARALLEL");
		this.record_doAsynchronousFlow(job, "ASYNCHRONOUS");
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.SEQUENTIAL);
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.PARALLEL);
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.ASYNCHRONOUS);
		this.record_JobActivatableSet_activateJobs();

		// Execute to invoke the flows and join on them
		this.replayMockObjects();
		this.doJob(job, true);
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

	/**
	 * Tests joining the same {@link Flow} multiple times which should result on
	 * waiting on the {@link Flow} only once.
	 */
	public void testJoinSameFlowMultipleTimes() {

		// Create a job invoking a flow and joining on it multiple times
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				FlowFuture sameFlow = context.doFlow(0,
						FlowInstigationStrategyEnum.ASYNCHRONOUS, "SAME FLOW");
				context.join(sameFlow);
				context.join(sameFlow);
				context.join(sameFlow);
				return null;
			}
		});

		// Record invoking the flow, joining on it many times and waiting once
		this.record_JobContainer_initialSteps(job, null);
		this.record_doAsynchronousFlow(job, "SAME FLOW");
		this.record_JobContainer_waitOnFlow(job,
				FlowInstigationStrategyEnum.ASYNCHRONOUS);
		this.record_JobActivatableSet_activateJobs();

		// Execute to join on invoked flows and wait only once
		this.replayMockObjects();
		this.doJob(job, true);
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

}