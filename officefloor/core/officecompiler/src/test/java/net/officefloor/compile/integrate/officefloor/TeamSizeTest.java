/*-
 * #%L
 * OfficeCompiler
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
