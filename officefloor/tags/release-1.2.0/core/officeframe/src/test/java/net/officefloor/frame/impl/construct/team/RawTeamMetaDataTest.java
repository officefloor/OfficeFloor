/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
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
	private final TeamConfiguration<?> configuration = this
			.createMock(TeamConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if no {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record no team available
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Team added without a name");

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
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), null);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"No TeamSource class provided");

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
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), FailInstantiateTeamSource.class);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Failed to instantiate "
						+ FailInstantiateTeamSource.class.getName(), failure);

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
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), NoPropertyTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), noProperties);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Must specify property '" + NoPropertyTeamSource.PROPERTY_NAME
						+ "'");

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
		public void init(TeamSourceContext context) throws Exception {
			String property = context.getProperty(PROPERTY_NAME);
			assertEquals("Incorrect property value", PROPERTY_VALUE, property);
		}
	}

	/**
	 * Ensures issue if failure in init the {@link Team}.
	 */
	public void testTeamInitFailure() {

		final Exception failure = new Exception("init failure");

		// Record team source that fails to initialise
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), FailInitTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new SourcePropertiesImpl());
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Failed to initialise TeamSource", failure);

		// Attempt to construct team
		this.replayMockObjects();
		FailInitTeamSource.initFailure = failure;
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that fails to initialise.
	 */
	@TestSource
	public static class FailInitTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Exception} to be thrown on init.
		 */
		public static Exception initFailure;

		@Override
		public void init(TeamSourceContext context) throws Exception {
			throw initFailure;
		}
	}

	/**
	 * Ensures handles failure to create the {@link Team}.
	 */
	public void testTeamCreateFailure() {

		final RuntimeException failure = new RuntimeException("create failure");

		// Record team source that fails to initialise
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), FailCreateTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new SourcePropertiesImpl());
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Failed to create Team", failure);

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
		public Team createTeam() {
			throw createFailure;
		}
	}

	/**
	 * Ensures indicates issue if no {@link Team} created.
	 */
	public void testNullTeamSourced() {

		// Record team source that fails to initialise
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), SourceTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new SourcePropertiesImpl());
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"TeamSource failed to provide Team");

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
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), NameTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new SourcePropertiesImpl());

		// Attempt to construct team
		this.replayMockObjects();
		NameTeamSource.expectedTeamName = TEAM_NAME;
		NameTeamSource.isInitialised = false;
		NameTeamSource.team = team;
		this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure team source initialise
		assertTrue("Team Source should be initialised",
				NameTeamSource.isInitialised);
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
		public void init(TeamSourceContext context) throws Exception {
			assertFalse("Should only be initialised once", isInitialised);
			isInitialised = true;
			assertEquals("Incorrect team name", expectedTeamName, context
					.getTeamName());
		}

		@Override
		public Team createTeam() {
			return team;
		}
	}

	/**
	 * Ensures able to successfully source the {@link Team} and details of
	 * {@link RawTeamMetaDataImpl} are correct.
	 */
	public void testTeamSourced() {

		final Team team = this.createMock(Team.class);

		// Record constructing team
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), SourceTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new SourcePropertiesImpl());

		// Attempt to construct team
		this.replayMockObjects();
		SourceTeamSource.team = team;
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertEquals("Incorrect team", team, metaData.getTeam());
		assertEquals("Should be no listeners", 0, metaData
				.getProcessContextListeners().length);
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
		public Team createTeam() {
			return team;
		}
	}

	/**
	 * Ensure able to register a {@link ProcessContextListener}.
	 */
	public void testRegisterProcessContextListener() {

		final ProcessContextListener listener = this
				.createMock(ProcessContextListener.class);
		final Team team = this.createMock(Team.class);

		// Record constructing team
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), ProcessContextListenerTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new SourcePropertiesImpl());

		// Attempt to construct team
		this.replayMockObjects();
		ProcessContextListenerTeamSource.listener = listener;
		ProcessContextListenerTeamSource.team = team;
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertEquals("Incorrect team", team, metaData.getTeam());
		ProcessContextListener[] listeners = metaData
				.getProcessContextListeners();
		assertEquals("Should be a listener", 1, listeners.length);
		assertEquals("Incorrect listener", listener, listeners[0]);
	}

	/**
	 * {@link TeamSource} that registers a {@link ProcessContextListener}.
	 */
	@TestSource
	public static class ProcessContextListenerTeamSource extends
			SourceTeamSource {

		/**
		 * {@link ProcessContextListener}.
		 */
		public static ProcessContextListener listener;

		@Override
		public void init(TeamSourceContext context) throws Exception {
			// Register the listener
			context.registerProcessContextListener(listener);

			// Do super functions
			super.init(context);
		}
	}

	/**
	 * Constructs the {@link RawTeamMetaDataImpl} with the mock objects.
	 * 
	 * @return {@link RawTeamMetaDataImpl}.
	 */
	private RawTeamMetaData constructRawTeamMetaData(
			boolean isExpectConstruction) {

		// Attempt to construct
		RawTeamMetaData metaData = RawTeamMetaDataImpl.getFactory()
				.constructRawTeamMetaData(this.configuration, this.issues);

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