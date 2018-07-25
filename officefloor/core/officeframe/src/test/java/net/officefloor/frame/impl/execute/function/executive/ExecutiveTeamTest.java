/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.function.executive;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.TeamSourceContextWrapper;
import net.officefloor.frame.api.executive.TeamSourceContextWrapper.WorkerEnvironment;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link Executive} can wrap the {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveTeamTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the {@link Executive} can wrap {@link Team}.
	 */
	public void testExecutiveWrapTeam() throws Exception {
		this.doExecutiveTeamTest(false);
	}

	/**
	 * Ensure the {@link Executive} can wrap {@link Thread} of {@link Team}.
	 */
	public void testExecutiveWrapWorker() throws Exception {
		this.doExecutiveTeamTest(true);
	}

	/**
	 * Ensure the {@link Executive} can wrap {@link Team}.
	 */
	public void doExecutiveTeamTest(boolean isWrapWorker) throws Exception {

		// Construct Executive
		MockExecutiveSource.isWrapWorker = isWrapWorker;
		this.getOfficeFloorBuilder().setExecutive(MockExecutiveSource.class);

		// Construct the team (4 threads)
		MockTeamSource.teamSize = -1;
		this.constructTeam("FUNCTION_TEAM", MockTeamSource.class).setTeamSize(4);

		// Create the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "function");
		task.getBuilder().setResponsibleTeam("FUNCTION_TEAM");

		// Invoke the function
		MockExecutiveSource.isInterceptTeam = false;
		MockExecutiveSource.worker = null;
		this.invokeFunction("function", null);

		// Ensure intercepted by executive
		assertTrue("Function should be invoked", work.isFunctionInvoked);
		assertTrue("Executive should intercept Job", MockExecutiveSource.isInterceptTeam);

		// Determine if wrap worker
		if (!isWrapWorker) {
			assertNull("Should not wrap worker", MockExecutiveSource.worker);
			assertEquals("Incorrect thread name", "FUNCTION_TEAM", work.functionThread.getName());
			assertEquals("Should pass through team size", 4, MockTeamSource.teamSize);
		} else {
			assertNotNull("Should wrap worker", MockExecutiveSource.worker);
			assertEquals("Incorrect thread name", "FUNCTION_TEAM-EXECUTIVE", work.functionThread.getName());
			assertEquals("Executive should be able to control team size", 2, MockTeamSource.teamSize);
		}

		// Ensure provided thread factory uses thread decoration
		this.assertThreadUsed(work.functionThread);
	}

	@TestSource
	public static class MockExecutiveSource extends AbstractExecutiveSource
			implements Executive, Team, WorkerEnvironment {

		private static boolean isWrapWorker = false;

		private Team team;

		private volatile Object processIdentifier = null;

		private static volatile boolean isInterceptTeam = false;

		private static volatile Runnable worker = null;

		/*
		 * =============== ExecutiveSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ================= Executive ======================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[0];
		}

		@Override
		public Team createTeam(ExecutiveContext context) throws Exception {

			// Ensure team source
			assertTrue("Incorrect team source", context.getTeamSource() instanceof OnePersonTeamSource);

			// Ensure correct team size
			assertEquals("Incorrect team size", 4, context.getTeamSize());

			// Create the team source context
			TeamSourceContext teamContext = context;
			if (isWrapWorker) {
				teamContext = new TeamSourceContextWrapper(teamContext, 2, "EXECUTIVE", this);
			}

			// Create the team
			this.team = context.getTeamSource().createTeam(teamContext);

			// Return this to intercept team
			return this;
		}

		@Override
		public Object createProcessIdentifier() {
			assertNull("Should only create one process in test", this.processIdentifier);
			this.processIdentifier = new Object();
			return this.processIdentifier;
		}

		/*
		 * ================= Executive ======================
		 */

		@Override
		public void startWorking() {
			this.team.startWorking();
		}

		@Override
		public void assignJob(Job job) {

			// Ensure using process identifier
			assertNotNull("Should have assigned process identifier", this.processIdentifier);
			assertSame("Incorrect process identifier", this.processIdentifier, job.getProcessIdentifier());

			// Indicate intercepted team
			isInterceptTeam = true;

			// Delegate to actual team
			this.team.assignJob(job);
		}

		@Override
		public void stopWorking() {
			this.team.stopWorking();
		}

		/*
		 * ============== WorkerEnvironment ===================
		 */

		@Override
		public Runnable createWorkerEnvironment(Runnable worker) {
			return () -> {
				MockExecutiveSource.worker = worker;
				worker.run();
			};
		}
	}

	@TestSource
	public static class MockTeamSource extends OnePersonTeamSource {

		private static int teamSize;

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			teamSize = context.getTeamSize();
			return super.createTeam(context);
		}
	}

	public class TestWork {

		public volatile boolean isFunctionInvoked = false;

		public volatile Thread functionThread = null;

		public void function() {
			this.isFunctionInvoked = true;
			this.functionThread = Thread.currentThread();
		}
	}

}