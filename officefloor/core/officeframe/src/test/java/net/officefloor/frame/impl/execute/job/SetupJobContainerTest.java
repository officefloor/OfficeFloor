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
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.spi.team.Job;

/**
 * Ensure able to provide setup {@link FunctionState} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SetupJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures execution of setup {@link FunctionState} requested before {@link ManagedFunction}
	 * execution.
	 */
	public void testPreExecuteSetupTask() {

		// Create a job that should not be executed
		Job job = this.createJob(false,
				new ManagedObjectIndex[] { new ManagedObjectIndexImpl(
						ManagedObjectScope.PROCESS, 0) }, null, null,
				new JobFunctionality() {
					@Override
					public Object executeFunctionality(
							JobFunctionalityContext context) throws Throwable {
						fail("Job should not be executed");
						return null;
					}
				});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);

		// Record setup job from managed object
		this.record_WorkContainer_loadManagedObjects(job);
		this.record_WorkContainer_governManagedObjects(job, true, false);
		this.record_WorkContainer_coordinateManagedObjects(job, true);
		this.record_WorkContainer_isManagedObjectsReady(job, true);

		// Record activating jobs
		this.record_JobActivatableSet_activateJobs();

		// Record activating the setup task (passively completes)
		this.record_parallelJob_getParallelNode(null);
		this.record_parallelJob_activateJob(job, true);

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobNotExecuted(job);
	}

	/**
	 * Ensures execution of setup {@link FunctionState} requested during {@link ManagedFunction}
	 * execution.
	 */
	public void testPostExecuteSetupTask() {

		final ManagedFunctionMetaData<?, ?, ?> taskMetaData = this
				.createMock(ManagedFunctionMetaData.class);
		final Object parameter = new Object();

		// Create a job with setup task
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				context.addSetupTask(taskMetaData, parameter);
				return null;
			}
		});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);

		// Record setup task
		this.record_ContainerContext_addSetupTask(job, taskMetaData, parameter);

		// Record activating the setup task (completes passively)
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_parallelJob_getParallelNode(null);
		this.record_parallelJob_activateJob(job, true);

		// Record activating jobs
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
	 * Ensures execution of setup {@link FunctionState} requested before {@link ManagedFunction}
	 * execution.
	 */
	public void testPreExecuteGovernanceActivity() {

		// Create a job that should not be executed
		Job job = this.createJob(false,
				new ManagedObjectIndex[] { new ManagedObjectIndexImpl(
						ManagedObjectScope.PROCESS, 0) }, null, null,
				new JobFunctionality() {
					@Override
					public Object executeFunctionality(
							JobFunctionalityContext context) throws Throwable {
						fail("Job should not be executed");
						return null;
					}
				});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);

		// Record setup job from managed object
		this.record_WorkContainer_loadManagedObjects(job);
		this.record_WorkContainer_governManagedObjects(job, false, true);
		this.record_WorkContainer_coordinateManagedObjects(job, true);
		this.record_WorkContainer_isManagedObjectsReady(job, true);

		// Record activating jobs
		this.record_JobActivatableSet_activateJobs();

		// Record activating the setup task (passively completes)
		this.record_parallelJob_getParallelNode(null);
		this.record_parallelJob_activateJob(job, true);

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobNotExecuted(job);
	}

	/**
	 * Ensures execution of setup {@link FunctionState} requested during {@link ManagedFunction}
	 * execution.
	 */
	public void testPostExecuteGovernanceActivity() {

		final GovernanceActivity<?, ?> activity = this
				.createMock(GovernanceActivity.class);

		// Create a job with setup task
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				context.addGovernanceActivity(activity);
				return null;
			}
		});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);

		// Record setup task
		this.record_ContainerContext_addGovernanceActivity(job, activity);

		// Record activating the setup task (completes passively)
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_parallelJob_getParallelNode(null);
		this.record_parallelJob_activateJob(job, true);

		// Record activating jobs
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