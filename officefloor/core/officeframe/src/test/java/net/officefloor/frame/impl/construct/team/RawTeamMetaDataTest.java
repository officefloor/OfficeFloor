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
package net.officefloor.frame.impl.construct.team;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareContext;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawTeamMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawTeamMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Team}.
	 */
	private final String TEAM_NAME = "TEAM NAME";

	/**
	 * {@link TeamConfiguration}.
	 */
	private final TeamConfiguration<?> configuration = this.createMock(TeamConfiguration.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this.createMock(SourceContext.class);

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor = this.createMock(ThreadLocalAwareExecutor.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if no {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record no team available
		this.recordReturn(this.configuration, this.configuration.getTeamName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OfficeFloor", "Team added without a name");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link TeamSource}.
	 */
	public void testNoTeamSource() {

		// Record no source available
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), null);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "No TeamSource class provided");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to instantiate the {@link TeamSource}.
	 */
	public void testTeamSourceInstantiateFailure() {

		final Exception failure = new Exception("Instantiate failure");

		// Record team source that fails instantiation
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), FailInstantiateTeamSource.class);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Failed to instantiate " + FailInstantiateTeamSource.class.getName(), failure);

		// Attempt to construct team
		this.replayMockObjects();
		FailInstantiateTeamSource.instantiateFailure = failure;
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that will be failed to instantiate.
	 */
	@TestSource
	public static class FailInstantiateTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Exception} to be thrown on instantiating.
		 */
		public static Exception instantiateFailure;

		/**
		 * Constructor that will fail instantiation.
		 * 
		 * @throws Exception
		 *             Failure to instantiate.
		 */
		public FailInstantiateTeamSource() throws Exception {
			throw instantiateFailure;
		}
	}

	/**
	 * Ensures issue if required property is not specified.
	 */
	public void testMissingProperty() {

		SourcePropertiesImpl noProperties = new SourcePropertiesImpl();

		// Record team source that has missing required property
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), NoPropertyTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), noProperties);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Must specify property '" + NoPropertyTeamSource.PROPERTY_NAME + "'");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that obtains a property and creates a {@link Team}.
	 */
	@TestSource
	public static class NoPropertyTeamSource extends TeamSourceAdapter {

		/**
		 * Name of the required property.
		 */
		public static final String PROPERTY_NAME = "required.property";

		/**
		 * Expected value for the property.
		 */
		public static final String PROPERTY_VALUE = "property.value";

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			String property = context.getProperty(PROPERTY_NAME);
			assertEquals("Incorrect property value", PROPERTY_VALUE, property);
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures issue if required class is not available.
	 */
	public void testMissingClass() {

		// Record team source that has missing required class
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), NoClassTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());
		this.sourceContext.loadClass(NoClassTeamSource.CLASS_NAME);
		this.control(this.sourceContext)
				.setThrowable(new UnknownClassError("TEST ERROR", NoClassTeamSource.CLASS_NAME));
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "Can not load class '" + NoClassTeamSource.CLASS_NAME + "'");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that obtains a {@link Class} and creates a
	 * {@link Team}.
	 */
	@TestSource
	public static class NoClassTeamSource extends TeamSourceAdapter {

		/**
		 * Name of the required {@link Class}.
		 */
		public static final String CLASS_NAME = "REQUIRED CLASS";

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			context.loadClass(CLASS_NAME);
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures issue if required resource is not available.
	 */
	public void testMissingResource() {

		// Record team source that has missing required resource
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), NoResourceTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());
		this.sourceContext.getResource(NoResourceTeamSource.RESOURCE_LOCATION);
		this.control(this.sourceContext)
				.setThrowable(new UnknownResourceError("TEST ERROR", NoResourceTeamSource.RESOURCE_LOCATION));
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Can not obtain resource at location '" + NoResourceTeamSource.RESOURCE_LOCATION + "'");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that obtains a resource and creates a {@link Team}.
	 */
	@TestSource
	public static class NoResourceTeamSource extends TeamSourceAdapter {

		/**
		 * Location of the required resource.
		 */
		public static final String RESOURCE_LOCATION = "REQUIRED RESOURCE";

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			context.getResource(RESOURCE_LOCATION);
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures handles failure to create the {@link Team}.
	 */
	public void testTeamCreateFailure() {

		final RuntimeException failure = new RuntimeException("create failure");

		// Record team source that fails to initialise
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), FailCreateTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "Failed to create Team", failure);

		// Attempt to construct team
		this.replayMockObjects();
		FailCreateTeamSource.createFailure = failure;
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that fails to create {@link Team}.
	 */
	@TestSource
	public static class FailCreateTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Exception} to be thrown on create.
		 */
		public static RuntimeException createFailure;

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			throw createFailure;
		}
	}

	/**
	 * Ensures indicates issue if no {@link Team} created.
	 */
	public void testNullTeamSourced() {

		// Record team source that fails to initialise
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), SourceTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "TeamSource failed to provide Team");

		// Attempt to construct team
		this.replayMockObjects();
		SourceTeamSource.team = null;
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain the {@link Team} name from the
	 * {@link TeamSourceContext}.
	 */
	public void testTeamNameAvailableFromContext() {

		final Team team = this.createMock(Team.class);

		// Record constructing team
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), NameTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());

		// Attempt to construct team
		this.replayMockObjects();
		NameTeamSource.expectedTeamName = TEAM_NAME;
		NameTeamSource.isInitialised = false;
		NameTeamSource.team = team;
		this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure team source initialise
		assertTrue("Team Source should be initialised", NameTeamSource.isInitialised);
	}

	/**
	 * {@link TeamSource} that validates {@link Team} name is available.
	 * 
	 * @author Daniel Sagenschneider
	 */
	@TestSource
	public static class NameTeamSource extends TeamSourceAdapter {

		/**
		 * Expected {@link Team} name.
		 */
		public static String expectedTeamName;

		/**
		 * Indicates if initialised.
		 */
		public static boolean isInitialised = false;

		/**
		 * {@link Team} to be returned.
		 */
		public static Team team;

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			assertFalse("Should only be initialised once", isInitialised);
			isInitialised = true;

			// Validate the team
			assertEquals("Incorrect team name", expectedTeamName, context.getTeamName());

			// Return the team
			return team;
		}
	}

	/**
	 * Ensures able to successfully source the {@link Team} and details of
	 * {@link RawTeamMetaData} are correct.
	 */
	public void testTeamSourced() {

		final Team team = this.createMock(Team.class);

		// Record constructing team
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(), SourceTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());

		// Attempt to construct team
		this.replayMockObjects();
		SourceTeamSource.team = team;
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertEquals("Incorrect team", team, metaData.getTeamManagement().getTeam());
		assertFalse("Should not be thread local aware", metaData.isRequireThreadLocalAwareness());
	}

	/**
	 * {@link TeamSource} that sources a {@link Team}.
	 */
	@TestSource
	public static class SourceTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Team} to be returned.
		 */
		public static Team team;

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return team;
		}
	}

	/**
	 * Ensure able to register a {@link ThreadLocalAwareTeam}.
	 */
	public void testRegisterThreadLocalAwareTeam() {

		MockThreadLocalAwareTeamSource.context = null;

		// Record constructing team
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration.getTeamSourceClass(),
				MockThreadLocalAwareTeamSource.class);
		this.recordReturn(this.configuration, this.configuration.getProperties(), new SourcePropertiesImpl());

		// Attempt to construct team
		this.replayMockObjects();
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertTrue("Should be thread local aware", metaData.isRequireThreadLocalAwareness());
		assertNotNull("Should have thread local aware context", MockThreadLocalAwareTeamSource.context);
	}

	/**
	 * Mock {@link ThreadLocalAwareTeam}.
	 */
	@TestSource
	public static class MockThreadLocalAwareTeamSource extends TeamSourceAdapter implements ThreadLocalAwareTeam {

		public static ThreadLocalAwareContext context = null;

		@Override
		public void setThreadLocalAwareness(ThreadLocalAwareContext context) {
			MockThreadLocalAwareTeamSource.context = context;
		}
	}

	/**
	 * Constructs the {@link RawTeamMetaDataImpl} with the mock objects.
	 * 
	 * @return {@link RawTeamMetaDataImpl}.
	 */
	private RawTeamMetaData constructRawTeamMetaData(boolean isExpectConstruction) {

		// Attempt to construct
		RawTeamMetaData metaData = RawTeamMetaDataImpl.getFactory().constructRawTeamMetaData(this.configuration,
				this.sourceContext, this.threadLocalAwareExecutor, this.issues);

		// Provide assertion on whether should be constructed
		if (isExpectConstruction) {
			assertNotNull("Should have constructed meta-data", metaData);
		} else {
			assertNull("Should not construct meta-data", metaData);
		}

		// Return the meta-data
		return metaData;
	}
}