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

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests {@link AbstractJobContainer} handling escalations.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalateJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensure able to handle by {@link EscalationProcedure} of {@link Job}.
	 */
	public void testFailureHandledByJob() {

		// Create job with thread failure
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(false);

		// Record failure on thread
		this.record_JobContainer_initialSteps(job, failure);
		this.record_JobContainer_handleEscalation(job, failure, true);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job with thread failure
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure parallel owner can handle escalation.
	 */
	public void testFailureHandledByParallelOwner() {

		// Create job with thread failure and parallel owner
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(true);

		// Record failure on thread
		this.record_JobContainer_initialSteps(job, failure);
		this.record_JobContainer_handleEscalation(job, failure, true);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job with thread failure
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure {@link Office} {@link EscalationProcedure} can handle escalation.
	 */
	public void testFailureHandledByOfficeEscalationProcedure() {

		// Create job with thread failure
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(false);

		// Record failure on thread
		this.record_JobContainer_initialSteps(job, failure);
		this.record_JobContainer_handleEscalation(job, failure, false);
		this.record_JobContainer_globalEscalation(job, failure,
				EscalationLevel.OFFICE);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job with thread failure
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure {@link ManagedObjectSource} {@link EscalationHandler} handles the
	 * escalation.
	 */
	public void testFailureHandledManagedObjectSourceEscalationHandler() {

		// Create job with thread failure
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(false);

		// Record failure on thread
		this.record_JobContainer_initialSteps(job, failure);
		this.record_JobContainer_handleEscalation(job, failure, false);
		this.record_JobContainer_globalEscalation(job, failure,
				EscalationLevel.INVOCATION_HANDLER);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job with thread failure
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed
		assertJobNotExecuted(job);
	}

	/**
	 * Ensure {@link OfficeFloor} {@link EscalationHandler} can handle
	 * escalation.
	 */
	public void testFailureHandledByOfficeFloorEscalationHandler() {

		// Create job with thread failure
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(false);

		// Record failure on thread
		this.record_JobContainer_initialSteps(job, failure);
		this.record_JobContainer_handleEscalation(job, failure, false);
		this.record_JobContainer_globalEscalation(job, failure,
				EscalationLevel.OFFICE_FLOOR);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute the job with thread failure
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job not executed
		assertJobNotExecuted(job);
	}

}