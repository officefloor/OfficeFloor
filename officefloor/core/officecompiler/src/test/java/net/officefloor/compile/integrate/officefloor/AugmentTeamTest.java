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
package net.officefloor.compile.integrate.officefloor;

import java.util.concurrent.ThreadFactory;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to augment {@link Team} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AugmentTeamTest extends OfficeFrameTestCase {

	/**
	 * Ensure indicates already linked.
	 */
	public void testAugmentTeamAlreadyLinked() throws Exception {
		this.doAugmentedTeamTest(true);
	}

	/**
	 * Ensure can augment the {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceTeam() throws Exception {
		this.doAugmentedTeamTest(false);
	}

	/**
	 * Undertakes the augment {@link Team} test.
	 * 
	 * @param isAlreadyLinked Indicates if already linked.
	 */
	private void doAugmentedTeamTest(boolean isAlreadyLinked) throws Exception {

		// Create the augment
		AugmentTeamSource teamSource = new AugmentTeamSource();
		AugmentExecutiveSource executiveSource = new AugmentExecutiveSource();

		// Compile with the augmented managed object source
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Add the team
			final String TEAM_NAME = "TEAM";
			OfficeFloorTeam team = deployer.addTeam(TEAM_NAME, teamSource);

			// Add the executive
			OfficeFloorExecutive executive = deployer.setExecutive(executiveSource);
			OfficeFloorTeamOversight oversight = executive.getOfficeFloorTeamOversight("TEST");

			// Determine if already linked
			if (isAlreadyLinked) {
				deployer.link(team, oversight);
			}

			// Augment the managed object source
			deployer.addTeamAugmentor((augment) -> {

				// Ensure correct name
				assertEquals("Incorrect team name", TEAM_NAME, augment.getTeamName());

				// Determine if have oversight
				assertEquals("Incorrectly already linked", isAlreadyLinked, augment.isTeamOversight());

				// Possibly link team
				if (!augment.isTeamOversight()) {
					augment.setTeamOversight(oversight);
				}
			});
		});
		compile.compileAndOpenOfficeFloor();

		// Ensure team oversight on the team
		assertSame("Should have team oversight on team", teamSource, executiveSource.teamSource);
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
	public static class AugmentExecutiveSource extends AbstractExecutiveSource
			implements Executive, ExecutionStrategy, ThreadFactory, TeamOversight {

		private TeamSource teamSource;

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
		public TeamOversight[] getTeamOversights() {
			return new TeamOversight[] { this };
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
			fail("Should not create thread");
			return null;
		}

		/*
		 * ===================== TeamOversight ===========================
		 */

		@Override
		public String getTeamOversightName() {
			return "TEST";
		}

		@Override
		public Team createTeam(ExecutiveContext context) throws Exception {
			this.teamSource = context.getTeamSource();
			return TeamOversight.super.createTeam(context);
		}
	}

}