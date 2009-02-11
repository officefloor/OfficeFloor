/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.team;

import java.util.Properties;

import net.officefloor.frame.api.OfficeFloorIssues;
import net.officefloor.frame.api.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawTeamMetaDataImpl}.
 * 
 * @author Daniel
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

		Properties noProperties = new Properties();

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
	public static class NoPropertyTeamSource extends TeamSourceAdapter {

		/**
		 * Name of the required property.
		 */
		public static final String PROPERTY_NAME = "required.property";

		/**
		 * Expected value for the property.
		 */
		public static final String PROPERTY_VALUE = "property.value";

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.impl.construct.team.TeamSourceAdapter#init(
		 * net.officefloor.frame.spi.team.TeamSourceContext)
		 */
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
				.getProperties(), new Properties());
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
	public static class FailInitTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Exception} to be thrown on init.
		 */
		public static Exception initFailure;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.impl.construct.team.TeamSourceAdapter#init(
		 * net.officefloor.frame.spi.team.TeamSourceContext)
		 */
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
				.getProperties(), new Properties());
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
	public static class FailCreateTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Exception} to be thrown on create.
		 */
		public static RuntimeException createFailure;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.impl.construct.team.TeamSourceAdapter#createTeam
		 * ()
		 */
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
				.getProperties(), new Properties());
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"TeamSource failed to provide Team");

		// Attempt to construct team
		this.replayMockObjects();
		SourceTeamSource.team = null;
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to successfully source the {@link Team} and details of
	 * {@link RawTeamMetaDataImpl} are correct.
	 */
	public void testTeamSourced() {

		final Team team = this.createMock(Team.class);

		// Record team source that fails to initialise
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTeamSourceClass(), SourceTeamSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new Properties());

		// Attempt to construct team
		this.replayMockObjects();
		SourceTeamSource.team = team;
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertEquals("Incorrect team", team, metaData.getTeam());
	}

	/**
	 * {@link TeamSource} that sources a {@link Team}.
	 */
	public static class SourceTeamSource extends TeamSourceAdapter {

		/**
		 * {@link Team} to be returned.
		 */
		public static Team team;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.impl.construct.team.TeamSourceAdapter#createTeam
		 * ()
		 */
		@Override
		public Team createTeam() {
			return team;
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
