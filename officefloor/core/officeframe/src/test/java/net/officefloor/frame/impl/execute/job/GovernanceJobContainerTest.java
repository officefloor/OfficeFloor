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

import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests {@link Governance} within the {@link ManagedFunctionContainerImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceJobContainerTest extends AbstractManagedFunctionContainerTest {

	/**
	 * Ensure can not managed the {@link Governance}.
	 */
	public void testNotManage() {

		// Create a job without required Governance
		Job job = this.createJob(false, new ManagedObjectIndex[0], null, null);

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
	 * Ensure can activate {@link Governance}.
	 */
	public void testActivation() {

		// Create a job requiring Governance
		Job job = this.createJob(GovernanceDeactivationStrategy.ENFORCE, false,
				true, false, true);

		// Record activating governance
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobContainer_activateGovernance(job, false, false, false,
				false);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed as wait on governance
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure can enforce the {@link Governance}.
	 */
	public void testEnforce() {

		// Create a job requiring Governance
		Job job = this.createJob(GovernanceDeactivationStrategy.ENFORCE, false,
				false, false, false);

		// Record activating governance
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobContainer_activateGovernance(job, true, false, true,
				false);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed as wait on governance
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure can disregard the {@link Governance}.
	 */
	public void testDisregard() {

		// Create a job requiring Governance
		Job job = this.createJob(GovernanceDeactivationStrategy.DISREGARD,
				false, false, false, false);

		// Record activating governance
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobContainer_activateGovernance(job, false, true, true,
				false);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed as wait on governance
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure {@link Governance} already appropriately activated.
	 */
	public void testNoChange() {

		boolean[] requiredGovernance = new boolean[3];
		requiredGovernance[0] = true;
		requiredGovernance[2] = true;

		// Create a job requiring Governance
		Job job = this.createJob(GovernanceDeactivationStrategy.ENFORCE,
				requiredGovernance);

		// Record activating governance
		this.record_JobContainer_initialSteps(job, null);
		this.record_JobContainer_activateGovernance(job, requiredGovernance);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job executed as no governance state change
		assertJobExecuted(job);
	}

	/**
	 * Convenience method for creating a {@link Job} with required
	 * {@link Governance}.
	 * 
	 * @param deactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @param requiredGovernance
	 *            Required {@link Governance}.
	 * @return {@link Job}.
	 */
	private Job createJob(GovernanceDeactivationStrategy deactivationStrategy,
			boolean... requiredGovernance) {
		return this.createJob(false, new ManagedObjectIndex[0],
				requiredGovernance, deactivationStrategy);
	}

}