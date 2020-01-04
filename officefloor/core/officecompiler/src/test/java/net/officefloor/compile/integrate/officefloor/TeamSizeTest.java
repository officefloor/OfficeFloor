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

import java.util.function.Consumer;

import net.officefloor.compile.impl.structure.TeamNodeImpl;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure enforces configuration of {@link Team} size if required.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSizeTest extends OfficeFrameTestCase {

	/**
	 * Ensure {@link CompilerIssue} if {@link Team} size not specified when
	 * required.
	 */
	public void testIssueAsRequireTeamSize() throws Exception {
		this.doTeamSizeTest((issues) -> {
			issues.recordIssue("TEAM", TeamNodeImpl.class, "Team size must be specified for team 'TEAM'");
		}, (deployer) -> {
			deployer.addTeam("TEAM", new SizedTeamSource(true));
		});
	}

	/**
	 * Ensure {@link CompilerIssue} if {@link Team} size is invalid.
	 */
	public void testIssueAsInvalidTeamSize() throws Exception {
		this.doTeamSizeTest((issues) -> {
			issues.recordIssue("TEAM", TeamNodeImpl.class, "Invalid size (0) for team 'TEAM'.  Must be 1 or more.");
		}, (deployer) -> {
			OfficeFloorTeam team = deployer.addTeam("TEAM", new SizedTeamSource(true));
			team.setTeamSize(0);
		});
	}

	/**
	 * Ensure can provide {@link Team} size.
	 */
	public void testProvideTeamSize() throws Exception {
		this.doTeamSizeTest(null, (deployer) -> {
			OfficeFloorTeam team = deployer.addTeam("TEAM", new SizedTeamSource(true));
			team.setTeamSize(1);
		});
	}

	/**
	 * Ensure require {@link Team} size.
	 */
	public void testNotRequireTeamSize() throws Exception {
		this.doTeamSizeTest(null, (deployer) -> {
			deployer.addTeam("TEAM", new SizedTeamSource(false));
		});
	}

	/**
	 * Undertakes the {@link Team} size test.
	 * 
	 * @param issueLoader {@link Consumer} to load the {@link CompilerIssue}. May be
	 *                    <code>null</code>.
	 * @param teamLoader  Loads the {@link Team}.
	 */
	private void doTeamSizeTest(Consumer<MockCompilerIssues> issueLoader, Consumer<OfficeFloorDeployer> teamLoader)
			throws Exception {

		// Record the issues
		MockCompilerIssues issues = new MockCompilerIssues(this);
		if (issueLoader != null) {
			issueLoader.accept(issues);
		}

		// Test
		this.replayMockObjects();
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			teamLoader.accept(deployer);
		});
		if (issueLoader != null) {
			// Issues so should not compile
			assertNull("Should not compile", compile.compileOfficeFloor());
		} else {
			// No issues, so should compile
			assertNotNull("Should compile", compile.compileOfficeFloor());
		}
		this.verifyMockObjects();

	}

	@TestSource
	public static class SizedTeamSource extends AbstractTeamSource implements Team {

		private final boolean isRequireTeamSize;

		public SizedTeamSource(boolean isRequireTeamSize) {
			this.isRequireTeamSize = isRequireTeamSize;
		}

		/*
		 * ============== TeamSource ===========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			if (this.isRequireTeamSize) {
				context.getTeamSize();
			}
			return this;
		}

		/*
		 * ================= Team ==============================
		 */

		@Override
		public void startWorking() {
			fail("Should not use team");
		}

		@Override
		public void assignJob(Job job) {
			fail("Should not use team");
		}

		@Override
		public void stopWorking() {
			fail("Should not use team");
		}
	}

}
