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

import net.officefloor.frame.impl.execute.function.AbstractManagedFunctionContainer;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests the {@link AbstractManagedFunctionContainer} invoking sequential
 * {@link Flow} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SequentialJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures execution of a {@link Job} with a sequential {@link Flow}
	 * invoked.
	 */
	public void testExecuteJobWithSequentialFlow() {

		// Create a job invoking a sequential flow
		final Object sequentialFlowParameter = "Sequential Flow Parameter";
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				context.doFlow(0, FlowInstigationStrategyEnum.SEQUENTIAL,
						sequentialFlowParameter);
				return null;
			}
		});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);
		this.record_doSequentialFlow(job, sequentialFlowParameter, true);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

}