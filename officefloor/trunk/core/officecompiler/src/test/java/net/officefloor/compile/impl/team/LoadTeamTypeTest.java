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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link TeamType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadTeamTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * Ensure issue if missing property.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.record_issue("Missing property 'missing'");

		// Attempt to load
		this.loadTeamType(false, new Loader() {
			@Override
			public void sourceTeam(TeamSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.record_issue("Can not load class 'missing'");

		// Attempt to load
		this.loadTeamType(false, new Loader() {
			@Override
			public void sourceTeam(TeamSourceContext context) throws Exception {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing class
		this.record_issue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadTeamType(false, new Loader() {
			@Override
			public void sourceTeam(TeamSourceContext context) throws Exception {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, null, AssetType.TEAM,
				null, issueDescription);
	}

	/**
	 * Loads the {@link TeamType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link TeamType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link TeamType}.
	 */
	private TeamType loadTeamType(boolean isExpectedToLoad, Loader loader,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the team loader and load the team
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		TeamLoader teamLoader = compiler.getTeamLoader();
		MockLoadTeamSource.loader = loader;
		TeamType teamType = teamLoader.loadTeamType(MockLoadTeamSource.class,
				propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the team type", teamType);
		} else {
			assertNull("Should not load the team type", teamType);
		}

		// Return the team type
		return teamType;
	}

	/**
	 * Implemented to load the {@link TeamType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link TeamType}.
		 * 
		 * @param context
		 *            {@link TeamSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link TeamType}.
		 */
		void sourceTeam(TeamSourceContext context) throws Exception;
	}

	/**
	 * Mock {@link TeamSource} for testing.
	 */
	@TestSource
	public static class MockLoadTeamSource implements TeamSource, Team {

		/**
		 * {@link Loader} to load the {@link TeamType}.
		 */
		public static Loader loader;

		/**
		 * Failure in instantiating an instance.
		 */
		public static RuntimeException instantiateFailure;

		/**
		 * Resets the state for the next test.
		 */
		public static void reset() {
			loader = null;
			instantiateFailure = null;
		}

		/**
		 * Default constructor.
		 */
		public MockLoadTeamSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ TeamSource ======================================
		 */

		@Override
		public TeamSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining team type");
			return null;
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			loader.sourceTeam(context);
			return this;
		}

		/*
		 * ======================== Team =======================================
		 */

		@Override
		public void startWorking() {
			fail("Should not be invoked in obtaining team type");
		}

		@Override
		public void assignJob(Job job, TeamIdentifier assignerTeam) {
			fail("Should not be invoked in obtaining team type");
		}

		@Override
		public void stopWorking() {
			fail("Should not be invoked in obtaining team type");
		}
	}

}