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

import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests the {@link AbstractManagedJobNodeContainer} with {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectJobContainerTest extends AbstractJobContainerTest {

	/**
	 * Ensures execution of {@link Job} invoked with {@link ManagedObject}.
	 */
	public void testExecuteJobWithManagedObject() {

		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);

		// Create a job to use the managed object
		final Object moObject = "ManagedObject Object";
		Job job = this.createJob(false, new ManagedObjectIndex[] { moIndex },
				null, null, new JobFunctionality() {
					@Override
					public Object executeFunctionality(
							JobFunctionalityContext context) throws Throwable {
						// Ensure get the managed object
						Object object = context.getObject(moIndex);
						assertEquals("Incorrect managed object", moObject,
								object);
						return null;
					}
				});

		// Record actions
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_loadManagedObjects(job);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_coordinateManagedObjects(job, true);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(moIndex, moObject);
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

		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);

		// Create a job to use the asynchronous managed object
		final Object moObject = "AsynchronousManagedObject Object";
		Job job = this.createJob(false, new ManagedObjectIndex[] { moIndex },
				null, null, new JobFunctionality() {
					@Override
					public Object executeFunctionality(
							JobFunctionalityContext context) throws Throwable {
						// Ensure get the managed object
						Object object = context.getObject(moIndex);
						assertEquals("Incorrect managed object", moObject,
								object);
						return null;
					}
				});

		// Record actions of attempt to load managed objects
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_loadManagedObjects(job);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_coordinateManagedObjects(job, false);
		this.record_JobActivatableSet_activateJobs();

		// Record actions on managed object now loaded
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_coordinateManagedObjects(job, true);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(moIndex, moObject);
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
	 * Ensures execution of {@link Job} invoked that can not coordinate until a
	 * dependency is ready.
	 */
	public void testExecuteJobWithCoordinatingWaitingOnDependency() {

		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);

		// Create a job to use the coordinating managed object
		final Object moObject = "CoordinatingManagedObject Object";
		Job job = this.createJob(false, new ManagedObjectIndex[] { moIndex },
				null, null, new JobFunctionality() {
					@Override
					public Object executeFunctionality(
							JobFunctionalityContext context) throws Throwable {
						// Ensure get the managed object
						Object object = context.getObject(moIndex);
						assertEquals("Incorrect managed object", moObject,
								object);
						return null;
					}
				});

		// Record actions of attempt to coordinate managed objects
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_loadManagedObjects(job);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_coordinateManagedObjects(job, false);
		this.record_JobActivatableSet_activateJobs();

		// Record actions on completing coordination
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_coordinateManagedObjects(job, true);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(moIndex, moObject);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// First attempt, but managed object not coordinated
		this.doJob(job, true);

		// Second attempt, with managed object coordinated
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

	/**
	 * Ensures execution of {@link Job} invoked with a
	 * {@link CoordinatingManagedObject} that triggers an asynchronous
	 * operation.
	 */
	public void testExecuteJobWithCoordinateTriggerAsynchronousOperation() {

		final ManagedObjectIndex moOne = this
				.createMock(ManagedObjectIndex.class);
		final ManagedObjectIndex moTwo = this
				.createMock(ManagedObjectIndex.class);

		// Create a job to use the asynchronous managed object
		final Object moObject = "CoordinatingManagedObject Object";
		Job job = this.createJob(false,
				new ManagedObjectIndex[] { moOne, moTwo }, null, null,
				new JobFunctionality() {
					@Override
					public Object executeFunctionality(
							JobFunctionalityContext context) throws Throwable {
						// Ensure get the managed object
						Object object = context.getObject(moTwo);
						assertEquals("Incorrect managed object", moObject,
								object);
						return null;
					}
				});

		// Record actions of attempt to coordinate managed objects
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_loadManagedObjects(job);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_coordinateManagedObjects(job, true);
		this.record_WorkContainer_isManagedObjectsReady(job, false);
		this.record_JobActivatableSet_activateJobs();

		// Record actions on managed object now ready
		this.record_JobContainer_initialSteps(job, null);
		this.record_WorkContainer_governManagedObjects(job, false, false);
		this.record_WorkContainer_isManagedObjectsReady(job, true);
		this.record_WorkContainer_getObject(moTwo, moObject);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// First attempt, coordinating triggering asynchronous operation
		this.doJob(job, true);

		// Second attempt, with managed object coordinated
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

}