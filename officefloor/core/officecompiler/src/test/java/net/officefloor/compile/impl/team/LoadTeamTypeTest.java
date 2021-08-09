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

import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;
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
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * Ensure issue if missing property.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Must specify property 'missing'");

		// Attempt to load
		this.loadTeamType(false, (context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing'");

		// Attempt to load
		this.loadTeamType(false, (context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing class
		this.issues.recordIssue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadTeamType(false, (context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadTeamType(false, (context) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadTeamType(false, (context) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure can load the {@link TeamType}.
	 */
	public void testLoadTeamTypeWithoutSize() {
		TeamType type = this.loadTeamType(true, (context) -> {
			// do not request team size
		});
		assertNotNull("Should load the team type", type);
		assertFalse("Should not require team size", type.isRequireTeamSize());
	}

	/**
	 * Ensure can load the {@link TeamType} requiring a {@link Team} size.
	 */
	public void testLoadTeamTypeRequiringSize() {
		TeamType type = this.loadTeamType(true, (context) -> {
			assertEquals("Calling to get size and ensuring valid value for creating a team", 10, context.getTeamSize());
		});
		assertNotNull("Should load the team type", type);
		assertTrue("Should require team size", type.isRequireTeamSize());
	}

	/**
	 * Loads the {@link TeamType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link TeamType}.
	 * @param loader                 {@link Loader}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link TeamType}.
	 */
	private TeamType loadTeamType(boolean isExpectedToLoad, Loader loader, String... propertyNameValuePairs) {

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
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		TeamLoader teamLoader = compiler.getTeamLoader();
		MockLoadTeamSource.loader = loader;
		TeamType teamType = teamLoader.loadTeamType("team", MockLoadTeamSource.class, propertyList);

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
		 * @param context {@link TeamSourceContext}.
		 * @throws Exception If fails to source {@link TeamType}.
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
		public void assignJob(Job job) {
			fail("Should not be invoked in obtaining team type");
		}

		@Override
		public void stopWorking() {
			fail("Should not be invoked in obtaining team type");
		}
	}

}
