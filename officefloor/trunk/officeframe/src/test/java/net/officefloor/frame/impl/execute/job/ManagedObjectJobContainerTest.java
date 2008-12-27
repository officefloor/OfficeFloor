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

import net.officefloor.frame.impl.execute.AbstractJobContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests the {@link AbstractJobContainer} with {@link ManagedObject} instances.
 * 
 * @author Daniel
 */
public class ManagedObjectJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures execution of {@link Job} invoked with {@link ManagedObject}.
	 */
	public void testExecuteJobWithManagedObject() {

		// Create a job to use the managed object
		final Object moObject = "ManagedObject Object";
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				// Ensure get the managed object
				Object object = context.getObject(0);
				assertEquals("Incorrect managed object", moObject, object);
				return null;
			}
		});

		// Record actions
		this.record_JobContainer_initialSteps(null, 0);
		this.record_WorkContainer_loadManagedObjects(job, true);
		this.record_WorkContainer_coordinateManagedObjects(job);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(0, moObject);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

	/**
	 * Ensures execution of {@link Job} invoked with an
	 * {@link AsynchronousManagedObject} that takes time to load.
	 */
	public void testExecuteJobWithAsynchronousLoadManagedObject() {

		// Create a job to use the asynchronous managed object
		final Object moObject = "AsynchronousManagedObject Object";
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				// Ensure get the managed object
				Object object = context.getObject(0);
				assertEquals("Incorrect managed object", moObject, object);
				return null;
			}
		});

		// Record actions of attempt to load managed objects
		this.record_JobContainer_initialSteps(null, 0);
		this.record_WorkContainer_loadManagedObjects(job, false);
		this.record_JobActivatableSet_activateJobs();

		// Record actions on managed object now loaded
		this.record_JobContainer_initialSteps(null, 0);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_coordinateManagedObjects(job);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(0, moObject);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// First attempt, but managed object not loaded
		this.doJob(job, true);

		// Second attempt, with managed object loaded
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

	/**
	 * Ensures execution of {@link Job} invoked with a
	 * {@link CoordinatingManagedObject} that takes time to coordinate.
	 */
	public void testExecuteJobWithTimelyCoordinateManagedObject() {

		// Create a job to use the asynchronous managed object
		final Object moObject = "CoordinatingManagedObject Object";
		Job job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				// Ensure get the managed object
				Object object = context.getObject(1);
				assertEquals("Incorrect managed object", moObject, object);
				return null;
			}
		});

		// Record actions of attempt to load managed objects
		this.record_JobContainer_initialSteps(null, 0, 1);
		this.record_WorkContainer_loadManagedObjects(job, true);
		this.record_WorkContainer_coordinateManagedObjects(job);
		this.record_WorkContainer_isManagedObjectsReady(job, false);
		this.record_JobActivatableSet_activateJobs();

		// Record actions on managed object now loaded
		this.record_JobContainer_initialSteps(null, 0, 1);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(1, moObject);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// First attempt, but coordinating taking time
		this.doJob(job, true);

		// Second attempt, with managed object coordinated
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

}
