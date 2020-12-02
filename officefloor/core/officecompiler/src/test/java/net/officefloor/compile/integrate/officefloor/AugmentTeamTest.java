/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.integrate.officefloor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure able to augment {@link Team} instances.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class AugmentTeamTest {

	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * Ensure notifies of issue.
	 */
	@Test
	public void addIssue() throws Exception {

		// Record the issues
		final Exception exception = new Exception("TEST");
		MockCompilerIssues issues = new MockCompilerIssues(this.mocks);
		issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class, "Issue One");
		issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class, "Issue Two", exception);

		// Test
		this.mocks.replayMockObjects();
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Add team (to ensure visit)
			deployer.addTeam("team", AugmentTeamSource.class.getName());

			// Augment
			deployer.addTeamAugmentor((augment) -> {

				// Add the issue
				augment.addIssue("Issue One");

				// Add issue with exception
				throw augment.addIssue("Issue Two", exception);
			});
		});
		assertNull(compile.compileOfficeFloor(), "Should not compile");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link TeamOversight}.
	 */
	@Test
	public void allowTeamOversight() throws Exception {
		this.doAugmentedTeamTest(false, false);
	}

	/**
	 * Ensure indicates already linked.
	 */
	@Test
	public void teamAlreadyRequestNoOversight() throws Exception {
		this.doAugmentedTeamTest(true, false);
	}

	/**
	 * Ensure can augment the request for no {@link TeamOversight}.
	 */
	@Test
	public void augmentRequestNoTeamOversight() throws Exception {
		this.doAugmentedTeamTest(false, true);
	}

	/**
	 * Ensure can flag both in configuration and augmenting request for no
	 * {@link TeamOversight}.
	 */
	@Test
	public void bothConfigureAndAugmentRequestForNoTeamOversight() throws Exception {
		this.doAugmentedTeamTest(true, true);
	}

	/**
	 * Undertakes the augment {@link Team} test.
	 * 
	 * @param isRequestNoTeamOversight Indicates to request no
	 *                                 {@link TeamOversight}.
	 * @param isAugmentNoTeamOversight Indicates augment requests no
	 *                                 {@link TeamOversight}.
	 */
	private void doAugmentedTeamTest(boolean isRequestNoTeamOversight, boolean isAugmentNoTeamOversight)
			throws Exception {

		// Create the augment
		AugmentTeamSource teamSource = new AugmentTeamSource();
		AugmentExecutiveSource executiveSource = new AugmentExecutiveSource(new AugmentTeamSource());

		// Compile with the augmented managed object source
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Add the team
			final String TEAM_NAME = "TEAM";
			OfficeFloorTeam team = deployer.addTeam(TEAM_NAME, teamSource);
			if (isRequestNoTeamOversight) {
				team.requestNoTeamOversight();
			}

			// Add the executive
			deployer.setExecutive(executiveSource);

			// Augment the managed object source
			deployer.addTeamAugmentor((augment) -> {

				// Ensure correct name
				assertEquals(TEAM_NAME, augment.getTeamName(), "Incorrect team name");
				assertNotNull(augment.getTeamType(), "Should have team type");

				// Request possible no team oversight
				if (isAugmentNoTeamOversight) {
					augment.requestNoTeamOversight();
				}
			});
		});
		compile.compileAndOpenOfficeFloor();

		// Ensure appropriate request on team oversight
		assertEquals(isRequestNoTeamOversight || isAugmentNoTeamOversight, executiveSource.isRequestNoTeamOversight,
				"Should have team oversight on team");
	}

	@TestSource
	public static class AugmentTeamSource extends AbstractTeamSource implements Team {

		/*
		 * ==================== TeamSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ======================== Team ============================
		 */

		@Override
		public void startWorking() {
		}

		@Override
		public void assignJob(Job job) {
		}

		@Override
		public void stopWorking() {
		}
	}

	@TestSource
	public static class AugmentExecutiveSource extends DefaultExecutive
			implements ExecutionStrategy, ThreadFactory, TeamOversight {

		private final TeamSource teamSource;

		private boolean isRequestNoTeamOversight = false;

		private AugmentExecutiveSource(TeamSource teamSource) {
			this.teamSource = teamSource;
		}

		/*
		 * ===================== ExecutiveSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ======================= Executive ===========================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[] { this };
		}

		@Override
		public TeamOversight getTeamOversight() {
			return this;
		}

		/*
		 * ==================== ExecutionStrategy =======================
		 */

		@Override
		public String getExecutionStrategyName() {
			return "TEST";
		}

		@Override
		public ThreadFactory[] getThreadFactories() {
			return new ThreadFactory[] { this };
		}

		/*
		 * ===================== ThreadFactory ===========================
		 */

		@Override
		public Thread newThread(Runnable r) {
			return fail("Should not create thread");
		}

		/*
		 * ===================== TeamOversight ===========================
		 */

		@Override
		public Team createTeam(ExecutiveContext context) throws Exception {
			this.isRequestNoTeamOversight = context.isRequestNoTeamOversight();
			return context.isRequestNoTeamOversight() ? context.getTeamSource().createTeam(context)
					: this.teamSource.createTeam(context);
		}
	}

}
