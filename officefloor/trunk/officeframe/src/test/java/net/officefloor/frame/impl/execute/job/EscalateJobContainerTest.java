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

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests {@link AbstractJobContainer} handling escalations.
 * 
 * @author Daniel
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
		this.record_JobContainer_initialSteps(failure);
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
		this.record_JobContainer_initialSteps(failure);
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
	 * Ensure {@link ManagedObjectSource} {@link Handler}
	 * {@link EscalationHandler} handles the escalation.
	 */
	public void testFailureHandledManagedObjectSourceEscalationHandler() {

		// Create job with thread failure
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(false);

		// Record failure on thread
		this.record_JobContainer_initialSteps(failure);
		this.record_JobContainer_handleEscalation(job, failure, false);
		this.record_JobContainer_globalEscalation(job, failure,
				EscalationLevel.MANAGED_OBJECT_SOURCE_HANDLER);
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
		this.record_JobContainer_initialSteps(failure);
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
	 * Ensure {@link OfficeFloor} {@link EscalationHandler} can handle
	 * escalation.
	 */
	public void testFailureHandledByOfficeFloorEscalationHandler() {

		// Create job with thread failure
		final Throwable failure = new Throwable("Thread failure");
		Job job = this.createJob(false);

		// Record failure on thread
		this.record_JobContainer_initialSteps(failure);
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