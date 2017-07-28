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
package net.officefloor.frame.impl.construct.officefloor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.impl.execute.escalation.EscalationHandlerEscalationFlow.EscalationKey;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.RawAdministrationMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.construct.RawTeamMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteContextFactory;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawOfficeFloorMetaDataImpl}.
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
	private final OfficeFloorConfiguration configuration = this.createMock(OfficeFloorConfiguration.class);

	/**
	 * {@link Thread} decorator.
	 */
	@SuppressWarnings("unchecked")
	private final Consumer<Thread> threadDecorator = this.createMock(Consumer.class);

	/**
	 * {@link ManagedExecutionFactory}.
	 */
	private final ManagedExecutionFactory managedExecutionFactory = this.createMock(ManagedExecutionFactory.class);

	/**
	 * {@link ThreadCompletionListener}.
	 */
	private final ThreadCompletionListener threadCompletionListener = this.createMock(ThreadCompletionListener.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this.createMock(SourceContext.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link RawTeamMetaDataFactory}.
	 */
	private final RawTeamMetaDataFactory rawTeamFactory = this.createMock(RawTeamMetaDataFactory.class);

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor = this.createMock(ThreadLocalAwareExecutor.class);

	/**
	 * {@link RawManagedObjectMetaDataFactory}.
	 */
	private final RawManagedObjectMetaDataFactory rawMosFactory = this
			.createMock(RawManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundMoFactory = this
			.createMock(RawBoundManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawGovernanceMetaDataFactory}.
	 */
	private final RawGovernanceMetaDataFactory rawGovernanceFactory = this
			.createMock(RawGovernanceMetaDataFactory.class);

	/**
	 * {@link RawAdministrationMetaDataFactory}.
	 */
	private final RawAdministrationMetaDataFactory rawBoundAdminFactory = this
			.createMock(RawAdministrationMetaDataFactory.class);

	/**
	 * {@link RawOfficeMetaDataFactory}.
	 */
	private final RawOfficeMetaDataFactory rawOfficeFactory = this.createMock(RawOfficeMetaDataFactory.class);

	/**
	 * {@link RawManagedFunctionMetaDataFactory}.
	 */
	private final RawManagedFunctionMetaDataFactory rawFunctionMetaDataFactory = this
			.createMock(RawManagedFunctionMetaDataFactory.class);

	/**
	 * Listing of the constructed {@link RawManagedObjectMetaData} instances.
	 */
	private final List<RawManagedObjectMetaData<?, ?>> constructedManagedObjects = new LinkedList<RawManagedObjectMetaData<?, ?>>();

	/**
	 * Ensures issue if no {@link OfficeFloor} name.
	 */
	public void testNoOfficeFloorName() {

		// Record no OfficeFloor name
		this.recordReturn(this.configuration, this.configuration.getOfficeFloorName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "Unknown", "Name not provided for OfficeFloor");
		this.recordReturn(this.configuration, this.configuration.getSourceContext(), this.sourceContext);
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link SourceContext}.
	 */
	public void testNoSourceContext() {

		// Record no source context
		this.recordReturn(this.configuration, this.configuration.getOfficeFloorName(), OFFICE_FLOOR_NAME);
		this.recordReturn(this.configuration, this.configuration.getSourceContext(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, OFFICE_FLOOR_NAME, "No SourceContext provided from configuration");
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		TeamConfiguration<?> breakChainConfiguration = this.createMock(TeamConfiguration.class);
		this.recordReturn(this.configuration, this.configuration.getBreakChainTeamConfiguration(),
				breakChainConfiguration);
		RawTeamMetaData breakChainMetaData = this.createMock(RawTeamMetaData.class);
		this.recordReturn(this.rawTeamFactory,
				this.rawTeamFactory.constructRawTeamMetaData(breakChainConfiguration, null, this.threadDecorator,
						this.threadLocalAwareExecutor, this.managedExecutionFactory, this.issues),
				breakChainMetaData, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect configuration", expected[0], actual[0]);
						assertNotNull("Must have default source context", actual[1]);
						for (int i = 2; i <= 5; i++) {
							assertEquals("Incorrect value " + i, expected[0], actual[0]);
						}
						return true;
					}
				});
		TeamManagement breakChainTeam = this.createMock(TeamManagement.class);
		this.recordReturn(breakChainMetaData, breakChainMetaData.getTeamManagement(), breakChainTeam);
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures handle not construct {@link Team}.
	 */
	public void testNotConstructTeam() {

		final TeamConfiguration<?> teamConfiguration = this.createMock(TeamConfiguration.class);

		// Record not construct team
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.recordReturn(this.configuration, this.configuration.getThreadDecorator(), this.threadDecorator);
		this.recordReturn(this.configuration, this.configuration.getTeamConfiguration(),
				new TeamConfiguration[] { teamConfiguration });
		this.recordReturn(this.rawTeamFactory,
				this.rawTeamFactory.constructRawTeamMetaData(teamConfiguration, this.sourceContext,
						this.threadDecorator, this.threadLocalAwareExecutor, this.managedExecutionFactory, this.issues),
				null, this.constructTeamMatacher);

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if two {@link Team} instances by same name.
	 */
	public void testDuplicateTeamNames() {

		final String DUPLICATE_TEAM_NAME = "TEAM";

		final TeamConfiguration<?> teamConfigurationOne = this.createMock(TeamConfiguration.class);
		final RawTeamMetaData rawTeamOne = this.createMock(RawTeamMetaData.class);
		final TeamManagement teamOne = this.createMock(TeamManagement.class);

		final TeamConfiguration<?> teamConfigurationTwo = this.createMock(TeamConfiguration.class);
		final RawTeamMetaData rawTeamTwo = this.createMock(RawTeamMetaData.class);

		// Record construct teams with duplicate name
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.recordReturn(this.configuration, this.configuration.getThreadDecorator(), this.threadDecorator);
		this.recordReturn(this.configuration, this.configuration.getTeamConfiguration(),
				new TeamConfiguration[] { teamConfigurationOne, teamConfigurationTwo });
		this.recordReturn(this.rawTeamFactory,
				this.rawTeamFactory.constructRawTeamMetaData(teamConfigurationOne, this.sourceContext,
						this.threadDecorator, this.threadLocalAwareExecutor, this.managedExecutionFactory, this.issues),
				rawTeamOne);
		this.recordReturn(rawTeamOne, rawTeamOne.getTeamName(), DUPLICATE_TEAM_NAME);
		this.recordReturn(rawTeamOne, rawTeamOne.getTeamManagement(), teamOne);
		this.recordReturn(this.rawTeamFactory,
				this.rawTeamFactory.constructRawTeamMetaData(teamConfigurationTwo, this.sourceContext,
						this.threadDecorator, this.threadLocalAwareExecutor, this.managedExecutionFactory, this.issues),
				rawTeamTwo);
		this.recordReturn(rawTeamTwo, rawTeamTwo.getTeamName(), DUPLICATE_TEAM_NAME);
		this.record_issue("Teams registered with the same name '" + DUPLICATE_TEAM_NAME + "'");
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();

		// Ensure obtain the only first team
		assertEquals("Incorrect team", rawTeamOne, metaData.getRawTeamMetaData(DUPLICATE_TEAM_NAME));
	}

	/**
	 * Ensures successfully construct {@link Team} instances.
	 */
	public void testConstructTeams() {

		// Record constructing teams
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		TeamManagement[] expectedTeams = this.record_constructTeams("ONE", "TWO", "THREE");
		TeamManagement breakChainTeam = this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		TeamManagement[] actualTeams = metaData.getOfficeFloorMetaData().getTeams();
		this.verifyMockObjects();

		// Ensure teams registered
		assertNotNull(metaData.getRawTeamMetaData("ONE"));
		assertNotNull(metaData.getRawTeamMetaData("TWO"));
		assertNotNull(metaData.getRawTeamMetaData("THREE"));

		// Validate the teams
		assertEquals("Incorrect number of teams", 4, actualTeams.length);
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect team " + i, expectedTeams[i], actualTeams[i]);
		}
		assertEquals("Incorrect break chain team", breakChainTeam, actualTeams[3]);
	}

	/**
	 * Ensures successfully construct a {@link Team} with a
	 * {@link ThreadLocalAwareTeam}.
	 */
	public void testConstructThreadLocalAwareTeam() {

		// Mock Process Context Listener
		final TeamConfiguration<?> teamConfiguration = this.createMock(TeamConfiguration.class);
		final RawTeamMetaData rawTeamMetaData = this.createMock(RawTeamMetaData.class);
		final String TEAM_NAME = "TEAM";
		final TeamManagement team = this.createMock(TeamManagement.class);

		// Record constructing team
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.recordReturn(this.configuration, this.configuration.getThreadDecorator(), this.threadDecorator);
		this.recordReturn(this.configuration, this.configuration.getTeamConfiguration(),
				new TeamConfiguration[] { teamConfiguration });
		this.recordReturn(this.rawTeamFactory,
				this.rawTeamFactory.constructRawTeamMetaData(teamConfiguration, this.sourceContext,
						this.threadDecorator, this.threadLocalAwareExecutor, this.managedExecutionFactory, this.issues),
				rawTeamMetaData);
		this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeamName(), TEAM_NAME);
		this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeamManagement(), team);
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct OfficeFloor
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
	 * Ensures successfully construct break {@link FunctionState} chain
	 * {@link Team}.
	 */
	public void testConstructBreakChainTeam() {

		// Record constructing teams
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		TeamManagement breakChainTeam = this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();

		// Ensure providing break chain team
		assertEquals("Incorrect break chain team", breakChainTeam, metaData.getBreakChainTeamManagement());
	}

	/**
	 * Ensures not construct if not construct {@link ManagedObject}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testNotConstructManagedObject() {

		final ManagedObjectSourceConfiguration mosConfiguration = this
				.createMock(ManagedObjectSourceConfiguration.class);

		// Record not construct managed object
		this.record_officeFloorName();
		this.recordReturn(this.configuration, this.configuration.getManagedObjectSourceConfiguration(),
				new ManagedObjectSourceConfiguration[] { mosConfiguration });
		this.recordReturn(this.rawMosFactory, this.rawMosFactory.constructRawManagedObjectMetaData(mosConfiguration,
				this.sourceContext, this.issues, this.configuration), null);

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if two {@link ManagedObjectSource} instances by same name.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDuplicateManagedObjectSourceNames() {

		final String DUPLICATE_MANAGED_OBJECT_SOURCE_NAME = "MANAGED_OBJECT_SOURCE";

		final ManagedObjectSourceConfiguration mosConfigurationOne = this
				.createMock(ManagedObjectSourceConfiguration.class);
		final RawManagedObjectMetaData mosOne = this.createMock(RawManagedObjectMetaData.class);
		final RawManagingOfficeMetaData rawOfficeMoMetaData = this.createMock(RawManagingOfficeMetaData.class);

		final ManagedObjectSourceConfiguration mosConfigurationTwo = this
				.createMock(ManagedObjectSourceConfiguration.class);
		final RawManagedObjectMetaData mosTwo = this.createMock(RawManagedObjectMetaData.class);

		// Record construct managed object sources with duplicate name
		this.record_officeFloorName();
		this.recordReturn(this.configuration, this.configuration.getManagedObjectSourceConfiguration(),
				new ManagedObjectSourceConfiguration[] { mosConfigurationOne, mosConfigurationTwo });
		this.recordReturn(this.rawMosFactory, this.rawMosFactory.constructRawManagedObjectMetaData(mosConfigurationOne,
				this.sourceContext, this.issues, this.configuration), mosOne);
		this.recordReturn(mosOne, mosOne.getManagedObjectName(), DUPLICATE_MANAGED_OBJECT_SOURCE_NAME);
		this.recordReturn(mosOne, mosOne.getRawManagingOfficeMetaData(), rawOfficeMoMetaData);
		this.recordReturn(mosOne, mosOne.getThreadCompletionListeners(),
				new ThreadCompletionListener[] { this.threadCompletionListener });
		this.recordReturn(rawOfficeMoMetaData, rawOfficeMoMetaData.getManagingOfficeName(), "OFFICE");
		this.constructedManagedObjects.add(mosOne); // constructed later
		this.recordReturn(this.rawMosFactory, this.rawMosFactory.constructRawManagedObjectMetaData(mosConfigurationTwo,
				this.sourceContext, this.issues, this.configuration), mosTwo);
		this.recordReturn(mosTwo, mosTwo.getManagedObjectName(), DUPLICATE_MANAGED_OBJECT_SOURCE_NAME);
		this.record_issue(
				"Managed object sources registered with the same name '" + DUPLICATE_MANAGED_OBJECT_SOURCE_NAME + "'");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices("OFFICE", new RawManagingOfficeMetaData[] { rawOfficeMoMetaData });
		this.record_constructManagedObjectInstances();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();

		// Ensure obtain the only first managed object
		assertEquals("Incorrect managed object source", mosOne,
				metaData.getRawManagedObjectMetaData(DUPLICATE_MANAGED_OBJECT_SOURCE_NAME));
	}

	/**
	 * Ensures issue if no managing {@link Office} for
	 * {@link ManagedObjectSource}.
	 */
	public void testNoManagingOfficeForManagedObjectSource() {

		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this.createMock(RawManagedObjectMetaData.class);
		final String managedObjectSourceName = "MO";

		// Record no managing office for managed object
		this.record_officeFloorName();
		RawManagingOfficeMetaData<?> managingOffice = this.record_constructManagedObjectSources("UNKNOWN_OFFICE",
				managedObjectSourceName)[0];
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices();
		this.recordReturn(managingOffice, managingOffice.getRawManagedObjectMetaData(), rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(), managedObjectSourceName);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
				"Can not find managing office 'UNKNOWN_OFFICE'");
		this.record_constructManagedObjectInstances();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures successfully construct {@link ManagedObjectSource} instances.
	 */
	public void testConstructManagedObjectSources() {

		// Record construction of the teams
		this.record_officeFloorName();
		RawManagingOfficeMetaData<?>[] managingOffices = this.record_constructManagedObjectSources("OFFICE", "ONE",
				"TWO", "THREE");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.record_constructOffices("OFFICE", managingOffices);
		ManagedObjectSource<?, ?>[] managedObjectSources = this.record_constructManagedObjectInstances();

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		ManagedObjectSourceInstance<?>[] mosInstances = metaData.getOfficeFloorMetaData()
				.getManagedObjectSourceInstances();
		this.verifyMockObjects();

		// Ensure managed object sources registered
		assertNotNull(metaData.getRawManagedObjectMetaData("ONE"));
		assertNotNull(metaData.getRawManagedObjectMetaData("TWO"));
		assertNotNull(metaData.getRawManagedObjectMetaData("THREE"));

		// Validate managed object source instances
		assertEquals("Incorrect number of managed object source instances", 3, mosInstances.length);
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect managed object source instance " + i, managedObjectSources[i],
					mosInstances[i].getManagedObjectSource());
		}
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

			// Record no escalation procedure
			this.record_officeFloorName();
			this.record_constructManagedObjectSources("OFFICE");
			this.record_constructTeams();
			this.record_constructBreakChainTeam();
			this.recordReturn(this.configuration, this.configuration.getEscalationHandler(), null);
			this.record_constructOffices();

			// Record escalation
			this.recordReturn(context, context.getObject(EscalationKey.EXCEPTION), escalation);

			// Attempt to construct OfficeFloor
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
			expectedError.write("FAILURE: Office not handling:\n");
			escalation.printStackTrace(new PrintWriter(expectedError));
			expectedError.write("\n");
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

		@SuppressWarnings("rawtypes")
		ManagedFunctionContext context = this.createMock(ManagedFunctionContext.class);
		Exception escalation = new Exception("TEST");

		// Record have escalation handler
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.recordReturn(this.configuration, this.configuration.getEscalationHandler(), escalationHandler);
		this.record_constructOffices();

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

		final OfficeConfiguration officeConfiguration = this.createMock(OfficeConfiguration.class);

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.recordReturn(this.configuration, this.configuration.getOfficeConfiguration(),
				new OfficeConfiguration[] { officeConfiguration });
		this.recordReturn(officeConfiguration, officeConfiguration.getOfficeName(), null);
		this.record_issue("Office added without a name");

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle not construct the {@link Office}.
	 */
	public void testNoConstructOffice() {

		final OfficeConfiguration officeConfiguration = this.createMock(OfficeConfiguration.class);

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		this.recordReturn(this.configuration, this.configuration.getOfficeConfiguration(),
				new OfficeConfiguration[] { officeConfiguration });
		this.recordReturn(officeConfiguration, officeConfiguration.getOfficeName(), "OFFICE");
		this.recordReturn(this.rawOfficeFactory,
				this.rawOfficeFactory.constructRawOfficeMetaData(officeConfiguration, this.issues, null, null,
						this.rawBoundMoFactory, null, this.rawBoundAdminFactory, this.rawFunctionMetaDataFactory),
				null, new AlwaysMatcher());

		// Attempt to construct OfficeFloor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct an {@link Office}.
	 */
	public void testConstructOffice() {

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructTeams();
		this.record_constructBreakChainTeam();
		this.record_constructEscalation();
		OfficeMetaData expectedOffice = this.record_constructOffices("OFFICE", null)[0];

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this.constructRawOfficeFloorMetaData(true);
		OfficeMetaData actualOffice = metaData.getOfficeFloorMetaData().getOfficeMetaData()[0];
		this.verifyMockObjects();

		// Ensure have office meta-data
		assertEquals("Incorrect office meta-data", expectedOffice, actualOffice);
	}

	/**
	 * Records obtaining the {@link OfficeFloor} name.
	 */
	private void record_officeFloorName() {
		this.recordReturn(this.configuration, this.configuration.getOfficeFloorName(), OFFICE_FLOOR_NAME);
		this.recordReturn(this.configuration, this.configuration.getSourceContext(), this.sourceContext);
	}

	/**
	 * {@link AbstractMatcher} for the construction of the
	 * {@link RawTeamMetaData}.
	 */
	private final AbstractMatcher constructTeamMatacher = new AbstractMatcher() {
		@Override
		public boolean matches(Object[] expected, Object[] actual) {
			boolean isMatch = true;

			// configuration to thread local aware executor
			for (int i = 0; i <= 3; i++) {
				isMatch = isMatch && (expected[0] == actual[0]);
			}

			// managed execution factory
			isMatch = isMatch && (actual[4] instanceof ManagedExecutionFactory);

			// Issues
			isMatch = isMatch && (expected[5] == actual[5]);

			return isMatch;
		}
	};

	/**
	 * Records construction of {@link RawTeamMetaData} instances.
	 * 
	 * @param teamNames
	 *            Names of the {@link Team} instances to construct.
	 * @return {@link TeamManagement} instances.
	 */
	private TeamManagement[] record_constructTeams(String... teamNames) {

		// Record obtaining the thread decorator
		this.recordReturn(this.configuration, this.configuration.getThreadDecorator(), this.threadDecorator);

		// Create the mock objects
		TeamConfiguration<?>[] teamConfigurations = new TeamConfiguration[teamNames.length];
		RawTeamMetaData[] rawTeamMetaDatas = new RawTeamMetaData[teamNames.length];
		TeamManagement[] teams = new TeamManagement[teamNames.length];
		for (int i = 0; i < teamNames.length; i++) {
			teamConfigurations[i] = this.createMock(TeamConfiguration.class);
			rawTeamMetaDatas[i] = this.createMock(RawTeamMetaData.class);
			teams[i] = this.createMock(TeamManagement.class);
		}

		// Record obtaining the team configuration
		this.recordReturn(this.configuration, this.configuration.getTeamConfiguration(), teamConfigurations);

		// Record constructing the teams
		for (int i = 0; i < teamNames.length; i++) {
			TeamConfiguration<?> teamConfiguration = teamConfigurations[i];
			String teamName = teamNames[i];
			RawTeamMetaData rawTeamMetaData = rawTeamMetaDatas[i];
			TeamManagement team = teams[i];

			// Record constructing the team
			this.recordReturn(this.rawTeamFactory,
					this.rawTeamFactory.constructRawTeamMetaData(teamConfiguration, this.sourceContext,
							this.threadDecorator, this.threadLocalAwareExecutor, this.managedExecutionFactory,
							this.issues),
					rawTeamMetaData, this.constructTeamMatacher);
			this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeamName(), teamName);
			this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeamManagement(), team);
		}

		// Return the constructed teams
		return teams;
	}

	/**
	 * Record constructing the break {@link FunctionState} chain
	 * {@link TeamManagement}.
	 * 
	 * @return Break {@link FunctionState} chain {@link TeamManagement}.
	 */
	private TeamManagement record_constructBreakChainTeam() {

		// Record building the break chain team
		TeamConfiguration<?> breakChainConfiguration = this.createMock(TeamConfiguration.class);
		this.recordReturn(this.configuration, this.configuration.getBreakChainTeamConfiguration(),
				breakChainConfiguration);
		RawTeamMetaData breakChainMetaData = this.createMock(RawTeamMetaData.class);
		this.recordReturn(this.rawTeamFactory,
				this.rawTeamFactory.constructRawTeamMetaData(breakChainConfiguration, this.sourceContext,
						this.threadDecorator, this.threadLocalAwareExecutor, this.managedExecutionFactory, this.issues),
				breakChainMetaData, this.constructTeamMatacher);
		TeamManagement breakChainTeam = this.createMock(TeamManagement.class);
		this.recordReturn(breakChainMetaData, breakChainMetaData.getTeamManagement(), breakChainTeam);

		// Return the break chain team
		return breakChainTeam;
	}

	/**
	 * Records construction of {@link RawManagedObjectMetaData} instances.
	 * 
	 * @param officeName
	 *            Name of the managing {@link Office}.
	 * @param managedObjectSourceNames
	 *            Names of the {@link ManagedObjectSource} instances to
	 *            construct.
	 * @return {@link RawManagingOfficeMetaData} instances for the constructed
	 *         {@link RawManagedObjectMetaData}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RawManagingOfficeMetaData[] record_constructManagedObjectSources(String officeName,
			String... managedObjectSourceNames) {

		// Create the mock objects
		ManagedObjectSourceConfiguration<?, ?>[] mosConfigurations = new ManagedObjectSourceConfiguration[managedObjectSourceNames.length];
		RawManagedObjectMetaData<?, ?>[] rawMoMetaDatas = new RawManagedObjectMetaData[managedObjectSourceNames.length];
		RawManagingOfficeMetaData[] managingOffices = new RawManagingOfficeMetaData[managedObjectSourceNames.length];
		for (int i = 0; i < mosConfigurations.length; i++) {
			mosConfigurations[i] = this.createMock(ManagedObjectSourceConfiguration.class);
			rawMoMetaDatas[i] = this.createMock(RawManagedObjectMetaData.class);
			managingOffices[i] = this.createMock(RawManagingOfficeMetaData.class);
		}

		// Record obtaining the managed object source configuration
		this.recordReturn(this.configuration, this.configuration.getManagedObjectSourceConfiguration(),
				mosConfigurations);

		// Record constructing the managed object sources
		for (int i = 0; i < managedObjectSourceNames.length; i++) {
			ManagedObjectSourceConfiguration mosConfiguration = mosConfigurations[i];
			String managedObjectSourceName = managedObjectSourceNames[i];
			RawManagedObjectMetaData<?, ?> rawMoMetaData = rawMoMetaDatas[i];
			RawManagingOfficeMetaData managingOffice = managingOffices[i];

			// Record constructing managed object source
			this.recordReturn(this.rawMosFactory, this.rawMosFactory.constructRawManagedObjectMetaData(mosConfiguration,
					this.sourceContext, this.issues, this.configuration), rawMoMetaData);
			this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(), managedObjectSourceName);
			this.recordReturn(rawMoMetaData, rawMoMetaData.getRawManagingOfficeMetaData(), managingOffice);
			this.recordReturn(managingOffice, managingOffice.getManagingOfficeName(), officeName);
			this.recordReturn(rawMoMetaData, rawMoMetaData.getThreadCompletionListeners(),
					new ThreadCompletionListener[] { this.threadCompletionListener });

			// Add the managed object for later construction
			this.constructedManagedObjects.add(rawMoMetaData);
		}

		// Return the managing offices
		return managingOffices;
	}

	/**
	 * Records construction of the {@link EscalationFlow} for the
	 * {@link OfficeFloor}.
	 */
	private void record_constructEscalation() {
		this.recordReturn(this.configuration, this.configuration.getEscalationHandler(), null);
	}

	/**
	 * Records constructing {@link OfficeMetaData} instances.
	 * 
	 * @param officeNameManagedObjectListPairs
	 *            Listing of {@link Office} name and
	 *            {@link RawManagingOfficeMetaData} array pairs.
	 * @return {@link OfficeMetaData} instances.
	 */
	private OfficeMetaData[] record_constructOffices(Object... officeNameManagedObjectListPairs) {

		// Obtain the office names and office managing managed objects
		int officeCount = officeNameManagedObjectListPairs.length / 2;
		final String[] officeNames = new String[officeCount];
		final RawManagingOfficeMetaData<?>[][] officeManagedObjects = new RawManagingOfficeMetaData[officeCount][];
		for (int i = 0; i < officeNameManagedObjectListPairs.length; i += 2) {
			officeNames[i / 2] = (String) officeNameManagedObjectListPairs[i];
			officeManagedObjects[i / 2] = (RawManagingOfficeMetaData[]) officeNameManagedObjectListPairs[i + 1];
		}

		// Create the necessary mock objects
		final OfficeConfiguration[] officeConfigurations = new OfficeConfiguration[officeCount];
		final RawOfficeMetaData[] rawOfficeMetaDatas = new RawOfficeMetaData[officeCount];
		final OfficeMetaData[] officeMetaDatas = new OfficeMetaData[officeCount];
		for (int i = 0; i < officeCount; i++) {
			officeConfigurations[i] = this.createMock(OfficeConfiguration.class);
			rawOfficeMetaDatas[i] = this.createMock(RawOfficeMetaData.class);
			officeMetaDatas[i] = this.createMock(OfficeMetaData.class);
		}

		// Record the construction of the offices
		this.recordReturn(this.configuration, this.configuration.getOfficeConfiguration(), officeConfigurations);
		for (int i = 0; i < officeCount; i++) {
			final OfficeConfiguration officeConfiguration = officeConfigurations[i];
			final String officeName = officeNames[i];
			final RawManagingOfficeMetaData<?>[] officeManagingManagedObjects = (officeManagedObjects[i] != null
					? officeManagedObjects[i] : new RawManagingOfficeMetaData[0]);
			final RawOfficeMetaData rawOfficeMetaData = rawOfficeMetaDatas[i];
			final OfficeMetaData officeMetaData = officeMetaDatas[i];

			// Record construction of the office
			this.recordReturn(officeConfiguration, officeConfiguration.getOfficeName(), officeName);
			this.recordReturn(this.rawOfficeFactory,
					this.rawOfficeFactory.constructRawOfficeMetaData(officeConfiguration, this.issues,
							officeManagingManagedObjects, null, this.rawBoundMoFactory, this.rawGovernanceFactory,
							this.rawBoundAdminFactory, this.rawFunctionMetaDataFactory),
					rawOfficeMetaData, new AbstractMatcher() {
						@Override
						public boolean matches(Object[] e, Object[] a) {
							assertEquals("Incorrect office configuration", e[0], a[0]);
							assertEquals("Incorrect issues", e[1], a[1]);
							RawManagingOfficeMetaData<?>[] eMos = (RawManagingOfficeMetaData[]) e[2];
							RawManagingOfficeMetaData<?>[] aMos = (RawManagingOfficeMetaData[]) a[2];
							assertTrue("Must have raw office floor meta-data", a[3] instanceof RawOfficeFloorMetaData);
							assertEquals("Incorrect managed object factory",
									RawOfficeFloorMetaDataTest.this.rawBoundMoFactory, a[4]);
							assertEquals("Incorrect governance factory",
									RawOfficeFloorMetaDataTest.this.rawGovernanceFactory, a[5]);
							assertEquals("Incorrect administrator factory",
									RawOfficeFloorMetaDataTest.this.rawBoundAdminFactory, a[6]);
							assertEquals("Incorrect work factory",
									RawOfficeFloorMetaDataTest.this.rawFunctionMetaDataFactory, a[7]);

							// Validate the managed objects
							assertEquals("Incorrect number of managed objects", eMos.length, aMos.length);
							for (int i = 0; i < eMos.length; i++) {
								assertEquals("Incorrect managed object " + i, eMos[i], aMos[i]);
							}
							return true;
						}
					});
			this.recordReturn(rawOfficeMetaData, rawOfficeMetaData.getOfficeMetaData(), officeMetaData);
		}

		// Return the constructed office meta-data
		return officeMetaDatas;
	}

	/**
	 * Records the construction of the {@link ManagedObjectSourceInstance}
	 * instances.
	 * 
	 * @return {@link ManagedObjectSource} instances.
	 */
	private ManagedObjectSource<?, ?>[] record_constructManagedObjectInstances() {

		// Create the managed object sources
		ManagedObjectSource<?, ?>[] managedObjectSources = new ManagedObjectSource<?, ?>[this.constructedManagedObjects
				.size()];
		for (int i = 0; i < managedObjectSources.length; i++) {
			managedObjectSources[i] = this.createMock(ManagedObjectSource.class);
		}

		// Record constructing the managed object instances
		for (int i = 0; i < managedObjectSources.length; i++) {
			RawManagedObjectMetaData<?, ?> rawMoMetaData = this.constructedManagedObjects.get(i);
			ManagedObjectSource<?, ?> managedObjectSource = managedObjectSources[i];

			final RawManagingOfficeMetaData<?> managingOffice = this.createMock(RawManagingOfficeMetaData.class);
			final ManagedObjectExecuteContextFactory<?> executeContextFactory = this
					.createMock(ManagedObjectExecuteContextFactory.class);

			// Record construction of the managed object instance
			this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectSource(), managedObjectSource);
			this.recordReturn(rawMoMetaData, rawMoMetaData.getRawManagingOfficeMetaData(), managingOffice);
			this.recordReturn(managingOffice, managingOffice.getManagedObjectExecuteContextFactory(),
					executeContextFactory);
			this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectPool(), null);
		}

		// Return the managed object sources
		return managedObjectSources;
	}

	/**
	 * Records an issue for the {@link OfficeFloor}.
	 * 
	 * @param issueDescription
	 *            Description of issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.OFFICE_FLOOR, OFFICE_FLOOR_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link RawOfficeFloorMetaData}.
	 * 
	 * @param isConstruct
	 *            Indicates if the {@link RawOfficeFloorMetaData} should be
	 *            returned.
	 * @return {@link RawOfficeFloorMetaData}.
	 */
	private RawOfficeFloorMetaData constructRawOfficeFloorMetaData(boolean isConstruct) {

		// Create the raw office floor meta-data
		RawOfficeFloorMetaData metaData = RawOfficeFloorMetaDataImpl.getFactory().constructRawOfficeFloorMetaData(
				this.configuration, this.issues, this.rawTeamFactory, this.threadLocalAwareExecutor, this.rawMosFactory,
				this.rawBoundMoFactory, this.rawGovernanceFactory, this.rawBoundAdminFactory, this.rawOfficeFactory,
				this.rawFunctionMetaDataFactory);

		// Determine if constructed
		if (isConstruct) {
			assertNotNull("Meta-data should be constructed", metaData);
		} else {
			assertNull("Meta-data should not be constructed", metaData);
		}

		// Return the raw office floor meta-data
		return metaData;
	}
}