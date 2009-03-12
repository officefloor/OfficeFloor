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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeManagingManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link RawOfficeMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawOfficeMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

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
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

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
				.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(),
				new OfficeEnhancer[] { this.officeEnhancer });
		this.officeEnhancer.enhanceOffice(null);
		this.control(this.officeEnhancer).setMatcher(new AlwaysMatcher());
		this.control(this.officeEnhancer).setThrowable(failure);
		this.record_issue("Failure in enhancing office", failure);
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
				.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(), new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.configuration, this.configuration
				.getFlowNodeBuilder(null, "WORK", "TASK"), null);
		this
				.record_issue("Task 'TASK' of work 'WORK' not available for enhancement");
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
				.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(), new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.rawOfficeFloorMetaData,
				this.rawOfficeFloorMetaData
						.getRawManagedObjectMetaData("MANAGED_OBJECT"), null);
		this
				.record_issue("Managed Object Source 'MANAGED_OBJECT' not available for enhancement");
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
		this.record_enhanceOffice();
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this.recordReturn(this.linkedTeamConfiguration,
				this.linkedTeamConfiguration.getOfficeTeamName(), null);
		this.record_issue("Team registered to Office without name");
		this.record_noManagedObjectsAndAdministrators();
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
		this.record_enhanceOffice();
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this
				.recordReturn(this.linkedTeamConfiguration,
						this.linkedTeamConfiguration.getOfficeTeamName(),
						"OFFICE_TEAM");
		this.recordReturn(this.linkedTeamConfiguration,
				this.linkedTeamConfiguration.getOfficeFloorTeamName(), "");
		this
				.record_issue("No Office Floor Team name for Office Team 'OFFICE_TEAM'");
		this.record_noManagedObjectsAndAdministrators();
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
		this.record_enhanceOffice();
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
		this
				.record_issue("Unknown Team 'OFFICE_FLOOR_TEAM' not available to register to Office");
		this.record_noManagedObjectsAndAdministrators();
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
		this.record_enhanceOffice();
		this.record_teams();
		this
				.recordReturn(
						this.configuration,
						this.configuration.getRegisteredManagedObjectSources(),
						new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration.getOfficeManagedObjectName(), null);
		this.record_issue("Managed Object registered to Office without name");
		this.record_processBoundManagedObjects(null);
		this.record_processBoundAdministrators(null, null);
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
		this.record_enhanceOffice();
		this.record_teams();
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
		this
				.record_issue("No Managed Object Source name for Office Managed Object 'MO'");
		this.record_processBoundManagedObjects(null);
		this.record_processBoundAdministrators(null, null);
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

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
		this.record_enhanceOffice();
		this.record_teams();
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
		this
				.record_issue("Unknown Managed Object Source 'MOS' not available to register to Office");
		this.record_processBoundManagedObjects(null);
		this.record_processBoundAdministrators(null, null);
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct a {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void testConstructProcessBoundManagedObject() {

		// Record creating a process bound managed object
		this.record_enhanceOffice();
		this.record_teams();
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects = this
				.record_processBoundManagedObjects(
						registeredManagedObjectSources, "BOUND_MO");
		this.record_processBoundAdministrators(null, null);
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects,
				"BOUND_MO");
		this.record_linkTasksForManagedObjects(processManagedObjects,
				"BOUND_MO");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to affix {@link ProcessState} bound {@link ManagedObject}.
	 */
	public void testAffixProcessManagedObject() {

		final RawOfficeManagingManagedObjectMetaData affixManagedObject = this
				.createMock(RawOfficeManagingManagedObjectMetaData.class);
		this.officeManagingManagedObjects.add(affixManagedObject);

		// Record affixing a process managed object
		this.record_enhanceOffice();
		this.record_teams();
		this.record_registerManagedObjectSources();
		Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects = this
				.record_processBoundManagedObjects(null);
		this.record_processBoundAdministrators(null, null);
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects,
				"OFFICE_MO_0");
		this.record_linkTasksForManagedObjects(processManagedObjects,
				"OFFICE_MO_0");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct a {@link ThreadState} bound
	 * {@link ManagedObject}.
	 */
	public void testConstructThreadBoundManagedObject() {

		// Record creating a thread bound managed object
		this.record_enhanceOffice();
		Map<String, Team> teams = this.record_teams();
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects = this
				.record_processBoundManagedObjects(registeredManagedObjectSources);
		this.record_processBoundAdministrators(teams, processManagedObjects);
		Map<String, RawBoundManagedObjectMetaData<?>> threadManagedObjects = this
				.record_threadBoundManagedObjects(
						registeredManagedObjectSources, processManagedObjects,
						"BOUND_MO");
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(threadManagedObjects,
				"BOUND_MO");
		this
				.record_linkTasksForManagedObjects(threadManagedObjects,
						"BOUND_MO");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link ThreadState} and {@link ProcessState}
	 * bound {@link ManagedObject} instances.
	 */
	public void testConstructProcessAndThreadBoundManagedObjects() {

		// Record creating a thread bound managed object
		this.record_enhanceOffice();
		Map<String, Team> teams = this.record_teams();
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects = this
				.record_processBoundManagedObjects(
						registeredManagedObjectSources, "PROCESS");
		this.record_processBoundAdministrators(teams, processManagedObjects);
		Map<String, RawBoundManagedObjectMetaData<?>> threadManagedObjects = this
				.record_threadBoundManagedObjects(
						registeredManagedObjectSources, processManagedObjects,
						"THREAD");
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(threadManagedObjects,
				"THREAD");
		this.record_constructManagedObjectMetaData(processManagedObjects,
				"PROCESS");
		this.record_linkTasksForManagedObjects(threadManagedObjects, "THREAD");
		this
				.record_linkTasksForManagedObjects(processManagedObjects,
						"PROCESS");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct a {@link ProcessState} bound
	 * {@link Administrator}.
	 */
	public void testConstructProcessBoundAdministrator() {

		// Record creating a process bound administrator
		this.record_enhanceOffice();
		Map<String, Team> teams = this.record_teams("TEAM");
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects = this
				.record_processBoundManagedObjects(
						registeredManagedObjectSources, "BOUND_MO");
		Map<String, RawBoundAdministratorMetaData<?, ?>> processAdministrators = this
				.record_processBoundAdministrators(teams,
						processManagedObjects, "BOUND_ADMIN");
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects,
				"BOUND_MO");
		this.record_constructAdministratorMetaData(processAdministrators,
				"BOUND_ADMIN");
		this.record_linkTasksForManagedObjects(processManagedObjects,
				"BOUND_MO");
		this.record_linkTasksForAdministrators(processAdministrators,
				"BOUND_ADMIN");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct a {@link ThreadState} bound
	 * {@link Administrator}.
	 */
	public void testConstructThreadBoundAdministrator() {

		// Record creating a process bound administrator
		this.record_enhanceOffice();
		Map<String, Team> teams = this.record_teams("TEAM");
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects = this
				.record_processBoundManagedObjects(
						registeredManagedObjectSources, "PROCESS_MO");
		this.record_processBoundAdministrators(null, null);
		Map<String, RawBoundManagedObjectMetaData<?>> threadManagedObjects = this
				.record_threadBoundManagedObjects(
						registeredManagedObjectSources, processManagedObjects,
						"THREAD_MO");
		Map<String, RawBoundManagedObjectMetaData<?>> scopeManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		scopeManagedObjects.putAll(processManagedObjects);
		scopeManagedObjects.putAll(threadManagedObjects);
		Map<String, RawBoundAdministratorMetaData<?, ?>> threadAdministrators = this
				.record_threadBoundAdministrators(teams, scopeManagedObjects,
						"BOUND_ADMIN");
		this.record_work();
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects,
				"PROCESS_MO");
		this.record_constructManagedObjectMetaData(threadManagedObjects,
				"THREAD_MO");
		this.record_constructAdministratorMetaData(threadAdministrators,
				"BOUND_ADMIN");
		this.record_linkTasksForManagedObjects(processManagedObjects,
				"PROCESS_MO");
		this.record_linkTasksForManagedObjects(threadManagedObjects,
				"THREAD_MO");
		this.record_linkTasksForAdministrators(threadAdministrators,
				"BOUND_ADMIN");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Enable able to handle not constructing {@link Work}.
	 */
	public void testNotConstructWork() {

		// Record constructing work
		this.record_enhanceOffice();
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		this.record_work((RawWorkMetaData<?>) null);
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Enable able to construct {@link Work}.
	 */
	public void testConstructWork() {

		final RawWorkMetaData<?> rawWorkMetaData = this
				.createMock(RawWorkMetaData.class);

		// Record constructing work
		this.record_enhanceOffice();
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		WorkMetaData<?> workMetaData = this.record_work(rawWorkMetaData)[0];
		this.record_noOfficeStartupTasks();
		this.record_noOfficeEscalationHandler();
		this.recordReturn(workMetaData, workMetaData.getWorkName(), "WORK");
		this.recordReturn(workMetaData, workMetaData.getTaskMetaData(),
				new TaskMetaData[0]);
		this.record_linkTasksForWork(rawWorkMetaData);

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Enable issue if {@link OfficeStartupTask} not found.
	 */
	public void testUnknownStartupTask() {

		final RawWorkMetaData<?> rawWorkMetaData = this
				.createMock(RawWorkMetaData.class);
		final TaskNodeReference startupTaskReference = this
				.createMock(TaskNodeReference.class);

		// Record adding startup task
		this.record_enhanceOffice();
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		WorkMetaData<?> workMetaData = this.record_work(rawWorkMetaData)[0];
		this.recordReturn(this.configuration, this.configuration
				.getStartupTasks(),
				new TaskNodeReference[] { startupTaskReference });
		this.recordReturn(workMetaData, workMetaData.getWorkName(), "WORK");
		this.recordReturn(workMetaData, workMetaData.getTaskMetaData(),
				new TaskMetaData[0]);
		this.record_noOfficeEscalationHandler();
		this.recordReturn(startupTaskReference, startupTaskReference
				.getWorkName(), null); // no work name to not find
		this.record_issue("No work name provided for Startup Task 0");
		this.record_linkTasksForWork(rawWorkMetaData);

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this
				.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify startup task not loaded
		OfficeStartupTask[] startupTasks = officeMetaData.getStartupTasks();
		assertEquals("Incorrect number of startup task entries", 1,
				startupTasks.length);
		assertNull("Should not have startup task loaded", startupTasks[0]);
	}

	/**
	 * Enable able to link in an {@link OfficeStartupTask}.
	 */
	public void testConstructStartupTask() {

		final RawWorkMetaData<?> rawWorkMetaData = this
				.createMock(RawWorkMetaData.class);
		final TaskMetaData<?, ?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final TaskNodeReference startupTaskReference = this
				.createMock(TaskNodeReference.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);

		// Record adding startup task
		this.record_enhanceOffice();
		this.record_teams();
		this.record_noManagedObjectsAndAdministrators();
		WorkMetaData<?> workMetaData = this.record_work(rawWorkMetaData)[0];
		this.recordReturn(this.configuration, this.configuration
				.getStartupTasks(),
				new TaskNodeReference[] { startupTaskReference });
		this.recordReturn(workMetaData, workMetaData.getWorkName(), "WORK");
		this.recordReturn(workMetaData, workMetaData.getTaskMetaData(),
				new TaskMetaData[] { taskMetaData });
		this.recordReturn(taskMetaData, taskMetaData.getTaskName(), "TASK");
		this.record_noOfficeEscalationHandler();
		this.recordReturn(startupTaskReference, startupTaskReference
				.getWorkName(), "WORK");
		this.recordReturn(startupTaskReference, startupTaskReference
				.getTaskName(), "TASK");
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.OFFICE, OFFICE_NAME,
						"StartupTask0", this.issues), assetManager);
		this.record_linkTasksForWork(rawWorkMetaData);

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this
				.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify startup task loaded
		OfficeStartupTask[] startupTasks = officeMetaData.getStartupTasks();
		assertEquals("Incorrect number of startup tasks", 1,
				startupTasks.length);
		assertEquals("Incorrect startup task meta-data", taskMetaData,
				startupTasks[0].getFlowMetaData().getInitialTaskMetaData());
		assertEquals("Incorrect startup task asset manager", assetManager,
				startupTasks[0].getFlowMetaData().getFlowManager());
	}

	/**
	 * Records obtaining the {@link Office} name and providing no
	 * {@link OfficeEnhancer} instances.
	 */
	private void record_enhanceOffice() {
		this.recordReturn(this.configuration, this.configuration
				.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEnhancers(), new OfficeEnhancer[0]);
	}

	/**
	 * Records no {@link Team} instances.
	 * 
	 * @param teamNames
	 *            Names of the {@link Team} instances bound to the
	 *            {@link Office}.
	 * @return Mapping of {@link Team} instances by their {@link Office} bound
	 *         names.
	 */
	private Map<String, Team> record_teams(String... teamNames) {

		// Create configuration for each team name
		LinkedTeamConfiguration[] teamConfigurations = new LinkedTeamConfiguration[teamNames.length];
		for (int i = 0; i < teamConfigurations.length; i++) {
			teamConfigurations[i] = this
					.createMock(LinkedTeamConfiguration.class);
		}

		// Record registering the teams
		Map<String, Team> teams = new HashMap<String, Team>();
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredTeams(), teamConfigurations);
		for (int i = 0; i < teamNames.length; i++) {
			LinkedTeamConfiguration teamConfiguration = teamConfigurations[i];
			String teamName = teamNames[i];
			String officeFloorTeamName = teamName + "-officefloor";
			RawTeamMetaData rawTeam = this.createMock(RawTeamMetaData.class);
			Team team = this.createMock(Team.class);

			// Record registering the team
			this.recordReturn(teamConfiguration, teamConfiguration
					.getOfficeTeamName(), teamName);
			this.recordReturn(teamConfiguration, teamConfiguration
					.getOfficeFloorTeamName(), officeFloorTeamName);
			this.recordReturn(this.rawOfficeFloorMetaData,
					this.rawOfficeFloorMetaData
							.getRawTeamMetaData(officeFloorTeamName), rawTeam);
			this.recordReturn(rawTeam, rawTeam.getTeam(), team);

			// Register the team for return
			teams.put(teamName, team);
		}

		// Return the registry of the teams
		return teams;
	}

	/**
	 * Records registering {@link RawManagedObjectMetaData} instances to the
	 * {@link Office}.
	 * 
	 * @param managedObjectNames
	 *            Names that the {@link RawManagedObjectMetaData} instances are
	 *            registered under.
	 */
	private Map<String, RawManagedObjectMetaData<?, ?>> record_registerManagedObjectSources(
			String... managedObjectNames) {

		// Create configuration for each managed object name
		LinkedManagedObjectSourceConfiguration[] moConfigurations = new LinkedManagedObjectSourceConfiguration[managedObjectNames.length];
		for (int i = 0; i < moConfigurations.length; i++) {
			moConfigurations[i] = this
					.createMock(LinkedManagedObjectSourceConfiguration.class);
		}

		// Record registering the managed object sources and collect for return
		Map<String, RawManagedObjectMetaData<?, ?>> rawMoMetaDatas = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		this.recordReturn(this.configuration, this.configuration
				.getRegisteredManagedObjectSources(), moConfigurations);
		for (int i = 0; i < managedObjectNames.length; i++) {
			LinkedManagedObjectSourceConfiguration moConfiguration = moConfigurations[i];
			String managedObjectName = managedObjectNames[i];
			String managedObjectSourceName = managedObjectName + "-source";
			RawManagedObjectMetaData<?, ?> rawMoMetaData = this
					.createMock(RawManagedObjectMetaData.class);

			// Record registering the managed object
			this.recordReturn(moConfiguration, moConfiguration
					.getOfficeManagedObjectName(), managedObjectName);
			this.recordReturn(moConfiguration, moConfiguration
					.getOfficeFloorManagedObjectSourceName(),
					managedObjectSourceName);
			this
					.recordReturn(
							this.rawOfficeFloorMetaData,
							this.rawOfficeFloorMetaData
									.getRawManagedObjectMetaData(managedObjectSourceName),
							rawMoMetaData);

			// Load the managed object for return
			rawMoMetaDatas.put(managedObjectName, rawMoMetaData);
		}

		// Return the registered managed object meta-data
		return rawMoMetaDatas;
	}

	/**
	 * Records affixing {@link RawOfficeManagingManagedObjectMetaData}
	 * instances.
	 */
	private void record_affixOfficeManagingManagedObjects(
			final String expectedOfficeName,
			final RawBoundManagedObjectMetaData<?>[] expectedBoundMo,
			final RawOfficeManagingManagedObjectMetaData[] expectedOfficeMo,
			RawBoundManagedObjectMetaData<?>[] returnBoundManagedObjectMetaData) {
		this
				.recordReturn(this.rawBoundManagedObjectFactory,
						this.rawBoundManagedObjectFactory
								.affixOfficeManagingManagedObjects(
										expectedOfficeName, expectedBoundMo,
										expectedOfficeMo, this.issues),
						returnBoundManagedObjectMetaData,
						new AbstractMatcher() {
							@Override
							public boolean matches(Object[] expected,
									Object[] actual) {

								// Ensure correct office name
								assertEquals("Incorrect office name",
										expectedOfficeName, actual[0]);

								// Ensure correct process bound managed objects
								RawBoundManagedObjectMetaData<?>[] actualBoundMo = (RawBoundManagedObjectMetaData<?>[]) actual[1];
								if (expectedBoundMo == null) {
									// null is simple no managed objects
									assertEquals(
											"No bound managed objects expected",
											0, actualBoundMo.length);
								} else {
									// Validate bound managed objects
									assertEquals(
											"Incorrect number of bound managed objects",
											expectedBoundMo.length,
											actualBoundMo.length);
									for (int i = 0; i < expectedBoundMo.length; i++) {
										assertEquals(
												"Incorrect bound managed object "
														+ i,
												expectedBoundMo[i],
												actualBoundMo[i]);
									}
								}

								// Ensure correct office managed objects
								RawOfficeManagingManagedObjectMetaData[] actualOfficeMo = (RawOfficeManagingManagedObjectMetaData[]) actual[2];
								if (expectedOfficeMo == null) {
									// null is simple no managed objects
									assertEquals(
											"No office managed objects expected",
											0, actualOfficeMo.length);
								} else {
									// Validate office managed objects
									assertEquals(
											"Incorrect number of office managed objects",
											expectedOfficeMo.length,
											actualOfficeMo.length);
									for (int i = 0; i < expectedOfficeMo.length; i++) {
										assertEquals(
												"Incorrect office managed object "
														+ i,
												expectedOfficeMo[i],
												actualOfficeMo[i]);
									}
								}

								// Ensure correct issues
								assertEquals("Incorrect issues",
										RawOfficeMetaDataTest.this.issues,
										actual[3]);
								return true; // matches if here
							}
						});
	}

	/**
	 * Records constructing {@link ProcessState} bound {@link ManagedObject}
	 * instances.
	 * 
	 * @param registeredManagedObjectSources
	 *            Registered {@link RawManagedObjectMetaData} instances to the
	 *            {@link Office}.
	 * @param processBoundNames
	 *            Names of the {@link ProcessState} bound names.
	 * @return Mapping of {@link RawBoundManagedObjectMetaData} by its bound
	 *         name.
	 */
	private Map<String, RawBoundManagedObjectMetaData<?>> record_processBoundManagedObjects(
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources,
			String... processBoundNames) {

		final String OFFICE_MANAGING_PREFIX = "OFFICE_MO_";

		// Create the mock objects to register the process bound managed objects
		final RawOfficeManagingManagedObjectMetaData[] officeManagedObjects = this.officeManagingManagedObjects
				.toArray(new RawOfficeManagingManagedObjectMetaData[0]);
		final ManagedObjectConfiguration<?>[] moConfigurations = new ManagedObjectConfiguration[processBoundNames.length];
		final RawBoundManagedObjectMetaData<?>[] rawBoundMoMetaDatas = new RawBoundManagedObjectMetaData[processBoundNames.length];
		final RawBoundManagedObjectMetaData<?>[] officeBoundMoMetaDatas = new RawBoundManagedObjectMetaData[officeManagedObjects.length];
		final RawBoundManagedObjectMetaData<?>[] processBoundMoMetaDatas = new RawBoundManagedObjectMetaData[rawBoundMoMetaDatas.length
				+ officeBoundMoMetaDatas.length];
		final Map<String, RawBoundManagedObjectMetaData<?>> boundManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		for (int i = 0; i < processBoundNames.length; i++) {
			moConfigurations[i] = this
					.createMock(ManagedObjectConfiguration.class);
			rawBoundMoMetaDatas[i] = this
					.createMock(RawBoundManagedObjectMetaData.class);
			processBoundMoMetaDatas[i] = rawBoundMoMetaDatas[i];
			boundManagedObjects.put(processBoundNames[i],
					rawBoundMoMetaDatas[i]);
		}
		for (int i = 0; i < officeBoundMoMetaDatas.length; i++) {
			officeBoundMoMetaDatas[i] = this
					.createMock(RawBoundManagedObjectMetaData.class);
			processBoundMoMetaDatas[rawBoundMoMetaDatas.length + i] = officeBoundMoMetaDatas[i];
			boundManagedObjects.put(OFFICE_MANAGING_PREFIX + i,
					officeBoundMoMetaDatas[i]);
		}

		// Record constructing process bound managed objects
		this.recordReturn(this.configuration, this.configuration
				.getProcessManagedObjectConfiguration(), moConfigurations);
		if (processBoundNames.length > 0) {
			this.recordReturn(this.rawBoundManagedObjectFactory,
					this.rawBoundManagedObjectFactory
							.constructBoundManagedObjectMetaData(
									moConfigurations, this.issues,
									ManagedObjectScope.PROCESS,
									AssetType.OFFICE, OFFICE_NAME,
									registeredManagedObjectSources, null),
					rawBoundMoMetaDatas);
		}

		// Record affixing the office managing managed objects
		this.record_affixOfficeManagingManagedObjects(OFFICE_NAME,
				rawBoundMoMetaDatas, officeManagedObjects,
				processBoundMoMetaDatas);

		// Record creating map of the process bound managed objects
		for (int i = 0; i < processBoundNames.length; i++) {
			this.recordReturn(rawBoundMoMetaDatas[i], rawBoundMoMetaDatas[i]
					.getBoundManagedObjectName(), processBoundNames[i]);
		}
		for (int i = 0; i < officeBoundMoMetaDatas.length; i++) {
			this.recordReturn(officeBoundMoMetaDatas[i],
					officeBoundMoMetaDatas[i].getBoundManagedObjectName(),
					OFFICE_MANAGING_PREFIX + i);
		}

		// Return the bound managed objects
		return boundManagedObjects;
	}

	/**
	 * Records constructing {@link ThreadState} bound {@link ManagedObject}
	 * instances.
	 * 
	 * @param registeredManagedObjectSources
	 *            Registered {@link RawManagedObjectMetaData} instances to the
	 *            {@link Office}.
	 * @param processManagedObjects
	 *            {@link ProcessState} bound
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param threadBoundNames
	 *            Names of the {@link ThreadState} bound names.
	 * @return Mapping of {@link RawBoundManagedObjectMetaData} by its bound
	 *         name.
	 */
	private Map<String, RawBoundManagedObjectMetaData<?>> record_threadBoundManagedObjects(
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources,
			Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects,
			String... threadBoundNames) {

		// Create the mock objects to register the thread bound managed objects
		final ManagedObjectConfiguration<?>[] moConfigurations = new ManagedObjectConfiguration[threadBoundNames.length];
		final RawBoundManagedObjectMetaData<?>[] rawBoundMoMetaDatas = new RawBoundManagedObjectMetaData[threadBoundNames.length];
		final Map<String, RawBoundManagedObjectMetaData<?>> boundManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		for (int i = 0; i < threadBoundNames.length; i++) {
			moConfigurations[i] = this
					.createMock(ManagedObjectConfiguration.class);
			rawBoundMoMetaDatas[i] = this
					.createMock(RawBoundManagedObjectMetaData.class);
			boundManagedObjects
					.put(threadBoundNames[i], rawBoundMoMetaDatas[i]);
		}

		// Record constructing thread bound managed objects
		this.recordReturn(this.configuration, this.configuration
				.getThreadManagedObjectConfiguration(), moConfigurations);
		if (threadBoundNames.length > 0) {
			this
					.recordReturn(this.rawBoundManagedObjectFactory,
							this.rawBoundManagedObjectFactory
									.constructBoundManagedObjectMetaData(
											moConfigurations, this.issues,
											ManagedObjectScope.THREAD,
											AssetType.OFFICE, OFFICE_NAME,
											registeredManagedObjectSources,
											processManagedObjects),
							rawBoundMoMetaDatas);
		}
		for (int i = 0; i < threadBoundNames.length; i++) {
			this.recordReturn(rawBoundMoMetaDatas[i], rawBoundMoMetaDatas[i]
					.getBoundManagedObjectName(), threadBoundNames[i]);
		}

		// Return the bound managed objects
		return boundManagedObjects;
	}

	/**
	 * Records constructing {@link ProcessState} bound {@link Administrator}
	 * instances.
	 * 
	 * @param teams
	 *            {@link Team} instances by their {@link Office} bound names.
	 * @param processManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances by their
	 *            {@link ProcessState} bound names.
	 * @param processBoundNames
	 *            Names of the {@link ProcessState} bound names.
	 * @return Mapping of {@link RawBoundAdministratorMetaData} by its bound
	 *         name.
	 */
	private Map<String, RawBoundAdministratorMetaData<?, ?>> record_processBoundAdministrators(
			Map<String, Team> teams,
			Map<String, RawBoundManagedObjectMetaData<?>> processManagedObjects,
			String... processBoundNames) {

		// Create the mock objects to register the process bound administrators
		final AdministratorSourceConfiguration<?, ?>[] adminConfigurations = new AdministratorSourceConfiguration[processBoundNames.length];
		final RawBoundAdministratorMetaData<?, ?>[] rawBoundAdminMetaDatas = new RawBoundAdministratorMetaData[processBoundNames.length];
		final Map<String, RawBoundAdministratorMetaData<?, ?>> boundAdministrators = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		for (int i = 0; i < processBoundNames.length; i++) {
			adminConfigurations[i] = this
					.createMock(AdministratorSourceConfiguration.class);
			rawBoundAdminMetaDatas[i] = this
					.createMock(RawBoundAdministratorMetaData.class);
			boundAdministrators.put(processBoundNames[i],
					rawBoundAdminMetaDatas[i]);
		}

		// Record constructing process bound administrators
		this.recordReturn(this.configuration, this.configuration
				.getProcessAdministratorSourceConfiguration(),
				adminConfigurations);
		if (processBoundNames.length > 0) {
			this.recordReturn(this.rawBoundAdministratorFactory,
					this.rawBoundAdministratorFactory
							.constructRawBoundAdministratorMetaData(
									adminConfigurations, this.issues,
									AdministratorScope.PROCESS,
									AssetType.OFFICE, OFFICE_NAME, teams,
									processManagedObjects),
					rawBoundAdminMetaDatas);
		}
		for (int i = 0; i < processBoundNames.length; i++) {
			this.recordReturn(rawBoundAdminMetaDatas[i],
					rawBoundAdminMetaDatas[i].getBoundAdministratorName(),
					processBoundNames[i]);
		}

		// Return the process administrators
		return boundAdministrators;
	}

	/**
	 * Records constructing {@link ThreadState} bound {@link Administrator}
	 * instances.
	 * 
	 * @param teams
	 *            {@link Team} instances by their {@link Office} bound names.
	 * @param scopeManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances by their
	 *            {@link ProcessState} or {@link ThreadState} bound names.
	 * @param threadBoundNames
	 *            Names of the {@link ProcessState} bound names.
	 * @return Mapping of {@link RawBoundAdministratorMetaData} by its bound
	 *         name.
	 */
	private Map<String, RawBoundAdministratorMetaData<?, ?>> record_threadBoundAdministrators(
			Map<String, Team> teams,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeManagedObjects,
			String... threadBoundNames) {

		// Create the mock objects to register the thread bound administrators
		final AdministratorSourceConfiguration<?, ?>[] adminConfigurations = new AdministratorSourceConfiguration[threadBoundNames.length];
		final RawBoundAdministratorMetaData<?, ?>[] rawBoundAdminMetaDatas = new RawBoundAdministratorMetaData[threadBoundNames.length];
		final Map<String, RawBoundAdministratorMetaData<?, ?>> boundAdministrators = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		for (int i = 0; i < threadBoundNames.length; i++) {
			adminConfigurations[i] = this
					.createMock(AdministratorSourceConfiguration.class);
			rawBoundAdminMetaDatas[i] = this
					.createMock(RawBoundAdministratorMetaData.class);
			boundAdministrators.put(threadBoundNames[i],
					rawBoundAdminMetaDatas[i]);
		}

		// Record constructing thread bound administrators
		this.recordReturn(this.configuration, this.configuration
				.getThreadAdministratorSourceConfiguration(),
				adminConfigurations);
		if (threadBoundNames.length > 0) {
			this.recordReturn(this.rawBoundAdministratorFactory,
					this.rawBoundAdministratorFactory
							.constructRawBoundAdministratorMetaData(
									adminConfigurations, this.issues,
									AdministratorScope.THREAD,
									AssetType.OFFICE, OFFICE_NAME, teams,
									scopeManagedObjects),
					rawBoundAdminMetaDatas);
		}
		for (int i = 0; i < threadBoundNames.length; i++) {
			this.recordReturn(rawBoundAdminMetaDatas[i],
					rawBoundAdminMetaDatas[i].getBoundAdministratorName(),
					threadBoundNames[i]);
		}

		// Return the process administrators
		return boundAdministrators;
	}

	/**
	 * Records no {@link ManagedObject} and {@link Administrator} instances.
	 */
	private void record_noManagedObjectsAndAdministrators() {
		this.record_registerManagedObjectSources();
		this.record_processBoundManagedObjects(null);
		this.record_processBoundAdministrators(null, null);
		this.record_threadBoundManagedObjects(null, null);
		this.record_threadBoundAdministrators(null, null);
	}

	/**
	 * Records constructing the {@link ManagedObjectMetaData}.
	 * 
	 * @param boundManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances by their bound
	 *            names.
	 * @param constructManagedObjectNames
	 *            Names of the {@link RawBoundManagedObjectMetaData} to
	 *            construct the {@link ManagedObjectMetaData}.
	 * @return Constructed {@link ManagedObjectMetaData} instances.
	 */
	private ManagedObjectMetaData<?>[] record_constructManagedObjectMetaData(
			Map<String, RawBoundManagedObjectMetaData<?>> boundManagedObjects,
			String... constructManagedObjectNames) {
		ManagedObjectMetaData<?>[] moMetaDatas = new ManagedObjectMetaData[constructManagedObjectNames.length];
		for (int i = 0; i < constructManagedObjectNames.length; i++) {
			moMetaDatas[i] = this.createMock(ManagedObjectMetaData.class);
			RawBoundManagedObjectMetaData<?> rawBoundMo = boundManagedObjects
					.get(constructManagedObjectNames[i]);
			this.recordReturn(rawBoundMo,
					rawBoundMo.getManagedObjectMetaData(), moMetaDatas[i]);
		}
		return moMetaDatas;
	}

	/**
	 * Records constructing the {@link AdministratorMetaData}.
	 * 
	 * @param boundAdministrators
	 *            {@link RawBoundAdministratorMetaData} instances by their bound
	 *            names.
	 * @param constructAdministratorNames
	 *            Names of the {@link RawBoundAdministratorMetaData} to
	 *            construct the {@link AdministratorMetaData}.
	 * @return Constructed {@link AdministratorMetaData} instances.
	 */
	private AdministratorMetaData<?, ?>[] record_constructAdministratorMetaData(
			Map<String, RawBoundAdministratorMetaData<?, ?>> boundAdministrators,
			String... constructAdministratorNames) {
		AdministratorMetaData<?, ?>[] adminMetaDatas = new AdministratorMetaData[constructAdministratorNames.length];
		for (int i = 0; i < constructAdministratorNames.length; i++) {
			adminMetaDatas[i] = this.createMock(AdministratorMetaData.class);
			RawBoundAdministratorMetaData<?, ?> rawBoundAdmin = boundAdministrators
					.get(constructAdministratorNames[i]);
			this.recordReturn(rawBoundAdmin, rawBoundAdmin
					.getAdministratorMetaData(), adminMetaDatas[i]);
		}
		return adminMetaDatas;
	}

	/**
	 * Links the {@link Task} instances for the {@link ManagedObject} instances.
	 * 
	 * @param boundManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances by their bound
	 *            names.
	 * @param managedObjectNames
	 *            Names of the {@link RawBoundManagedObjectMetaData} to have
	 *            their {@link Task} instances linked.
	 */
	private void record_linkTasksForManagedObjects(
			Map<String, RawBoundManagedObjectMetaData<?>> boundManagedObjects,
			String... managedObjectNames) {
		for (int i = 0; i < managedObjectNames.length; i++) {
			RawBoundManagedObjectMetaData<?> rawBoundMo = boundManagedObjects
					.get(managedObjectNames[i]);
			rawBoundMo.linkTasks(null, this.issues);
			this.control(rawBoundMo).setMatcher(
					new TypeMatcher(TaskMetaDataLocator.class,
							OfficeFloorIssues.class));
		}
	}

	/**
	 * Links the {@link Task} instances for the {@link Administrator} instances.
	 * 
	 * @param boundAdministrators
	 *            {@link RawBoundAdministratorMetaData} instances by their bound
	 *            names.
	 * @param administratorNames
	 *            Names of the {@link RawBoundAdministratorMetaData} to have
	 *            their {@link Task} instances linked.
	 */
	private void record_linkTasksForAdministrators(
			Map<String, RawBoundAdministratorMetaData<?, ?>> boundAdministrators,
			String... administratorNames) {
		for (int i = 0; i < administratorNames.length; i++) {
			RawBoundAdministratorMetaData<?, ?> rawBoundAdmin = boundAdministrators
					.get(administratorNames[i]);
			rawBoundAdmin
					.linkTasks(null, this.assetManagerFactory, this.issues);
			this.control(rawBoundAdmin)
					.setMatcher(
							new TypeMatcher(TaskMetaDataLocator.class,
									AssetManagerFactory.class,
									OfficeFloorIssues.class));
		}
	}

	/**
	 * Records construction of {@link Work} instances.
	 * 
	 * @param rawWorkMetaDatas
	 *            {@link RawWorkMetaData} instances. Provide <code>null</code>
	 *            for a value should not construct that {@link Work}.
	 */
	private WorkMetaData<?>[] record_work(
			RawWorkMetaData<?>... rawWorkMetaDatas) {

		// Create the work configurations
		WorkConfiguration<?>[] workConfigurations = new WorkConfiguration[rawWorkMetaDatas.length];
		for (int i = 0; i < workConfigurations.length; i++) {
			workConfigurations[i] = this.createMock(WorkConfiguration.class);
		}

		// Record constructing the work instances
		this.recordReturn(this.configuration, this.configuration
				.getWorkConfiguration(), workConfigurations);
		List<WorkMetaData<?>> workMetaDatas = new LinkedList<WorkMetaData<?>>();
		for (int i = 0; i < rawWorkMetaDatas.length; i++) {
			RawWorkMetaData<?> rawWorkMetaData = rawWorkMetaDatas[i];

			// Record constructing the raw work
			this.recordReturn(this.rawWorkMetaDataFactory,
					this.rawWorkMetaDataFactory.constructRawWorkMetaData(
							workConfigurations[i], this.issues, null,
							this.assetManagerFactory,
							this.rawBoundManagedObjectFactory,
							this.rawBoundAdministratorFactory,
							this.rawTaskMetaDataFactory), rawWorkMetaData,
					new AbstractMatcher() {
						@Override
						public boolean matches(Object[] e, Object[] a) {
							assertEquals("Incorrect work configuration", e[0],
									a[0]);
							assertEquals("Incorrect issues", e[1], a[1]);
							assertTrue("Must have raw office meta-data",
									a[2] instanceof RawOfficeMetaData);
							assertEquals("Incorrect asset manager factory",
									e[3], a[3]);
							assertEquals(
									"Incorrect bound managed object factory",
									e[4], a[4]);
							assertEquals(
									"Incorrect bound administrator factory",
									e[5], a[5]);
							assertEquals("Incorrect task factory", e[6], a[6]);
							return true;
						}
					});

			// Determine if construct work
			if (rawWorkMetaData != null) {
				// Record constructing work and have it registered for return
				WorkMetaData<?> workMetaData = this
						.createMock(WorkMetaData.class);
				this.recordReturn(rawWorkMetaData, rawWorkMetaData
						.getWorkMetaData(), workMetaData);
				workMetaDatas.add(workMetaData);
			}
		}

		// Return the work meta-data
		return workMetaDatas.toArray(new WorkMetaData[0]);
	}

	/**
	 * Links the {@link Task} instances for the {@link Work} instances.
	 * 
	 * @param rawWorkMetaDatas
	 *            {@link RawWorkMetaData} instances.
	 */
	private void record_linkTasksForWork(RawWorkMetaData<?>... rawWorkMetaDatas) {
		for (int i = 0; i < rawWorkMetaDatas.length; i++) {
			rawWorkMetaDatas[i].linkTasks(null, this.assetManagerFactory,
					this.issues);
			this.control(rawWorkMetaDatas[i])
					.setMatcher(
							new TypeMatcher(TaskMetaDataLocator.class,
									AssetManagerFactory.class,
									OfficeFloorIssues.class));
		}
	}

	/**
	 * Records no {@link OfficeStartupTask} instances.
	 */
	private void record_noOfficeStartupTasks() {
		this.recordReturn(this.configuration, this.configuration
				.getStartupTasks(), null);
	}

	/**
	 * Records no {@link Office} {@link EscalationHandler}.
	 */
	private void record_noOfficeEscalationHandler() {
		this.recordReturn(this.configuration, this.configuration
				.getOfficeEscalationHandler(), null);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, issueDescription,
				cause);
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
						this.assetManagerFactory,
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