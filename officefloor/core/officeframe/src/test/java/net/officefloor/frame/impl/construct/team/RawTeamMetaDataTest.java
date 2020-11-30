/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.team;

import java.util.logging.Logger;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareContext;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawTeamMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawTeamMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_NAME = "OFFICE_FLOOR_NAME";

	/**
	 * Name of the {@link Team}.
	 */
	private final String TEAM_NAME = "TEAM NAME";

	/**
	 * {@link TeamConfiguration}.
	 */
	private TeamBuilderImpl<?> configuration = new TeamBuilderImpl<TeamSource>(TEAM_NAME, TeamSource.class);

	/**
	 * {@link TeamOversight}.
	 */
	private TeamOversight teamOversight = null;

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContextImpl sourceContext = new SourceContextImpl(this.getClass().getName(), false, null,
			Thread.currentThread().getContextClassLoader(), new MockClockFactory());

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

		// Record
		this.configuration = new TeamBuilderImpl<>(null, TeamSource.class);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, OFFICE_FLOOR_NAME, "Team added without a name");

		// Construct
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if negative {@link Team} size.
	 */
	public void testNegativeTeamSize() {

		// Record
		this.configuration.setTeamSize(-1);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "Team size can not be negative");

		// Construct
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link TeamSource}.
	 */
	public void testNoTeamSource() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, (TeamSource) null);
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
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, FailInstantiateTeamSource.class);
		FailInstantiateTeamSource.instantiateFailure = failure;
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Failed to instantiate " + FailInstantiateTeamSource.class.getName(), failure);

		// Attempt to construct team
		this.replayMockObjects();
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
		 * @throws Exception Failure to instantiate.
		 */
		public FailInstantiateTeamSource() throws Exception {
			throw instantiateFailure;
		}
	}

	/**
	 * Ensure correct {@link Logger}.
	 */
	public void testLogger() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, LoggerTeamSource.class);

		// Construct
		LoggerTeamSource.loggerName = null;
		this.replayMockObjects();
		this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure correct logger name
		assertEquals("Incorrect logger name", TEAM_NAME, LoggerTeamSource.loggerName);
	}

	/**
	 * {@link TeamSource} to confirm correct {@link Logger} name.
	 */
	@TestSource
	public static class LoggerTeamSource extends TeamSourceAdapter {

		public static String loggerName;

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			loggerName = context.getLogger().getName();
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures issue if required property is not specified.
	 */
	public void testMissingProperty() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, NoPropertyTeamSource.class);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Must specify property '" + NoPropertyTeamSource.PROPERTY_NAME + "'");

		// Construct
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

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			context.getProperty(PROPERTY_NAME);
			fail("Should not sucessfully obtain property");
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures issue if required class is not available.
	 */
	public void testMissingClass() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, NoClassTeamSource.class);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "Can not load class '" + NoClassTeamSource.CLASS_NAME + "'");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link TeamSource} that obtains a {@link Class} and creates a {@link Team}.
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
			fail("Should not successfully load class");
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures issue if required resource is not available.
	 */
	public void testMissingResource() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, NoResourceTeamSource.class);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME,
				"Can not obtain resource at location '" + NoResourceTeamSource.RESOURCE_LOCATION + "'");

		// Construct
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
			fail("Should not successfully obtain a resource");
			return super.createTeam(context);
		}
	}

	/**
	 * Ensures handles failure to create the {@link Team}.
	 */
	public void testTeamCreateFailure() {

		final RuntimeException failure = new RuntimeException("create failure");

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, FailCreateTeamSource.class);
		FailCreateTeamSource.createFailure = failure;
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "Failed to create Team", failure);

		// Attempt to construct team
		this.replayMockObjects();
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

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, SourceTeamSource.class);
		this.issues.addIssue(AssetType.TEAM, TEAM_NAME, "TeamSource failed to provide Team");

		// Construct
		this.replayMockObjects();
		SourceTeamSource.team = null;
		this.constructRawTeamMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain the {@link Team} name from the {@link TeamSourceContext}.
	 */
	public void testTeamNameAvailableFromContext() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, NameTeamSource.class);
		NameTeamSource.expectedTeamName = TEAM_NAME;
		NameTeamSource.isInitialised = false;
		NameTeamSource.team = this.createMock(Team.class);

		// Attempt to construct team
		this.replayMockObjects();
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
	 * Ensure provide {@link TeamOversight}.
	 */
	public void testTeamOversight() {

		// Record
		TeamBuilderImpl<SourceTeamSource> builder = new TeamBuilderImpl<>(TEAM_NAME, SourceTeamSource.class);
		this.configuration = builder;
		Team team = this.createMock(Team.class);
		this.teamOversight = (context) -> {
			assertFalse("Should be allowing team oversight", context.isRequestNoTeamOversight());
			assertTrue("Incorrect team source", context.getTeamSource() instanceof SourceTeamSource);
			return team;
		};

		// Construct
		this.replayMockObjects();
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertSame("Incorrect team (should be via oversight", team, metaData.getTeamManagement().getTeam());
	}

	/**
	 * Ensure can request no {@link TeamOversight}.
	 */
	public void testRequestNoTeamOversight() {

		// Record
		TeamBuilderImpl<SourceTeamSource> builder = new TeamBuilderImpl<>(TEAM_NAME, SourceTeamSource.class);
		builder.requestNoTeamOversight();
		this.configuration = builder;
		Team team = this.createMock(Team.class);
		this.teamOversight = (context) -> {
			assertTrue("Should request no team oversight", context.isRequestNoTeamOversight());
			assertTrue("Incorrect team source", context.getTeamSource() instanceof SourceTeamSource);
			return team;
		};

		// Construct
		this.replayMockObjects();
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertSame("Incorrect team (should be via oversight", team, metaData.getTeamManagement().getTeam());
	}

	/**
	 * Ensures able to successfully source the {@link Team} and details of
	 * {@link RawTeamMetaData} are correct.
	 */
	public void testTeamSourcedViaClass() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, SourceTeamSource.class);
		SourceTeamSource.team = this.createMock(Team.class);

		// Construct
		this.replayMockObjects();
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertEquals("Incorrect team", SourceTeamSource.team, metaData.getTeamManagement().getTeam());
		assertFalse("Should not be thread local aware", metaData.isRequireThreadLocalAwareness());
	}

	/**
	 * Ensures able to successfully source the {@link Team} and details of
	 * {@link RawTeamMetaData} are correct.
	 */
	public void testTeamSourcedViaInstance() {

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, new SourceTeamSource());
		SourceTeamSource.team = this.createMock(Team.class);

		// Construct
		this.replayMockObjects();
		RawTeamMetaData metaData = this.constructRawTeamMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect team name", TEAM_NAME, metaData.getTeamName());
		assertEquals("Incorrect team", SourceTeamSource.team, metaData.getTeamManagement().getTeam());
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

		// Record
		this.configuration = new TeamBuilderImpl<>(TEAM_NAME, MockThreadLocalAwareTeamSource.class);
		MockThreadLocalAwareTeamSource.context = null;

		// Construct
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
	 * Constructs the {@link RawTeamMetaData} with the mock objects.
	 * 
	 * @return {@link RawTeamMetaData}.
	 */
	private RawTeamMetaData constructRawTeamMetaData(boolean isExpectConstruction) {

		// Attempt to construct
		ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(
				new ThreadCompletionListener[0]);
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(managedExecutionFactory,
				(thread) -> {
				});
		Executive executive = new DefaultExecutive(threadFactoryManufacturer);
		RawTeamMetaData metaData = new RawTeamMetaDataFactory(this.sourceContext, executive, this.teamOversight,
				threadFactoryManufacturer, this.threadLocalAwareExecutor).constructRawTeamMetaData(this.configuration,
						OFFICE_FLOOR_NAME, this.issues);

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
