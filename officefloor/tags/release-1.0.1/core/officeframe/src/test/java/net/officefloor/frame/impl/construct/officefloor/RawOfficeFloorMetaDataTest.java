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

package net.officefloor.frame.impl.construct.officefloor;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.officefloor.DefaultOfficeFloorEscalationHandler;
import net.officefloor.frame.impl.execute.process.EscalationHandlerEscalation;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.construct.RawTeamMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteContextFactory;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

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
	private final OfficeFloorConfiguration configuration = this
			.createMock(OfficeFloorConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link RawTeamMetaDataFactory}.
	 */
	private final RawTeamMetaDataFactory rawTeamFactory = this
			.createMock(RawTeamMetaDataFactory.class);

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
	 * {@link RawBoundAdministratorMetaDataFactory}.
	 */
	private final RawBoundAdministratorMetaDataFactory rawBoundAdminFactory = this
			.createMock(RawBoundAdministratorMetaDataFactory.class);

	/**
	 * {@link RawOfficeMetaDataFactory}.
	 */
	private final RawOfficeMetaDataFactory rawOfficeFactory = this
			.createMock(RawOfficeMetaDataFactory.class);

	/**
	 * {@link RawWorkMetaDataFactory}.
	 */
	private final RawWorkMetaDataFactory rawWorkMetaDataFactory = this
			.createMock(RawWorkMetaDataFactory.class);

	/**
	 * {@link RawTaskMetaDataFactory}.
	 */
	private final RawTaskMetaDataFactory rawTaskMetaDataFactory = this
			.createMock(RawTaskMetaDataFactory.class);

	/**
	 * Listing of the constructed {@link RawManagedObjectMetaData} instances.
	 */
	private final List<RawManagedObjectMetaData<?, ?>> constructedManagedObjects = new LinkedList<RawManagedObjectMetaData<?, ?>>();

	/**
	 * Ensures issue if no {@link OfficeFloor} name.
	 */
	public void testNoOfficeFloorName() {

		// Record no office floor name
		this.recordReturn(this.configuration,
				this.configuration.getOfficeFloorName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "Unknown",
				"Name not provided for Office Floor");
		this.record_constructTeams();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensures handle not construct {@link Team}.
	 */
	public void testNotConstructTeam() {

		final TeamConfiguration<?> teamConfiguration = this
				.createMock(TeamConfiguration.class);

		// Record not construct team
		this.record_officeFloorName();
		this.recordReturn(this.configuration,
				this.configuration.getTeamConfiguration(),
				new TeamConfiguration[] { teamConfiguration });
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(teamConfiguration, this.issues), null);
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if two {@link Team} instances by same name.
	 */
	public void testDuplicateTeamNames() {

		final String DUPLICATE_TEAM_NAME = "TEAM";

		final TeamConfiguration<?> teamConfigurationOne = this
				.createMock(TeamConfiguration.class);
		final RawTeamMetaData rawTeamOne = this
				.createMock(RawTeamMetaData.class);
		final Team teamOne = this.createMock(Team.class);

		final TeamConfiguration<?> teamConfigurationTwo = this
				.createMock(TeamConfiguration.class);
		final RawTeamMetaData rawTeamTwo = this
				.createMock(RawTeamMetaData.class);

		// Record construct teams with duplicate name
		this.record_officeFloorName();
		this.recordReturn(this.configuration,
				this.configuration.getTeamConfiguration(),
				new TeamConfiguration[] { teamConfigurationOne,
						teamConfigurationTwo });
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(teamConfigurationOne, this.issues),
				rawTeamOne);
		this.recordReturn(rawTeamOne, rawTeamOne.getTeamName(),
				DUPLICATE_TEAM_NAME);
		this.recordReturn(rawTeamOne, rawTeamOne.getTeam(), teamOne);
		this.recordReturn(rawTeamOne, rawTeamOne.getProcessContextListeners(),
				new ProcessContextListener[0]);
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(teamConfigurationTwo, this.issues),
				rawTeamTwo);
		this.recordReturn(rawTeamTwo, rawTeamTwo.getTeamName(),
				DUPLICATE_TEAM_NAME);
		this.record_issue("Teams registered with the same name '"
				+ DUPLICATE_TEAM_NAME + "'");
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure obtain the only first team
		assertEquals("Incorrect team", rawTeamOne,
				metaData.getRawTeamMetaData(DUPLICATE_TEAM_NAME));
	}

	/**
	 * Ensures successfully construct {@link Team} instances.
	 */
	public void testConstructTeams() {

		// Record constructing teams
		this.record_officeFloorName();
		Team[] expectedTeams = this
				.record_constructTeams("ONE", "TWO", "THREE");
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		Team[] actualTeams = metaData.getOfficeFloorMetaData().getTeams();
		this.verifyMockObjects();

		// Ensure teams registered
		assertNotNull(metaData.getRawTeamMetaData("ONE"));
		assertNotNull(metaData.getRawTeamMetaData("TWO"));
		assertNotNull(metaData.getRawTeamMetaData("THREE"));

		// Should have no Process Context Listeners
		assertEquals("Should not have Process Context Listeners", 0,
				metaData.getProcessContextListeners().length);

		// Validate the teams
		assertEquals("Incorrect number of teams", 3, actualTeams.length);
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect team " + i, expectedTeams[i],
					actualTeams[i]);
		}
	}

	/**
	 * Ensures successfully construct a {@link Team} with a
	 * {@link ProcessContextListener}.
	 */
	public void testConstructTeamWithProcessContextListener() {

		// Mock Process Context Listener
		final ProcessContextListener listener = this
				.createMock(ProcessContextListener.class);
		final TeamConfiguration<?> teamConfiguration = this
				.createMock(TeamConfiguration.class);
		final RawTeamMetaData rawTeamMetaData = this
				.createMock(RawTeamMetaData.class);
		final String TEAM_NAME = "TEAM";
		final Team team = this.createMock(Team.class);

		// Record constructing team registering Process Context Listener
		this.record_officeFloorName();
		this.recordReturn(this.configuration,
				this.configuration.getTeamConfiguration(),
				new TeamConfiguration[] { teamConfiguration });
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(teamConfiguration, this.issues),
				rawTeamMetaData);
		this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeamName(),
				TEAM_NAME);
		this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeam(), team);
		this.recordReturn(rawTeamMetaData,
				rawTeamMetaData.getProcessContextListeners(),
				new ProcessContextListener[] { listener });
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure team registered
		assertNotNull(metaData.getRawTeamMetaData("TEAM"));

		// Should have a Process Context Listener
		ProcessContextListener[] listeners = metaData
				.getProcessContextListeners();
		assertEquals("Should have Process Context Listeners", 1,
				listeners.length);
		assertEquals("Incorrect registered Process Context Listener", listener,
				listeners[0]);
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
		this.record_constructTeams();
		this.recordReturn(this.configuration,
				this.configuration.getManagedObjectSourceConfiguration(),
				new ManagedObjectSourceConfiguration[] { mosConfiguration });
		this.recordReturn(this.rawMosFactory, this.rawMosFactory
				.constructRawManagedObjectMetaData(mosConfiguration,
						this.issues, this.configuration), null);
		this.record_constructEscalation();
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData();
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
		final RawManagedObjectMetaData mosOne = this
				.createMock(RawManagedObjectMetaData.class);
		final RawManagingOfficeMetaData rawOfficeMoMetaData = this
				.createMock(RawManagingOfficeMetaData.class);

		final ManagedObjectSourceConfiguration mosConfigurationTwo = this
				.createMock(ManagedObjectSourceConfiguration.class);
		final RawManagedObjectMetaData mosTwo = this
				.createMock(RawManagedObjectMetaData.class);

		// Record construct managed object sources with duplicate name
		this.record_officeFloorName();
		this.record_constructTeams();
		this.recordReturn(this.configuration,
				this.configuration.getManagedObjectSourceConfiguration(),
				new ManagedObjectSourceConfiguration[] { mosConfigurationOne,
						mosConfigurationTwo });
		this.recordReturn(this.rawMosFactory, this.rawMosFactory
				.constructRawManagedObjectMetaData(mosConfigurationOne,
						this.issues, this.configuration), mosOne);
		this.recordReturn(mosOne, mosOne.getManagedObjectName(),
				DUPLICATE_MANAGED_OBJECT_SOURCE_NAME);
		this.recordReturn(mosOne, mosOne.getRawManagingOfficeMetaData(),
				rawOfficeMoMetaData);
		this.recordReturn(rawOfficeMoMetaData,
				rawOfficeMoMetaData.getManagingOfficeName(), "OFFICE");
		this.constructedManagedObjects.add(mosOne); // constructed later
		this.recordReturn(this.rawMosFactory, this.rawMosFactory
				.constructRawManagedObjectMetaData(mosConfigurationTwo,
						this.issues, this.configuration), mosTwo);
		this.recordReturn(mosTwo, mosTwo.getManagedObjectName(),
				DUPLICATE_MANAGED_OBJECT_SOURCE_NAME);
		this.record_issue("Managed object sources registered with the same name '"
				+ DUPLICATE_MANAGED_OBJECT_SOURCE_NAME + "'");
		this.record_constructEscalation();
		this.record_constructOffices("OFFICE",
				new RawManagingOfficeMetaData[] { rawOfficeMoMetaData });
		this.record_constructManagedObjectInstances();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure obtain the only first managed object
		assertEquals(
				"Incorrect managed object source",
				mosOne,
				metaData.getRawManagedObjectMetaData(DUPLICATE_MANAGED_OBJECT_SOURCE_NAME));
	}

	/**
	 * Ensures issue if no managing {@link Office} for
	 * {@link ManagedObjectSource}.
	 */
	public void testNoManagingOfficeForManagedObjectSource() {

		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);
		final String managedObjectSourceName = "MO";

		// Record no managing office for managed object
		this.record_officeFloorName();
		this.record_constructTeams();
		RawManagingOfficeMetaData<?> managingOffice = this
				.record_constructManagedObjectSources("UNKNOWN_OFFICE",
						managedObjectSourceName)[0];
		this.record_constructEscalation();
		this.record_constructOffices();
		this.recordReturn(managingOffice,
				managingOffice.getRawManagedObjectMetaData(), rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(),
				managedObjectSourceName);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
				"Can not find managing office 'UNKNOWN_OFFICE'");
		this.record_constructManagedObjectInstances();

		// Attempt to construct office floor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensures successfully construct {@link ManagedObjectSource} instances.
	 */
	public void testConstructManagedObjectSources() {

		// Record construction of the teams
		this.record_officeFloorName();
		this.record_constructTeams();
		RawManagingOfficeMetaData<?>[] managingOffices = this
				.record_constructManagedObjectSources("OFFICE", "ONE", "TWO",
						"THREE");
		this.record_constructEscalation();
		this.record_constructOffices("OFFICE", managingOffices);
		ManagedObjectSource<?, ?>[] managedObjectSources = this
				.record_constructManagedObjectInstances();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		ManagedObjectSourceInstance<?>[] mosInstances = metaData
				.getOfficeFloorMetaData().getManagedObjectSourceInstances();
		this.verifyMockObjects();

		// Ensure managed object sources registered
		assertNotNull(metaData.getRawManagedObjectMetaData("ONE"));
		assertNotNull(metaData.getRawManagedObjectMetaData("TWO"));
		assertNotNull(metaData.getRawManagedObjectMetaData("THREE"));

		// Validate managed object source instances
		assertEquals("Incorrect number of managed object source instances", 3,
				mosInstances.length);
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect managed object source instance " + i,
					managedObjectSources[i],
					mosInstances[i].getManagedObjectSource());
		}
	}

	/**
	 * Ensures handle no {@link EscalationHandler}.
	 */
	public void testNoEscalationHandler() {

		// Record no escalation procedure
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources("OFFICE");
		this.recordReturn(this.configuration,
				this.configuration.getEscalationHandler(), null);
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure default escalation handler
		EscalationHandlerEscalation officeFloorEscalation = (EscalationHandlerEscalation) metaData
				.getOfficeFloorEscalation();
		assertTrue(
				"Must be default escalation handler",
				officeFloorEscalation.getEscalationHandler() instanceof DefaultOfficeFloorEscalationHandler);
	}

	/**
	 * Ensures handle provided a {@link EscalationProcedure}.
	 */
	public void testProvideEscalationProcedure() {

		final EscalationHandler escalationHandler = this
				.createMock(EscalationHandler.class);

		// Record have escalation handler
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources("OFFICE");
		this.recordReturn(this.configuration,
				this.configuration.getEscalationHandler(), escalationHandler);
		this.record_constructOffices();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure use provided escalation handler
		EscalationHandlerEscalation officeFloorEscalation = (EscalationHandlerEscalation) metaData
				.getOfficeFloorEscalation();
		assertEquals("Must be able to provide escalation handler",
				escalationHandler, officeFloorEscalation.getEscalationHandler());
	}

	/**
	 * Ensure issue if no {@link Office} name.
	 */
	public void testNoOfficeName() {

		final OfficeConfiguration officeConfiguration = this
				.createMock(OfficeConfiguration.class);

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.recordReturn(this.configuration,
				this.configuration.getOfficeConfiguration(),
				new OfficeConfiguration[] { officeConfiguration });
		this.recordReturn(officeConfiguration,
				officeConfiguration.getOfficeName(), null);
		this.record_issue("Office added without a name");

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		OfficeMetaData[] officeMetaDatas = metaData.getOfficeFloorMetaData()
				.getOfficeMetaData();
		this.verifyMockObjects();

		// Ensure no office meta-data
		assertEquals("Should not have office meta-data", 0,
				officeMetaDatas.length);
	}

	/**
	 * Ensure handle not construct the {@link Office}.
	 */
	public void testNoConstructOffice() {

		final OfficeConfiguration officeConfiguration = this
				.createMock(OfficeConfiguration.class);

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		this.recordReturn(this.configuration,
				this.configuration.getOfficeConfiguration(),
				new OfficeConfiguration[] { officeConfiguration });
		this.recordReturn(officeConfiguration,
				officeConfiguration.getOfficeName(), "OFFICE");
		this.recordReturn(this.rawOfficeFactory, this.rawOfficeFactory
				.constructRawOfficeMetaData(officeConfiguration, this.issues,
						null, null, this.rawBoundMoFactory,
						this.rawBoundAdminFactory, this.rawWorkMetaDataFactory,
						this.rawTaskMetaDataFactory), null, new AlwaysMatcher());

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		OfficeMetaData[] officeMetaDatas = metaData.getOfficeFloorMetaData()
				.getOfficeMetaData();
		this.verifyMockObjects();

		// Ensure no office meta-data
		assertEquals("Should not have office meta-data", 0,
				officeMetaDatas.length);
	}

	/**
	 * Ensure able to construct an {@link Office}.
	 */
	public void testConstructOffice() {

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources("OFFICE");
		this.record_constructEscalation();
		OfficeMetaData expectedOffice = this.record_constructOffices("OFFICE",
				null)[0];

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		OfficeMetaData actualOffice = metaData.getOfficeFloorMetaData()
				.getOfficeMetaData()[0];
		this.verifyMockObjects();

		// Ensure have office meta-data
		assertEquals("Incorrect office meta-data", expectedOffice, actualOffice);
	}

	/**
	 * Records obtaining the {@link OfficeFloor} name.
	 */
	private void record_officeFloorName() {
		this.recordReturn(this.configuration,
				this.configuration.getOfficeFloorName(), OFFICE_FLOOR_NAME);
	}

	/**
	 * Records construction of {@link RawTeamMetaData} instances.
	 * 
	 * @param teamNames
	 *            Names of the {@link Team} instances to construct.
	 * @return {@link Team} instances.
	 */
	private Team[] record_constructTeams(String... teamNames) {

		// Create the mock objects
		TeamConfiguration<?>[] teamConfigurations = new TeamConfiguration[teamNames.length];
		RawTeamMetaData[] rawTeamMetaDatas = new RawTeamMetaData[teamNames.length];
		Team[] teams = new Team[teamNames.length];
		for (int i = 0; i < teamNames.length; i++) {
			teamConfigurations[i] = this.createMock(TeamConfiguration.class);
			rawTeamMetaDatas[i] = this.createMock(RawTeamMetaData.class);
			teams[i] = this.createMock(Team.class);
		}

		// Record obtaining the team configuration
		this.recordReturn(this.configuration,
				this.configuration.getTeamConfiguration(), teamConfigurations);

		// Record constructing the teams
		for (int i = 0; i < teamNames.length; i++) {
			TeamConfiguration<?> teamConfiguration = teamConfigurations[i];
			String teamName = teamNames[i];
			RawTeamMetaData rawTeamMetaData = rawTeamMetaDatas[i];
			Team team = teams[i];

			// Record constructing the team
			this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
					.constructRawTeamMetaData(teamConfiguration, this.issues),
					rawTeamMetaData);
			this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeamName(),
					teamName);
			this.recordReturn(rawTeamMetaData, rawTeamMetaData.getTeam(), team);
			this.recordReturn(rawTeamMetaData,
					rawTeamMetaData.getProcessContextListeners(),
					new ProcessContextListener[0]);
		}

		// Return the constructed teams
		return teams;
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
	private RawManagingOfficeMetaData[] record_constructManagedObjectSources(
			String officeName, String... managedObjectSourceNames) {

		// Create the mock objects
		ManagedObjectSourceConfiguration<?, ?>[] mosConfigurations = new ManagedObjectSourceConfiguration[managedObjectSourceNames.length];
		RawManagedObjectMetaData<?, ?>[] rawMoMetaDatas = new RawManagedObjectMetaData[managedObjectSourceNames.length];
		RawManagingOfficeMetaData[] managingOffices = new RawManagingOfficeMetaData[managedObjectSourceNames.length];
		for (int i = 0; i < mosConfigurations.length; i++) {
			mosConfigurations[i] = this
					.createMock(ManagedObjectSourceConfiguration.class);
			rawMoMetaDatas[i] = this.createMock(RawManagedObjectMetaData.class);
			managingOffices[i] = this
					.createMock(RawManagingOfficeMetaData.class);
		}

		// Record obtaining the managed object source configuration
		this.recordReturn(this.configuration,
				this.configuration.getManagedObjectSourceConfiguration(),
				mosConfigurations);

		// Record constructing the managed object sources
		for (int i = 0; i < managedObjectSourceNames.length; i++) {
			ManagedObjectSourceConfiguration mosConfiguration = mosConfigurations[i];
			String managedObjectSourceName = managedObjectSourceNames[i];
			RawManagedObjectMetaData<?, ?> rawMoMetaData = rawMoMetaDatas[i];
			RawManagingOfficeMetaData managingOffice = managingOffices[i];

			// Record constructing managed object source
			this.recordReturn(this.rawMosFactory, this.rawMosFactory
					.constructRawManagedObjectMetaData(mosConfiguration,
							this.issues, this.configuration), rawMoMetaData);
			this.recordReturn(rawMoMetaData,
					rawMoMetaData.getManagedObjectName(),
					managedObjectSourceName);
			this.recordReturn(rawMoMetaData,
					rawMoMetaData.getRawManagingOfficeMetaData(),
					managingOffice);
			this.recordReturn(managingOffice,
					managingOffice.getManagingOfficeName(), officeName);

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
		this.recordReturn(this.configuration,
				this.configuration.getEscalationHandler(), null);
	}

	/**
	 * Records constructing {@link OfficeMetaData} instances.
	 * 
	 * @param officeNameManagedObjectListPairs
	 *            Listing of {@link Office} name and
	 *            {@link RawManagingOfficeMetaData} array pairs.
	 * @return {@link OfficeMetaData} instances.
	 */
	private OfficeMetaData[] record_constructOffices(
			Object... officeNameManagedObjectListPairs) {

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
			officeConfigurations[i] = this
					.createMock(OfficeConfiguration.class);
			rawOfficeMetaDatas[i] = this.createMock(RawOfficeMetaData.class);
			officeMetaDatas[i] = this.createMock(OfficeMetaData.class);
		}

		// Record the construction of the offices
		this.recordReturn(this.configuration,
				this.configuration.getOfficeConfiguration(),
				officeConfigurations);
		for (int i = 0; i < officeCount; i++) {
			final OfficeConfiguration officeConfiguration = officeConfigurations[i];
			final String officeName = officeNames[i];
			final RawManagingOfficeMetaData<?>[] officeManagingManagedObjects = (officeManagedObjects[i] != null ? officeManagedObjects[i]
					: new RawManagingOfficeMetaData[0]);
			final RawOfficeMetaData rawOfficeMetaData = rawOfficeMetaDatas[i];
			final OfficeMetaData officeMetaData = officeMetaDatas[i];

			// Record construction of the office
			this.recordReturn(officeConfiguration,
					officeConfiguration.getOfficeName(), officeName);
			this.recordReturn(this.rawOfficeFactory, this.rawOfficeFactory
					.constructRawOfficeMetaData(officeConfiguration,
							this.issues, officeManagingManagedObjects, null,
							this.rawBoundMoFactory, this.rawBoundAdminFactory,
							this.rawWorkMetaDataFactory,
							this.rawTaskMetaDataFactory), rawOfficeMetaData,
					new AbstractMatcher() {
						@Override
						public boolean matches(Object[] e, Object[] a) {
							assertEquals("Incorrect office configuration",
									e[0], a[0]);
							assertEquals("Incorrect issues", e[1], a[1]);
							RawManagingOfficeMetaData<?>[] eMos = (RawManagingOfficeMetaData[]) e[2];
							RawManagingOfficeMetaData<?>[] aMos = (RawManagingOfficeMetaData[]) a[2];
							assertTrue("Must have raw office floor meta-data",
									a[3] instanceof RawOfficeFloorMetaData);
							assertEquals(
									"Incorrect managed object factory",
									RawOfficeFloorMetaDataTest.this.rawBoundMoFactory,
									a[4]);
							assertEquals(
									"Incorrect administrator factory",
									RawOfficeFloorMetaDataTest.this.rawBoundAdminFactory,
									a[5]);
							assertEquals(
									"Incorrect work factory",
									RawOfficeFloorMetaDataTest.this.rawWorkMetaDataFactory,
									a[6]);
							assertEquals(
									"Incorrect task factory",
									RawOfficeFloorMetaDataTest.this.rawTaskMetaDataFactory,
									a[7]);

							// Validate the managed objects
							assertEquals("Incorrect number of managed objects",
									eMos.length, aMos.length);
							for (int i = 0; i < eMos.length; i++) {
								assertEquals("Incorrect managed object " + i,
										eMos[i], aMos[i]);
							}
							return true;
						}
					});
			this.recordReturn(rawOfficeMetaData,
					rawOfficeMetaData.getOfficeMetaData(), officeMetaData);
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
			managedObjectSources[i] = this
					.createMock(ManagedObjectSource.class);
		}

		// Record constructing the managed object instances
		for (int i = 0; i < managedObjectSources.length; i++) {
			RawManagedObjectMetaData<?, ?> rawMoMetaData = this.constructedManagedObjects
					.get(i);
			ManagedObjectSource<?, ?> managedObjectSource = managedObjectSources[i];

			final RawManagingOfficeMetaData<?> managingOffice = this
					.createMock(RawManagingOfficeMetaData.class);
			final ManagedObjectExecuteContextFactory<?> executeContextFactory = this
					.createMock(ManagedObjectExecuteContextFactory.class);

			// Record construction of the managed object instance
			this.recordReturn(rawMoMetaData,
					rawMoMetaData.getManagedObjectSource(), managedObjectSource);
			this.recordReturn(rawMoMetaData,
					rawMoMetaData.getRawManagingOfficeMetaData(),
					managingOffice);
			this.recordReturn(managingOffice,
					managingOffice.getManagedObjectExecuteContextFactory(),
					executeContextFactory);
			this.recordReturn(rawMoMetaData,
					rawMoMetaData.getManagedObjectPool(), null);
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
		this.issues.addIssue(AssetType.OFFICE_FLOOR, OFFICE_FLOOR_NAME,
				issueDescription);
	}

	/**
	 * Constructs the {@link RawOfficeFloorMetaDataImpl}.
	 * 
	 * @return {@link RawOfficeFloorMetaDataImpl}.
	 */
	private RawOfficeFloorMetaData constructRawOfficeFloorMetaData() {

		// Create the raw office floor meta-data
		RawOfficeFloorMetaData metaData = RawOfficeFloorMetaDataImpl
				.getFactory().constructRawOfficeFloorMetaData(
						this.configuration, this.issues, this.rawTeamFactory,
						this.rawMosFactory, this.rawBoundMoFactory,
						this.rawBoundAdminFactory, this.rawOfficeFactory,
						this.rawWorkMetaDataFactory,
						this.rawTaskMetaDataFactory);

		// Meta-data should always be constructed
		assertNotNull("Meta-data should be constructed", metaData);

		// Return the raw office floor meta-data
		return metaData;
	}
}