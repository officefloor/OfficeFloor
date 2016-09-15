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
package net.officefloor.compile.impl.team;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.MockCompilerIssues;
import net.officefloor.compile.impl.structure.TeamNodeImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;
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
		final Node node = this.createMock(Node.class);

		// Properties for testing
		final String TEAM_SOURCE_NAME = "TEAM";
		final String MOCK_PROPERTY_VALUE = "MOCK";

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(MockLoadTeamSource.MOCK_PROPERTY).setValue(
				MOCK_PROPERTY_VALUE);

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the officeflooor team source type
		TeamLoader teamLoader = nodeContext.getTeamLoader(node);
		OfficeFloorTeamSourceType teamType = teamLoader
				.loadOfficeFloorTeamSourceType(TEAM_SOURCE_NAME,
						MockLoadTeamSource.class, properties);
		MockLoadTeamSource.assertOfficeFloorTeamSourceType(teamType,
				TEAM_SOURCE_NAME, MOCK_PROPERTY_VALUE);
	}

	/**
	 * Ensures issue if failure in obtaining {@link TeamSourceSpecification}.
	 */
	public void testFailGetTeamSourceSpecification() {

		// Node
		final Node node = this.createMock(Node.class);

		final Error failure = new Error("specification failure");
		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record failure to instantiate
		issues.recordIssue("team", TeamNodeImpl.class,
				"Failed to obtain TeamSourceSpecification from "
						+ MockTeamSource.class.getName(), failure);

		// Attempt to obtain specification
		MockTeamSource.reset();
		MockTeamSource.specificationFailure = failure;
		this.replayMockObjects();

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the officeflooor managed object source type
		TeamLoader teamLoader = nodeContext.getTeamLoader(node);
		OfficeFloorTeamSourceType teamType = teamLoader
				.loadOfficeFloorTeamSourceType("team", MockTeamSource.class,
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