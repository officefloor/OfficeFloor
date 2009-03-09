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
package net.officefloor.frame.impl.construct.office;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeManagingManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link RawOfficeMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawOfficeMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeConfiguration}.
	 */
	private final OfficeConfiguration configuration = this
			.createMock(OfficeConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link OfficeEnhancer}.
	 */
	private final OfficeEnhancer officeEnhancer = this
			.createMock(OfficeEnhancer.class);

	/**
	 * {@link RawOfficeFloorMetaData}.
	 */
	private final RawOfficeFloorMetaData rawOfficeFloorMetaData = this
			.createMock(RawOfficeFloorMetaData.class);

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory = this
			.createMock(RawBoundManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawBoundAdministratorMetaDataFactory}.
	 */
	private final RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory = this
			.createMock(RawBoundAdministratorMetaDataFactory.class);

	/**
	 * {@link LinkedTeamConfiguration}.
	 */
	private final LinkedTeamConfiguration linkedTeamConfiguration = this
			.createMock(LinkedTeamConfiguration.class);

	/**
	 * {@link LinkedManagedObjectSourceConfiguration}.
	 */
	private final LinkedManagedObjectSourceConfiguration linkedMosConfiguration = this
			.createMock(LinkedManagedObjectSourceConfiguration.class);

	/**
	 * {@link RawOfficeManagingManagedObjectMetaData} instances.
	 */
	private final List<RawOfficeManagingManagedObjectMetaData> officeManagingManagedObjects = new LinkedList<RawOfficeManagingManagedObjectMetaData>();

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
	 * Ensure issue if no {@link Office} name.
	 */
	public void testNoOfficeName() {

		// Record no office name
		this.recordReturn(this.configuration, this.configuration
				.getOfficeName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Office registered without name");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if fail to enhance office.
	 */
	public void testFailOfficeEnhancing() {

		final RuntimeException failure = new RuntimeException("fail enhancing");

		// Record failing to enhance office
		this.recordReturn(this.configuration, this.configuration
				.getOfficeName(), "OFFICE");
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(),
				new OfficeEnhancer[] { this.officeEnhancer });
		this.officeEnhancer.enhanceOffice(null);
		this.control(this.officeEnhancer).setMatcher(new AlwaysMatcher());
		this.control(this.officeEnhancer).setThrowable(failure);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Failure in enhancing office", failure);
		this.record_noTeams();
		this.record_noManagedObjectsAndAdministrators();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no flow to be enhanced.
	 */
	public void testNoFlowForOfficeEnhancing() {

		// Office enhancer
		OfficeEnhancer enhancer = new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getFlowNodeBuilder("WORK", "TASK");
				fail("Should not continue on flow node not available");
			}
		};

		// Record attempting to enhance office
		this.recordReturn(this.configuration, this.configuration
				.getOfficeName(), "OFFICE");
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(), new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.configuration, this.configuration
				.getFlowNodeBuilder(null, "WORK", "TASK"), null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Task 'TASK' of work 'WORK' not available for enhancement");
		this.record_noTeams();
		this.record_noManagedObjectsAndAdministrators();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no managed object to be enhanced.
	 */
	public void testNoManagedObjectForOfficeEnhancing() {

		// Office enhancer
		OfficeEnhancer enhancer = new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getManagedObjectHandlerBuilder("MANAGED_OBJECT",
						None.class);
				fail("Should not continue on flow node not available");
			}
		};

		// Record attempting to enhance office
		this.recordReturn(this.configuration, this.configuration
				.getOfficeName(), "OFFICE");
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(), new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.rawOfficeFloorMetaData,
				this.rawOfficeFloorMetaData
						.getRawManagedObjectMetaData("MANAGED_OBJECT"), null);
		this.issues
				.addIssue(AssetType.OFFICE, "OFFICE",
						"Managed Object Source 'MANAGED_OBJECT' not available for enhancement");
		this.record_noTeams();
		this.record_noManagedObjectsAndAdministrators();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Office} {@link Team} name.
	 */
	public void testNoRegisteredTeamName() {

		// Record attempting to register team without name
		this.record_enhanceOffice("OFFICE");
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this.recordReturn(this.linkedTeamConfiguration,
				this.linkedTeamConfiguration.getOfficeTeamName(), null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Team registered to Office without name");
		this.record_noManagedObjectsAndAdministrators();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link OfficeFloor} {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record attempting to register team without name
		this.record_enhanceOffice("OFFICE");
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this
				.recordReturn(this.linkedTeamConfiguration,
						this.linkedTeamConfiguration.getOfficeTeamName(),
						"OFFICE_TEAM");
		this.recordReturn(this.linkedTeamConfiguration,
				this.linkedTeamConfiguration.getOfficeFloorTeamName(), "");
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"No Office Floor Team name for Office Team 'OFFICE_TEAM'");
		this.record_noManagedObjectsAndAdministrators();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record attempting to register unknown team
		this.record_enhanceOffice("OFFICE");
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this
				.recordReturn(this.linkedTeamConfiguration,
						this.linkedTeamConfiguration.getOfficeTeamName(),
						"OFFICE_TEAM");
		this.recordReturn(this.linkedTeamConfiguration,
				this.linkedTeamConfiguration.getOfficeFloorTeamName(),
				"OFFICE_FLOOR_TEAM");
		this.recordReturn(this.rawOfficeFloorMetaData,
				this.rawOfficeFloorMetaData
						.getRawTeamMetaData("OFFICE_FLOOR_TEAM"), null);
		this.issues
				.addIssue(AssetType.OFFICE, "OFFICE",
						"Unknown Team 'OFFICE_FLOOR_TEAM' not available to register to Office");
		this.record_noManagedObjectsAndAdministrators();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Office} {@link ManagedObject} name.
	 */
	public void testNoRegisteredManagedObjectName() {

		// Record attempting to register managed object without name
		this.record_enhanceOffice("OFFICE");
		this.record_noTeams();
		this
				.recordReturn(
						this.configuration,
						this.configuration.getRegisteredManagedObjectSources(),
						new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration.getOfficeManagedObjectName(), null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Managed Object registered to Office without name");
		this.recordReturn(this.configuration, this.configuration
				.getProcessManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getProcessAdministratorSourceConfiguration(), null);
		this.recordReturn(this.configuration, this.configuration
				.getThreadManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getThreadAdministratorSourceConfiguration(), null);

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link OfficeFloor} {@link ManagedObjectSource} name.
	 */
	public void testNoManagedObjectSourceName() {

		// Record attempting to register managed object source without name
		this.record_enhanceOffice("OFFICE");
		this.record_noTeams();
		this
				.recordReturn(
						this.configuration,
						this.configuration.getRegisteredManagedObjectSources(),
						new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration.getOfficeManagedObjectName(), "MO");
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration
						.getOfficeFloorManagedObjectSourceName(), null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"No Managed Object Source name for Office Managed Object 'MO'");
		this.recordReturn(this.configuration, this.configuration
				.getProcessManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getProcessAdministratorSourceConfiguration(), null);
		this.recordReturn(this.configuration, this.configuration
				.getThreadManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getThreadAdministratorSourceConfiguration(), null);

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedObjectSource}.
	 */
	public void testUnknownManagedObjectSource() {

		// Record attempting to register unknown managed object source
		this.record_enhanceOffice("OFFICE");
		this.record_noTeams();
		this
				.recordReturn(
						this.configuration,
						this.configuration.getRegisteredManagedObjectSources(),
						new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration.getOfficeManagedObjectName(), "MO");
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration
						.getOfficeFloorManagedObjectSourceName(), "MOS");
		this.recordReturn(this.rawOfficeFloorMetaData,
				this.rawOfficeFloorMetaData.getRawManagedObjectMetaData("MOS"),
				null);
		this.issues
				.addIssue(AssetType.OFFICE, "OFFICE",
						"Unknown Managed Object Source 'MOS' not available to register to Office");
		this.recordReturn(this.configuration, this.configuration
				.getProcessManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getProcessAdministratorSourceConfiguration(), null);
		this.recordReturn(this.configuration, this.configuration
				.getThreadManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getThreadAdministratorSourceConfiguration(), null);

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Records obtaining the {@link Office} name and providing no
	 * {@link OfficeEnhancer} instances.
	 * 
	 * @param officeName
	 *            {@link Office} name.
	 */
	private void record_enhanceOffice(String officeName) {
		this.recordReturn(this.configuration, this.configuration
				.getOfficeName(), officeName);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(), new OfficeEnhancer[0]);
	}

	/**
	 * Records no {@link Team} instances.
	 */
	private void record_noTeams() {
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(), new LinkedTeamConfiguration[0]);
	}

	/**
	 * Records no {@link ManagedObject} and {@link Administrator} instances.
	 */
	private void record_noManagedObjectsAndAdministrators() {
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredManagedObjectSources(),
				new LinkedManagedObjectSourceConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getProcessManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getProcessAdministratorSourceConfiguration(),
				new AdministratorSourceConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getThreadManagedObjectConfiguration(),
				new ManagedObjectConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getThreadAdministratorSourceConfiguration(),
				new AdministratorSourceConfiguration[0]);
	}

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            Flag indicating if should be constructed.
	 * @return Constructed {@link RawOfficeMetaData}.
	 */
	private RawOfficeMetaData constructRawOfficeMetaData(
			boolean isExpectConstruct) {

		// Obtain the office managing managed objects
		RawOfficeManagingManagedObjectMetaData[] officeMos = this.officeManagingManagedObjects
				.toArray(new RawOfficeManagingManagedObjectMetaData[0]);

		// Construct the meta-data
		RawOfficeMetaData metaData = RawOfficeMetaDataImpl.getFactory()
				.constructRawOfficeMetaData(this.configuration, this.issues,
						officeMos, this.rawOfficeFloorMetaData,
						this.rawBoundManagedObjectFactory,
						this.rawBoundAdministratorFactory,
						this.rawWorkMetaDataFactory,
						this.rawTaskMetaDataFactory);
		if (isExpectConstruct) {
			assertNotNull("Meta-data should be constructed", metaData);
		} else {
			assertNull("Meta-data should NOT be constructed", metaData);
		}

		// Return the meta-data
		return metaData;
	}

}