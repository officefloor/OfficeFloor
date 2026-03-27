/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.function.executive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.TeamSourceContextWrapper;
import net.officefloor.frame.api.executive.TeamSourceContextWrapper.WorkerEnvironment;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure {@link Executive} can wrap the {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ExecutiveTeamTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure the {@link Team} can not have oversight.
	 */
	@Test
	public void teamWithoutOversight() throws Exception {
		this.doExecutiveTeamTest(false, false);
	}

	/**
	 * Ensure the {@link Executive} can wrap {@link Team}.
	 */
	@Test
	public void executiveOverseeTeam() throws Exception {
		this.doExecutiveTeamTest(true, true);
	}

	/**
	 * Ensure the {@link Executive} can wrap {@link Thread} of {@link Team}.
	 */
	@Test
	public void executiveWrapWorker() throws Exception {
		this.doExecutiveTeamTest(true, true);
	}

	/**
	 * Ensure the {@link Executive} can wrap {@link Team}.
	 */
	public void doExecutiveTeamTest(boolean isTeamOversight, boolean isWrapWorker) throws Exception {

		// Construct Executive
		MockExecutiveSource.isWrapWorker = isWrapWorker;
		this.construct.getOfficeFloorBuilder().setExecutive(MockExecutiveSource.class);

		// Construct the team (4 threads)
		MockTeamSource.teamSize = -1;
		TeamBuilder<?> team = this.construct.constructTeam("FUNCTION_TEAM", MockTeamSource.class);
		team.setTeamSize(4);
		if (!isTeamOversight) {
			team.requestNoTeamOversight();
		}

		// Create the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "function");
		task.getBuilder().setResponsibleTeam("FUNCTION_TEAM");

		// Construct the OfficeFloor (allows startup processes to run)
		MockExecutiveSource.isOpeningOfficeFloor = true;
		MockExecutiveSource.isControlTeam = false;
		MockExecutiveSource.worker = null;
		this.construct.constructOfficeFloor().openOfficeFloor();

		// Invoke the function
		MockExecutiveSource.isOpeningOfficeFloor = false;
		MockExecutiveSource.isInterceptTeam = false;
		this.construct.invokeFunction("function", null);

		// Ensure function invoked
		assertTrue(work.isFunctionInvoked, "Function should be invoked");

		// Determine if team oversight
		if (!isTeamOversight) {

			// Should not intercept by executive
			assertFalse(MockExecutiveSource.isControlTeam, "Executive should not control team");
			assertFalse(MockExecutiveSource.isInterceptTeam, "Executive should not intercept Job");

		} else {

			// Ensure intercepted by executive
			assertTrue(MockExecutiveSource.isControlTeam, "Executive should control teams");
			assertTrue(MockExecutiveSource.isInterceptTeam, "Executive should intercept Job");

			// Determine if wrap worker
			if (!isWrapWorker) {
				assertNull(MockExecutiveSource.worker, "Should not wrap worker");
				assertEquals("of-FUNCTION_TEAM-1", work.functionThread.getName(), "Incorrect thread name");
				assertEquals(4, MockTeamSource.teamSize, "Should pass through team size");
			} else {
				assertNotNull(MockExecutiveSource.worker, "Should wrap worker");
				assertEquals("of-FUNCTION_TEAM-EXECUTIVE-1", work.functionThread.getName(), "Incorrect thread name");
				assertEquals(2, MockTeamSource.teamSize, "Executive should be able to control team size");
			}
		}

		// Ensure provided thread factory uses thread decoration
		this.construct.assertThreadUsed(work.functionThread);
	}

	@TestSource
	public static class MockExecutiveSource extends DefaultExecutive
			implements Executive, TeamOversight, Team, WorkerEnvironment {

		private static boolean isOpeningOfficeFloor = true;

		private static boolean isControlTeam = false;

		private static boolean isWrapWorker = false;

		private Team team;

		private volatile ProcessIdentifier processIdentifier = null;

		private static volatile boolean isInterceptTeam = false;

		private static volatile Runnable worker = null;

		/*
		 * ================= Executive ======================
		 */

		@Override
		public ProcessIdentifier createProcessIdentifier(ExecutiveOfficeContext context) {

			// Create appropriate processes to start OfficeFloor
			if (isOpeningOfficeFloor) {
				return new ProcessIdentifier() {
				};
			}

			// Ensure only one process created
			assertNull(this.processIdentifier, "Should only create one process in test");
			this.processIdentifier = new ProcessIdentifier() {
			};
			return this.processIdentifier;
		}

		@Override
		public TeamOversight getTeamOversight() {
			return isWrapWorker ? this : null;
		}

		/*
		 * ================ TeamOversight ====================
		 */

		@Override
		public Team createTeam(ExecutiveContext context) throws Exception {

			// Ensure correct logger
			assertEquals("of-FUNCTION_TEAM", context.getLogger().getName(), "Incorrect logging team name");

			// Indicate controlling the team
			isControlTeam = true;

			// Ensure correct team size
			assertEquals(4, context.getTeamSize(), "Incorrect team size");

			// Create the team source context
			TeamSourceContext teamContext = context;
			if (!context.isRequestNoTeamOversight()) {
				teamContext = new TeamSourceContextWrapper(context, (size) -> 2, "EXECUTIVE", this);
			}

			// Create the team
			this.team = context.getTeamSource().createTeam(teamContext);

			// Return this to intercept team
			return this;
		}

		/*
		 * ================= Team ======================
		 */

		@Override
		public void startWorking() {
			this.team.startWorking();
		}

		@Override
		public void assignJob(Job job) throws Exception {

			// Ensure using process identifier
			assertNotNull(this.processIdentifier, "Should have assigned process identifier");
			assertSame(this.processIdentifier, job.getProcessIdentifier(), "Incorrect process identifier");

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
