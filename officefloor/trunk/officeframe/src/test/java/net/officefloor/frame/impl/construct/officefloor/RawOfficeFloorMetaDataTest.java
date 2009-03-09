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
package net.officefloor.frame.impl.construct.officefloor;

import java.util.Arrays;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.construct.RawTeamMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawOfficeFloorMetaDataImpl}.
 * 
 * @author Daniel
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
	 * {@link TeamConfiguration}.
	 */
	private final TeamConfiguration<?> teamConfiguration = this
			.createMock(TeamConfiguration.class);

	/**
	 * {@link RawTeamMetaDataFactory}.
	 */
	private final RawTeamMetaDataFactory rawTeamFactory = this
			.createMock(RawTeamMetaDataFactory.class);

	/**
	 * {@link RawTeamMetaData}.
	 */
	private final RawTeamMetaData rawTeamMetaData = this
			.createMock(RawTeamMetaData.class);

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedObjectSourceConfiguration mosConfiguration = this
			.createMock(ManagedObjectSourceConfiguration.class);

	/**
	 * {@link RawManagedObjectMetaDataFactory}.
	 */
	private final RawManagedObjectMetaDataFactory rawMosFactory = this
			.createMock(RawManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private final RawManagedObjectMetaData<?, ?> rawMosMetaData = this
			.createMock(RawManagedObjectMetaData.class);

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
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

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
	 * Ensures issue if no {@link OfficeFloor} name.
	 */
	public void testNoOfficeFloorName() {

		// Record no office floor name
		this.recordReturn(this.configuration, this.configuration
				.getOfficeFloorName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "Unknown",
				"Name not provided for Office Floor");
		this.record_constructTeams();
		this.record_constructManagedObjectSources();
		this.record_constructEscalationProcedure();

		// Attempt to construct office floor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensures handle not construct {@link Team}.
	 */
	public void testNotConstructTeam() {

		// Record not construct team
		this.record_officeFloorName();
		this.recordReturn(this.configuration, this.configuration
				.getTeamConfiguration(),
				new TeamConfiguration[] { this.teamConfiguration });
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(this.teamConfiguration, this.issues),
				null);

		// Focus on teams
		this.record_constructManagedObjectSources();
		this.record_constructEscalationProcedure();

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
		final RawTeamMetaData teamOne = this.createMock(RawTeamMetaData.class);
		final RawTeamMetaData teamTwo = this.createMock(RawTeamMetaData.class);

		// Record construct teams with duplicate name
		this.record_officeFloorName();
		this.recordReturn(this.configuration, this.configuration
				.getTeamConfiguration(), new TeamConfiguration[] {
				this.teamConfiguration, this.teamConfiguration });
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(this.teamConfiguration, this.issues),
				teamOne);
		this.recordReturn(teamOne, teamOne.getTeamName(), DUPLICATE_TEAM_NAME);
		this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
				.constructRawTeamMetaData(this.teamConfiguration, this.issues),
				teamTwo);
		this.recordReturn(teamTwo, teamTwo.getTeamName(), DUPLICATE_TEAM_NAME);
		this.record_officeFloorIssue("Teams registered with the same name '"
				+ DUPLICATE_TEAM_NAME + "'");

		// Focus on teams
		this.record_constructManagedObjectSources();
		this.record_constructEscalationProcedure();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure obtain the only first team
		assertEquals("Incorrect team", teamOne, metaData
				.getRawTeamMetaData(DUPLICATE_TEAM_NAME));
	}

	/**
	 * Ensures successfully construct {@link Team} instances.
	 */
	public void testConstructTeams() {

		// Record constructing teams
		this.record_officeFloorName();
		this.record_constructTeams("ONE", "TWO", "THREE");
		this.record_constructManagedObjectSources();
		this.record_constructEscalationProcedure();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure teams registered
		assertNotNull(metaData.getRawTeamMetaData("ONE"));
		assertNotNull(metaData.getRawTeamMetaData("TWO"));
		assertNotNull(metaData.getRawTeamMetaData("THREE"));
	}

	/**
	 * Ensures not construct if not construct {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testNotConstructManagedObject() {

		// Record not construct managed object
		this.record_officeFloorName();
		this.record_constructTeams();
		this
				.recordReturn(
						this.configuration,
						this.configuration
								.getManagedObjectSourceConfiguration(),
						new ManagedObjectSourceConfiguration[] { this.mosConfiguration });
		this.recordReturn(this.rawMosFactory, this.rawMosFactory
				.constructRawManagedObjectMetaData(this.mosConfiguration,
						this.issues, this.assetManagerFactory,
						this.configuration), null);
		this.record_constructEscalationProcedure();

		// Attempt to construct office floor
		this.replayMockObjects();
		this.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if two {@link ManagedObjectSource} instances by same name.
	 */
	@SuppressWarnings("unchecked")
	public void testDuplicateManagedObjectSourceNames() {

		final String DUPLICATE_MANAGED_OBJECT_SOURCE_NAME = "MANAGED_OBJECT_SOURCE";
		final RawManagedObjectMetaData mosOne = this
				.createMock(RawManagedObjectMetaData.class);
		final RawManagedObjectMetaData mosTwo = this
				.createMock(RawManagedObjectMetaData.class);

		// Record construct managed object sources with duplicate name
		this.record_officeFloorName();
		this.record_constructTeams();
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceConfiguration(),
				new ManagedObjectSourceConfiguration[] { this.mosConfiguration,
						this.mosConfiguration });
		this.recordReturn(this.rawMosFactory, this.rawMosFactory
				.constructRawManagedObjectMetaData(this.mosConfiguration,
						this.issues, this.assetManagerFactory,
						this.configuration), mosOne);
		this.recordReturn(mosOne, mosOne.getManagedObjectName(),
				DUPLICATE_MANAGED_OBJECT_SOURCE_NAME);
		this.recordReturn(this.rawMosFactory, this.rawMosFactory
				.constructRawManagedObjectMetaData(this.mosConfiguration,
						this.issues, this.assetManagerFactory,
						this.configuration), mosTwo);
		this.recordReturn(mosTwo, mosTwo.getManagedObjectName(),
				DUPLICATE_MANAGED_OBJECT_SOURCE_NAME);
		this
				.record_officeFloorIssue("Managed object sources registered with the same name '"
						+ DUPLICATE_MANAGED_OBJECT_SOURCE_NAME + "'");
		this.record_constructEscalationProcedure();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure obtain the only first managed object
		assertEquals(
				"Incorrect managed object source",
				mosOne,
				metaData
						.getRawManagedObjectMetaData(DUPLICATE_MANAGED_OBJECT_SOURCE_NAME));
	}

	/**
	 * Ensures successfully construct {@link ManagedObjectSource} instances.
	 */
	public void testConstructManagedObjectSources() {

		// Record construction of the teams
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources("ONE", "TWO", "THREE");
		this.record_constructEscalationProcedure();

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure teams registered
		assertNotNull(metaData.getRawManagedObjectMetaData("ONE"));
		assertNotNull(metaData.getRawManagedObjectMetaData("TWO"));
		assertNotNull(metaData.getRawManagedObjectMetaData("THREE"));
	}

	/**
	 * Ensures handle no {@link EscalationProcedure}.
	 */
	public void testNoEscalationProcedure() {

		// Record no escalation procedure
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources();
		this.recordReturn(this.configuration, this.configuration
				.getEscalationProcedure(), null);

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure default escalation procedure
		EscalationProcedure escalationProcedure = metaData
				.getEscalationProcedure();
		assertTrue("Must default escalation procedure",
				escalationProcedure instanceof EscalationProcedureImpl);
	}

	/**
	 * Ensures handle provided a {@link EscalationProcedure}.
	 */
	public void testProvideEscalationProcedure() {

		final EscalationProcedure escalationProcedure = this
				.createMock(EscalationProcedure.class);

		// Record have escalation procedure
		this.record_officeFloorName();
		this.record_constructTeams();
		this.record_constructManagedObjectSources();
		this.recordReturn(this.configuration, this.configuration
				.getEscalationProcedure(), escalationProcedure);

		// Attempt to construct office floor
		this.replayMockObjects();
		RawOfficeFloorMetaData metaData = this
				.constructRawOfficeFloorMetaData();
		this.verifyMockObjects();

		// Ensure provide escalation procedure
		assertEquals("Must be able to provide escalation procedure",
				escalationProcedure, metaData.getEscalationProcedure());
	}

	/**
	 * Records obtaining the {@link OfficeFloor} name.
	 */
	private void record_officeFloorName() {
		this.recordReturn(this.configuration, this.configuration
				.getOfficeFloorName(), OFFICE_FLOOR_NAME);
	}

	/**
	 * Records construction of {@link RawTeamMetaData} instances.
	 * 
	 * @param teamNames
	 *            Names of the {@link Team} instances to construct.
	 */
	private void record_constructTeams(String... teamNames) {

		// Record obtaining the team configuration
		TeamConfiguration<?>[] teamConfigurations = new TeamConfiguration[teamNames.length];
		Arrays.fill(teamConfigurations, this.teamConfiguration);
		this.recordReturn(this.configuration, this.configuration
				.getTeamConfiguration(), teamConfigurations);

		// Record constructing the teams
		for (int i = 0; i < teamNames.length; i++) {
			this.recordReturn(this.rawTeamFactory, this.rawTeamFactory
					.constructRawTeamMetaData(this.teamConfiguration,
							this.issues), this.rawTeamMetaData);
			this.recordReturn(this.rawTeamMetaData, this.rawTeamMetaData
					.getTeamName(), teamNames[i]);
		}
	}

	/**
	 * Records construction of {@link RawManagedObjectMetaData} instances.
	 * 
	 * @param managedObjectSourceNames
	 *            Names of the {@link ManagedObjectSource} instances to
	 *            construct.
	 */
	@SuppressWarnings("unchecked")
	private void record_constructManagedObjectSources(
			String... managedObjectSourceNames) {

		// Record obtaining the managed object source configuration
		ManagedObjectSourceConfiguration<?, ?>[] mosConfigurations = new ManagedObjectSourceConfiguration[managedObjectSourceNames.length];
		Arrays.fill(mosConfigurations, this.mosConfiguration);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceConfiguration(), mosConfigurations);

		// Record constructing the teams
		for (int i = 0; i < managedObjectSourceNames.length; i++) {
			this.recordReturn(this.rawMosFactory, this.rawMosFactory
					.constructRawManagedObjectMetaData(this.mosConfiguration,
							this.issues, this.assetManagerFactory,
							this.configuration), this.rawMosMetaData);
			this.recordReturn(this.rawMosMetaData, this.rawMosMetaData
					.getManagedObjectName(), managedObjectSourceNames[i]);
		}
	}

	/**
	 * Records construction of the {@link EscalationProcedure}.
	 */
	private void record_constructEscalationProcedure() {
		this.recordReturn(this.configuration, this.configuration
				.getEscalationProcedure(), null);
	}

	/**
	 * Records an issue for the {@link OfficeFloor}.
	 * 
	 * @param issueDescription
	 *            Description of issue.
	 */
	private void record_officeFloorIssue(String issueDescription) {
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
						this.rawBoundAdminFactory, this.assetManagerFactory,
						this.rawOfficeFactory, this.rawWorkMetaDataFactory,
						this.rawTaskMetaDataFactory);

		// Meta-data should always be constructed
		assertNotNull("Meta-data should be constructed", metaData);

		// Return the raw office floor meta-data
		return metaData;
	}
}