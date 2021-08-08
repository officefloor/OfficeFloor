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

package net.officefloor.compile.impl.team;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link TeamLoader} in loading a {@link OfficeFloorTeamSourceType}.
 *
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorTeamSourceTypeTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load via {@link ClassManagedObjectSource} {@link Class}.
	 */
	public void testLoad() {

		// Node
		final TeamNode node = this.createMock(TeamNode.class);

		// Properties for testing
		final String TEAM_SOURCE_NAME = "TEAM";
		final String MOCK_PROPERTY_VALUE = "MOCK";

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(MockLoadTeamSource.MOCK_PROPERTY).setValue(MOCK_PROPERTY_VALUE);

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the officeflooor team source type
		TeamLoader teamLoader = nodeContext.getTeamLoader(node);
		OfficeFloorTeamSourceType teamType = teamLoader.loadOfficeFloorTeamSourceType(TEAM_SOURCE_NAME,
				MockLoadTeamSource.class, properties);
		MockLoadTeamSource.assertOfficeFloorTeamSourceType(teamType, TEAM_SOURCE_NAME, MOCK_PROPERTY_VALUE);
	}

	/**
	 * Ensures issue if failure in obtaining {@link TeamSourceSpecification}.
	 */
	public void testFailGetTeamSourceSpecification() {

		// Node
		final TeamNode node = this.createMock(TeamNode.class);

		final Error failure = new Error("specification failure");
		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record failure to instantiate
		this.recordReturn(node, node.getQualifiedName(), "team");
		issues.recordIssue("team", node.getClass(),
				"Failed to obtain TeamSourceSpecification from " + MockTeamSource.class.getName(), failure);

		// Attempt to obtain specification
		MockTeamSource.reset();
		MockTeamSource.specificationFailure = failure;
		this.replayMockObjects();

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the OfficeFlooor managed object source type
		TeamLoader teamLoader = nodeContext.getTeamLoader(node);
		OfficeFloorTeamSourceType teamType = teamLoader.loadOfficeFloorTeamSourceType("team", MockTeamSource.class,
				compiler.createPropertyList());

		// Ensure not loaded
		TestCase.assertNull("Should not load type", teamType);

		this.verifyMockObjects();
	}

	/**
	 * Mock {@link TeamSource} for testing.
	 */
	@TestSource
	public static class MockTeamSource implements TeamSource {

		/**
		 * Failure to obtain the {@link TeamSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * Resets the state for next test.
		 */
		public static void reset() {
			specificationFailure = null;
		}

		/*
		 * ================ TeamSource ================================
		 */

		@Override
		public TeamSourceSpecification getSpecification() {
			throw specificationFailure;
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			fail("Should not require creating team for types");
			return null;
		}
	}

}
