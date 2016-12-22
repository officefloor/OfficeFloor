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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.function.AbstractManagedFunctionContainer;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.MockTeamSource;

/**
 * Tests the {@link AbstractManagedFunctionContainer} executing a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecuteJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures execution of {@link Job}.
	 */
	public void testExecuteJob() {

		// Create a job without parallel owner
		Job job = this.createJob(false);

		// Record actions
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobMetaData_getNextTaskInFlow(false);
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

	/**
	 * Ensures execution of a {@link Job} that completes on third execution.
	 */
	public void testExecuteJobNotComplete() {

		// Create job that completes when specified
		final boolean[] isComplete = new boolean[1];
		isComplete[0] = false;
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				if (!isComplete[0]) {
					context.setComplete(false);
				}
				return null;
			}
		});

		// Record actions for first execution of job
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobActivatableSet_activateJobs();

		// Record actions for second execution of job
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobActivatableSet_activateJobs();

		// Record attempting to activate Job (but already active with Team)
		this.record_JobContainer_notActivateJob();

		// Record actions for third execution of job that completes
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job twice with it not completing
		this.doJob(job, false);
		assertJobExecuted(job); // should be executed on first attempt
		this.doJob(job, false);

		// Should not be able to assign Job to Team as Team still to complete
		job.activateJob(MockTeamSource.createTeamIdentifier());

		// Execute job third time which completes
		isComplete[0] = true;
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensures execution of {@link Job} with next {@link ManagedFunction}.
	 */
	public void testExecuteJobWithNextTask() {

		// Create a job to pass on a parameter
		final Object nextJobParameter = "Next job parameter";
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				return nextJobParameter;
			}
		});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobMetaData_getNextTaskInFlow(true);
		this.record_Flow_createJob(job, nextJobParameter);
		this.record_completeJob(job);
		this.record_nextJob_activateJob();
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