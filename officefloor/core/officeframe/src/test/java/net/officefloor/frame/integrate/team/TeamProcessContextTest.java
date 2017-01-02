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
package net.officefloor.frame.integrate.team;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensures that {@link ProcessContextListener} provides appropriate
 * {@link ProcessState} identifier.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamProcessContextTest extends AbstractOfficeConstructTestCase {

	/**
	 * Processing steps through the test.
	 */
	public static enum ProcessingStep {
		START, PROCESS_CREATED, ASSIGN_JOB, DO_TASK, PROCESS_COMPLETED
	}

	/**
	 * Current {@link ProcessingStep}.
	 */
	private static volatile ProcessingStep currentStep = ProcessingStep.START;

	/**
	 * Ensures the invoking {@link Thread} can be associated with the
	 * {@link ProcessState} context.
	 */
	public void testThreadAssociatedToProcessContext() throws Exception {

		// Reset current step
		currentStep = ProcessingStep.START;

		// Register the function
		MockWork work = new MockWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		function.getBuilder().setTeam("TEAM");

		// Register the team
		this.constructTeam("TEAM", MockListenerTeamSource.class);

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure all steps were taken
		assertEquals("Incorrect steps undertaken", ProcessingStep.PROCESS_COMPLETED, currentStep);
	}

	/**
	 * Mock functionality for testing.
	 */
	public static class MockWork {

		/**
		 * {@link ManagedFunction} to be executed.
		 */
		public void task() {
			assertEquals("Incorrect step", ProcessingStep.ASSIGN_JOB, currentStep);

			// Increment step
			currentStep = ProcessingStep.DO_TASK;
		}
	}

	/**
	 * Mock {@link TeamSource} for testing the {@link ProcessContextListener}.
	 */
	public static class MockListenerTeamSource extends AbstractTeamSource implements ProcessContextListener, Team {

		/**
		 * {@link ProcessContextListener}.
		 */
		private volatile Object identifier = null;

		/*
		 * ==================== TeamSource ==================================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {

			// Register as Process Context Listener
			context.registerProcessContextListener(this);

			// Return this as the team
			return this;
		}

		/*
		 * ==================== ProcessContextListener =======================
		 */

		@Override
		public void processCreated(Object processIdentifier) {
			assertEquals("Incorrect step", ProcessingStep.START, currentStep);
			assertNull("Process Identifier must not yet be specified", this.identifier);
			this.identifier = processIdentifier;
			assertNotNull("Must have Process Identifier", this.identifier);

			// Increment step
			currentStep = ProcessingStep.PROCESS_CREATED;
		}

		@Override
		public void processCompleted(Object processIdentifier) {
			assertEquals("Incorrect step", ProcessingStep.DO_TASK, currentStep);
			assertNotNull("Must have Process Identifier at this point", this.identifier);
			assertSame("Incorrect completed Process Identifier", this.identifier, processIdentifier);

			// Clear the Process Identifier as completed
			this.identifier = null;

			// Increment step
			currentStep = ProcessingStep.PROCESS_COMPLETED;
		}

		/*
		 * ============================ Team =================================
		 */

		@Override
		public void startWorking() {
			// Only validating process identifier
		}

		@Override
		public void assignJob(Job job) {
			assertEquals("Incorrect step", ProcessingStep.PROCESS_CREATED, currentStep);
			assertNotNull("Must have Process Identifier at this point", this.identifier);

			// Ensure the correct Process Identifier
			Object processIdentifier = job.getProcessIdentifier();
			assertSame("Incorrect Process Identifier for Job", this.identifier, processIdentifier);

			// Increment step
			currentStep = ProcessingStep.ASSIGN_JOB;

			// Execute the Job
			job.run();
		}

		@Override
		public void stopWorking() {
			// Only validating process identifier
		}
	}

}