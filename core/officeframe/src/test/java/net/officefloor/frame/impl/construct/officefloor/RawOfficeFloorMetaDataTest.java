/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.officefloor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.function.Supplier;

import org.junit.Assert;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.escalation.EscalationHandlerEscalationFlow.EscalationKey;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawOfficeFloorMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawOfficeFloorMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_NAME = "OFFICE_FLOOR";

	/**
	 * {@link OfficeFloorConfiguration}.
	 */
	private OfficeFloorBuilderImpl configuration = new OfficeFloorBuilderImpl(OFFICE_FLOOR_NAME);

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor = this.createMock(ThreadLocalAwareExecutor.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if no {@link OfficeFloor} name.
	 */
	public void testNoOfficeFloorName() {

		// Record
		this.configuration = new OfficeFloorBuilderImpl(null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "Unknown", "Name not provided for OfficeFloor");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if non-positive startup wait time.
	 */
	public void testNonPositiveStartupWaitTime() {

		// Record
		this.configuration.setMaxStartupWaitTime(0);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, OFFICE_FLOOR_NAME, "Must provide positive startup wait time");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure provide default startup wait time.
	 */
	public void testDefaultStartupWaitTime() {
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();
		assertEquals("Incorrect default startup wait time", 10_000,
				metaData.getOfficeFloorMetaData().getMaxStartupWaitTime());
	}

	/**
	 * Ensures appropriate {@link SourceContext}.
	 */
	public void testSourceContext() throws IOException {

		this.configuration.addResources((location) -> new ByteArrayInputStream(location.getBytes()));

		// Obtain the source context
		final String NAME = "TEST";
		long mockCurrentTime = 1000;
		Supplier<ClockFactory> defaultClockFactoryProvider = () -> new MockClockFactory(mockCurrentTime);
		SourceContext context = this.configuration.getSourceContext(NAME, defaultClockFactoryProvider);

		// Ensure correct logger
		assertEquals("Incorrect logger", NAME, context.getLogger().getName());

		// Obtain resource
		InputStream resource = context.getResource("test");
		assertNotNull("Should have resource", resource);
		Reader reader = new InputStreamReader(resource);
		StringWriter buffer = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			buffer.write(character);
		}
		assertEquals("Incorrect resource", "test", buffer.toString());

		// Validate default clock
		assertEquals("Incorrect default clock", Long.valueOf(mockCurrentTime),
				context.getClock((time) -> time).getTime());

		// Override the clock
		long mockOverrideTime = 2000;
		this.configuration.setClockFactory(new MockClockFactory(mockOverrideTime));
		assertEquals("Incorrect override clock", Long.valueOf(mockOverrideTime), this.configuration
				.getSourceContext(NAME, defaultClockFactoryProvider).getClock((time) -> time).getTime());
	}

	/**
	 * Ensures handle not construct {@link Team}.
	 */
	public void testNotConstructTeam() {

		// Record
		this.configuration.addTeam(null, MockTeamSource.class);
		this.record_issue("Team added without a name");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if two {@link Team} instances by same name.
	 */
	public void testDuplicateTeamNames() {

		Team team = this.createMock(Team.class);

		final String DUPLICATE_TEAM_NAME = "TEAM";
		this.configuration.addTeam(DUPLICATE_TEAM_NAME, new MockTeamSource(team));
		this.configuration.addTeam(DUPLICATE_TEAM_NAME, new MockTeamSource(team));
		this.record_issue("Teams registered with the same name '" + DUPLICATE_TEAM_NAME + "'");

		// Construct
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();

		// Ensure obtain the only first team
		assertSame("Incorrect team", team,
				metaData.getRawTeamMetaData(DUPLICATE_TEAM_NAME).getTeamManagement().getTeam());
	}

	/**
	 * Ensures successfully construct {@link Team} instances.
	 */
	public void testConstructTeams() {

		Team one = this.createMock(Team.class);
		Team two = this.createMock(Team.class);
		Team three = this.createMock(Team.class);

		this.configuration.addTeam("ONE", new MockTeamSource(one));
		this.configuration.addTeam("TWO", new MockTeamSource(two));
		this.configuration.addTeam("THREE", new MockTeamSource(three));

		// Construct
		this.replayMockObjects();
		RawOfficeFloorMetaData rawMetaData = this.constructRawOfficeFloorMetaData(true);
		OfficeFloorMetaData metaData = rawMetaData.getOfficeFloorMetaData();
		TeamManagement[] actualTeams = metaData.getTeams();
		this.verifyMockObjects();

		// Ensure teams registered
		assertNotNull(rawMetaData.getRawTeamMetaData("ONE"));
		assertNotNull(rawMetaData.getRawTeamMetaData("TWO"));
		assertNotNull(rawMetaData.getRawTeamMetaData("THREE"));

		// Validate the teams
		assertEquals("Incorrect number of teams", 3, actualTeams.length);
		assertSame("Incorrect team one", one, actualTeams[0].getTeam());
		assertSame("Incorrect team two", two, actualTeams[1].getTeam());
		assertSame("Incorrect team three", three, actualTeams[2].getTeam());
	}

	/**
	 * Ensures successfully construct a {@link Team} with a
	 * {@link ThreadLocalAwareTeam}.
	 */
	public void testConstructThreadLocalAwareTeam() {

		// Record
		Team team = this.createMock(Team.class);
		this.configuration.addTeam("TEAM", new MockTeamSource(team));

		// Construct
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();

		// Ensure team registered
		assertNotNull(metaData.getRawTeamMetaData("TEAM"));

		// Should have a thread aware executor
		assertSame("Incorrect thread local aware executor", this.threadLocalAwareExecutor,
				metaData.getThreadLocalAwareExecutor());
	}

	/**
	 * Ensures not construct if not construct {@link ManagedObject}.
	 */
	public void testNotConstructManagedObject() {

		// Record
		this.configuration.addManagedObject(null, MockManagedObjectSource.class);
		this.record_issue("ManagedObject added without a name");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if two {@link ManagedObjectSource} instances by same name.
	 */
	public void testDuplicateManagedObjectSourceNames() {

		// Record
		final String DUPLICATE_MANAGED_OBJECT_SOURCE_NAME = "MANAGED_OBJECT_SOURCE";
		this.configuration.addManagedObject(DUPLICATE_MANAGED_OBJECT_SOURCE_NAME, MockManagedObjectSource.class)
				.setManagingOffice("OFFICE");
		this.configuration.addManagedObject(DUPLICATE_MANAGED_OBJECT_SOURCE_NAME, MockManagedObjectSource.class)
				.setManagingOffice("OFFICE");
		this.configuration.addOffice("OFFICE");
		this.record_issue(
				"Managed object sources registered with the same name '" + DUPLICATE_MANAGED_OBJECT_SOURCE_NAME + "'");

		// Construct
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();

		// Ensure only first managed object
		assertNotNull("Incorrect managed object source",
				metaData.getRawManagedObjectMetaData(DUPLICATE_MANAGED_OBJECT_SOURCE_NAME));
	}

	/**
	 * Ensures issue if no managing {@link Office} for {@link ManagedObjectSource}.
	 */
	public void testNoManagingOfficeForManagedObjectSource() {

		// Record
		this.configuration.addManagedObject("MOS", MockManagedObjectSource.class);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MOS", "No managing office configuration");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if unknown managing {@link Office} for
	 * {@link ManagedObjectSource}.
	 */
	public void testUnknownManagingOfficeForManagedObjectSource() {

		// Record
		this.configuration.addManagedObject("MOS", MockManagedObjectSource.class).setManagingOffice("UNKNOWN");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MOS", "Can not find managing office 'UNKNOWN'");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures successfully construct {@link ManagedObjectSource} instances.
	 */
	public void testConstructManagedObjectSources() {

		MockManagedObjectSource one = new MockManagedObjectSource();
		MockManagedObjectSource two = new MockManagedObjectSource();
		MockManagedObjectSource three = new MockManagedObjectSource();

		this.configuration.addManagedObject("ONE", one).setManagingOffice("OFFICE");
		this.configuration.addManagedObject("TWO", two).setManagingOffice("OFFICE");
		this.configuration.addManagedObject("THREE", three).setManagingOffice("OFFICE");
		this.configuration.addOffice("OFFICE");

		// Construct
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		ManagedObjectSourceInstance<?>[][] mosInstances = metaData.getOfficeFloorMetaData()
				.getManagedObjectSourceInstances();
		this.verifyMockObjects();

		// Ensure managed object sources registered
		assertNotNull(metaData.getRawManagedObjectMetaData("ONE"));
		assertNotNull(metaData.getRawManagedObjectMetaData("TWO"));
		assertNotNull(metaData.getRawManagedObjectMetaData("THREE"));

		// Validate managed object source instances
		assertEquals("Incorrect number of managed object source instances", 3, mosInstances[0].length);
		assertSame("Incorrect first managed object source", one, mosInstances[0][0].getManagedObjectSource());
		assertSame("Incorrect second managed object source", three, mosInstances[0][1].getManagedObjectSource());
		assertSame("Incorrect third managed object source", two, mosInstances[0][2].getManagedObjectSource());
	}

	/**
	 * Ensures handle no {@link EscalationHandler} configured.
	 */
	@SuppressWarnings("unchecked")
	public void testNoEscalationHandlerConfigured() throws Throwable {

		PrintStream systemErr = System.err;
		try {
			ByteArrayOutputStream capture = new ByteArrayOutputStream();
			System.setErr(new PrintStream(capture));

			@SuppressWarnings("rawtypes")
			ManagedFunctionContext context = this.createMock(ManagedFunctionContext.class);
			Exception escalation = new Exception("TEST");

			// Record escalation
			this.recordReturn(context, context.getObject(EscalationKey.EXCEPTION), escalation);

			// Construct
			this.replayMockObjects();
			RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);

			// Ensure default escalation handler
			EscalationFlow officeFloorEscalation = metaData.getOfficeFloorEscalation();
			ManagedFunction<?, ?> function = officeFloorEscalation.getManagedFunctionMetaData()
					.getManagedFunctionFactory().createManagedFunction();
			function.execute(context);
			this.verifyMockObjects();

			// Ensure written to system error
			String errorOutput = new String(capture.toByteArray());
			StringWriter expectedError = new StringWriter();
			expectedError.write("FAILURE: Office not handling:" + END_OF_LINE);
			escalation.printStackTrace(new PrintWriter(expectedError));
			expectedError.write(END_OF_LINE);
			assertEquals("Incorrect error output", expectedError.toString(), errorOutput);

		} finally {
			System.setErr(systemErr);
		}
	}

	/**
	 * Ensures handle provided a {@link EscalationProcedure}.
	 */
	@SuppressWarnings("unchecked")
	public void testProvideEscalationProcedure() throws Throwable {

		final EscalationHandler escalationHandler = this.createMock(EscalationHandler.class);
		this.configuration.setEscalationHandler(escalationHandler);

		@SuppressWarnings("rawtypes")
		ManagedFunctionContext context = this.createMock(ManagedFunctionContext.class);
		Exception escalation = new Exception("TEST");

		// Record escalation handlings
		this.recordReturn(context, context.getObject(EscalationKey.EXCEPTION), escalation);
		escalationHandler.handleEscalation(escalation);

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);

		// Ensure default escalation handler
		EscalationFlow officeFloorEscalation = metaData.getOfficeFloorEscalation();
		ManagedFunction<?, ?> function = officeFloorEscalation.getManagedFunctionMetaData().getManagedFunctionFactory()
				.createManagedFunction();
		function.execute(context);

		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Office} name.
	 */
	public void testNoOfficeName() {

		// Record
		this.configuration.addOffice(null);
		this.record_issue("Office added without a name");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct an {@link Office}.
	 */
	public void testConstructOffice() {

		// Record
		this.configuration.addOffice("OFFICE");

		// Construct
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		OfficeMetaData office = metaData.getOfficeFloorMetaData().getOfficeMetaData()[0];
		this.verifyMockObjects();

		// Ensure have office meta-data
		assertEquals("Incorrect office meta-data", "OFFICE", office.getOfficeName());
	}

	/**
	 * Records an issue for the {@link OfficeFloor}.
	 * 
	 * @param issueDescription Description of issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.OFFICE_FLOOR, OFFICE_FLOOR_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link RawOfficeFloorMetaData}.
	 * 
	 * @param isConstruct Indicates if the {@link RawOfficeFloorMetaData} should be
	 *                    returned.
	 * @return {@link RawOfficeFloorMetaData}.
	 */
	private RawOfficeFloorMetaData constructRawOfficeFloorMetaData(boolean isConstruct) {

		// Create the raw office floor meta-data
		RawOfficeFloorMetaData metaData = new RawOfficeFloorMetaDataFactory(this.threadLocalAwareExecutor)
				.constructRawOfficeFloorMetaData(this.configuration, this.issues);

		// Determine if constructed
		if (isConstruct) {
			assertNotNull("Meta-data should be constructed", metaData);
		} else {
			assertNull("Meta-data should not be constructed", metaData);
		}

		// Return the raw office floor meta-data
		return metaData;
	}

	/**
	 * Mock {@link TeamSource} for testing.
	 */
	@TestSource
	public static class MockTeamSource extends AbstractTeamSource {

		private final Team team;

		public MockTeamSource(Team team) {
			this.team = team;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
			Assert.fail("Should not be invoked");
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return this.team;
		}
	}

	/**
	 * Mock {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require managed object");
			return null;
		}
	}

}
