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
package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.easymock.AbstractMatcher;
import org.easymock.ArgumentsMatcher;
import org.easymock.internal.AlwaysMatcher;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.ProcessContextListener;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionEscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawAdministrationMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaData;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.frame.util.MetaDataTestInstanceFactory;

/**
 * Tests the {@link RawOfficeMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawOfficeMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeConfiguration}.
	 */
	private final OfficeConfiguration configuration = this.createMock(OfficeConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link OfficeEnhancer}.
	 */
	private final OfficeEnhancer officeEnhancer = this.createMock(OfficeEnhancer.class);

	/**
	 * {@link RawOfficeFloorMetaData}.
	 */
	private final RawOfficeFloorMetaData rawOfficeFloorMetaData = this.createMock(RawOfficeFloorMetaData.class);

	/**
	 * {@link ProcessState} bound {@link RawBoundManagedObjectMetaData}.
	 */
	private Map<String, RawBoundManagedObjectMetaData> processManagedObjects = new HashMap<>();

	/**
	 * {@link ConstructBoundManagedObjectsMatcher}.
	 */
	private final ConstructBoundManagedObjectsMatcher constructBoundObjectsMatcher = new ConstructBoundManagedObjectsMatcher();

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory = this
			.createMock(RawBoundManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawGovernanceMetaDataFactory}.
	 */
	private final RawGovernanceMetaDataFactory rawGovernanceFactory = this
			.createMock(RawGovernanceMetaDataFactory.class);

	/**
	 * {@link RawAdministrationMetaDataFactory}.
	 */
	private final RawAdministrationMetaDataFactory rawBoundAdministratorFactory = this
			.createMock(RawAdministrationMetaDataFactory.class);

	/**
	 * {@link LinkedTeamConfiguration}.
	 */
	private final LinkedTeamConfiguration linkedTeamConfiguration = this.createMock(LinkedTeamConfiguration.class);

	/**
	 * {@link LinkedManagedObjectSourceConfiguration}.
	 */
	private final LinkedManagedObjectSourceConfiguration linkedMosConfiguration = this
			.createMock(LinkedManagedObjectSourceConfiguration.class);

	/**
	 * {@link RawManagingOfficeMetaData} instances.
	 */
	private final List<RawManagingOfficeMetaData<?>> officeManagingManagedObjects = new LinkedList<RawManagingOfficeMetaData<?>>();

	/**
	 * Bound input {@link ManagedObject} mapping.
	 */
	private final Map<String, String> boundInputManagedObjects = new HashMap<String, String>();

	/**
	 * {@link RawManagedFunctionMetaDataFactory}.
	 */
	private final RawManagedFunctionMetaDataFactory rawTaskMetaDataFactory = this
			.createMock(RawManagedFunctionMetaDataFactory.class);

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation = this.createMock(EscalationFlow.class);

	/**
	 * {@link RawGovernanceMetaData} instances by their registered names.
	 */
	private final Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaDatas = new HashMap<String, RawGovernanceMetaData<?, ?>>();

	/**
	 * {@link TeamManagement} instances by their {@link Office} names.
	 */
	private Map<String, TeamManagement> officeTeams;

	/**
	 * Ensure issue if no {@link Office} name.
	 */
	public void testNoOfficeName() {

		// Record no office name
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OfficeFloor", "Office registered without name");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to override the {@link OfficeClock}.
	 */
	public void testProvideOfficeClock() {

		final OfficeClock clock = this.createMock(OfficeClock.class);

		// Record providing profiler
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), 1000);
		this.recordReturn(this.configuration, this.configuration.getOfficeEnhancers(), new OfficeEnhancer[0]);
		this.record_teams();
		this.recordReturn(this.configuration, this.configuration.getOfficeClock(), clock);
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure override clock
		assertSame("Should override office clock", clock, rawOfficeMetaData.getOfficeMetaData().getOfficeClock());
	}

	/**
	 * Ensure able to default the {@link OfficeClock}.
	 */
	public void testDefaultOfficeClock() {

		// Record providing profiler
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure override clock
		assertNotNull("Should default office clock", rawOfficeMetaData.getOfficeMetaData().getOfficeClock());

	}

	/**
	 * Ensure issue if negative monitor {@link Office} interval.
	 */
	public void testNegativeMonitorOfficeInterval() {

		// Record negative monitor office interval
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), -1);
		this.record_issue("Monitor office interval can not be negative");

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
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), 1000);
		this.recordReturn(this.configuration, this.configuration.getOfficeEnhancers(),
				new OfficeEnhancer[] { this.officeEnhancer });
		this.officeEnhancer.enhanceOffice(null);
		this.control(this.officeEnhancer).setMatcher(new AlwaysMatcher());
		this.control(this.officeEnhancer).setThrowable(failure);
		this.record_issue("Failure in enhancing office", failure);
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
				context.getFlowNodeBuilder("TASK");
				fail("Should not continue on flow node not available");
			}
		};

		// Record attempting to enhance office
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), 1000);
		this.recordReturn(this.configuration, this.configuration.getOfficeEnhancers(),
				new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.configuration, this.configuration.getFlowNodeBuilder(null, "TASK"), null);
		this.record_issue("ManagedFunction 'TASK' of namespace '[none]' not available for enhancement");
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedObject} {@link ManagedFunction} in
	 * {@link Office}.
	 */
	public void testNoManagedObjectFunctionForOfficeEnhancing() {

		// Office enhancer
		OfficeEnhancer enhancer = new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getFlowNodeBuilder("MANAGED_OBJECT", "TASK");
				fail("Should not continue on flow node not available");
			}
		};

		// Record attempting to enhance office
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), 1000);
		this.recordReturn(this.configuration, this.configuration.getOfficeEnhancers(),
				new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.configuration, this.configuration.getFlowNodeBuilder("MANAGED_OBJECT", "TASK"), null);
		this.record_issue("ManagedFunction 'TASK' of namespace 'MANAGED_OBJECT' not available for enhancement");
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain {@link FlowNodeBuilder} for an
	 * {@link OfficeEnhancer}.
	 */
	public void testGetFlowNodeBuilderForOfficeEnhancing() {

		final FlowNodeBuilder<?> flowNodeBuilder = this.createMock(FlowNodeBuilder.class);

		// Office enhancer
		OfficeEnhancer enhancer = new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				assertEquals("Incorrect flow node builder", flowNodeBuilder,
						context.getFlowNodeBuilder("MANAGED_OBJECT", "TASK"));
			}
		};

		// Record attempting to enhance office
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), 1000);
		this.recordReturn(this.configuration, this.configuration.getOfficeEnhancers(),
				new OfficeEnhancer[] { enhancer });
		this.recordReturn(this.configuration, this.configuration.getFlowNodeBuilder("MANAGED_OBJECT", "TASK"),
				flowNodeBuilder);
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.recordReturn(this.configuration, this.configuration.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this.recordReturn(this.linkedTeamConfiguration, this.linkedTeamConfiguration.getOfficeTeamName(), null);
		this.record_issue("Team registered to Office without name");
		this.recordReturn(this.configuration, this.configuration.getOfficeDefaultTeamName(), null);
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.recordReturn(this.configuration, this.configuration.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this.recordReturn(this.linkedTeamConfiguration, this.linkedTeamConfiguration.getOfficeTeamName(),
				"OFFICE_TEAM");
		this.recordReturn(this.linkedTeamConfiguration, this.linkedTeamConfiguration.getOfficeFloorTeamName(), "");
		this.record_issue("No Office Floor Team name for Office Team 'OFFICE_TEAM'");
		this.recordReturn(this.configuration, this.configuration.getOfficeDefaultTeamName(), null);
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.recordReturn(this.configuration, this.configuration.getRegisteredTeams(),
				new LinkedTeamConfiguration[] { this.linkedTeamConfiguration });
		this.recordReturn(this.linkedTeamConfiguration, this.linkedTeamConfiguration.getOfficeTeamName(),
				"OFFICE_TEAM");
		this.recordReturn(this.linkedTeamConfiguration, this.linkedTeamConfiguration.getOfficeFloorTeamName(),
				"OFFICE_FLOOR_TEAM");
		this.recordReturn(this.rawOfficeFloorMetaData,
				this.rawOfficeFloorMetaData.getRawTeamMetaData("OFFICE_FLOOR_TEAM"), null);
		this.record_issue("Unknown Team 'OFFICE_FLOOR_TEAM' not available to register to Office");
		this.recordReturn(this.configuration, this.configuration.getOfficeDefaultTeamName(), null);
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown default {@link Team}.
	 */
	public void testUnknownDefaultTeam() {

		// Record attempting to register unknown team
		this.record_enhanceOffice();
		this.recordReturn(this.configuration, this.configuration.getRegisteredTeams(),
				new LinkedTeamConfiguration[] {});
		this.recordReturn(this.configuration, this.configuration.getOfficeDefaultTeamName(), "UNKNOWN");
		this.record_issue("No default team UNKNOWN linked to Office");
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.record_defaultOfficeClock();
		this.record_governance();
		this.recordReturn(this.configuration, this.configuration.getRegisteredManagedObjectSources(),
				new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration, this.linkedMosConfiguration.getOfficeManagedObjectName(), null);
		this.record_issue("Managed Object registered to Office without name");
		this.record_boundInputManagedObjects();
		this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.record_defaultOfficeClock();
		this.record_governance();
		this.recordReturn(this.configuration, this.configuration.getRegisteredManagedObjectSources(),
				new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration, this.linkedMosConfiguration.getOfficeManagedObjectName(), "MO");
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration.getOfficeFloorManagedObjectSourceName(), null);
		this.record_issue("No Managed Object Source name for Office Managed Object 'MO'");
		this.record_boundInputManagedObjects();
		this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.record_defaultOfficeClock();
		this.record_governance();
		this.recordReturn(this.configuration, this.configuration.getRegisteredManagedObjectSources(),
				new LinkedManagedObjectSourceConfiguration[] { this.linkedMosConfiguration });
		this.recordReturn(this.linkedMosConfiguration, this.linkedMosConfiguration.getOfficeManagedObjectName(), "MO");
		this.recordReturn(this.linkedMosConfiguration,
				this.linkedMosConfiguration.getOfficeFloorManagedObjectSourceName(), "MOS");
		this.recordReturn(this.rawOfficeFloorMetaData, this.rawOfficeFloorMetaData.getRawManagedObjectMetaData("MOS"),
				null);
		this.record_issue("Unknown Managed Object Source 'MOS' not available to register to Office");
		this.record_boundInputManagedObjects();
		this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no Input {@link ManagedObject} name.
	 */
	public void testNoInputManagedObjectName() {

		BoundInputManagedObjectConfiguration boundInputConfiguration = this
				.createMock(BoundInputManagedObjectConfiguration.class);

		// Record no Input Managed Object Name
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_registerManagedObjectSources();
		this.recordReturn(this.configuration, this.configuration.getBoundInputManagedObjectConfiguration(),
				new BoundInputManagedObjectConfiguration[] { boundInputConfiguration });
		this.recordReturn(boundInputConfiguration, boundInputConfiguration.getInputManagedObjectName(), null);
		this.record_issue("No input Managed Object name for binding");
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no bound {@link ManagedObjectSource} name for Input
	 * {@link ManagedObject}.
	 */
	public void testNoBoundInputManagedObjectSourceName() {

		BoundInputManagedObjectConfiguration boundInputConfiguration = this
				.createMock(BoundInputManagedObjectConfiguration.class);

		// Record no bound Managed Object Source name
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_registerManagedObjectSources();
		this.recordReturn(this.configuration, this.configuration.getBoundInputManagedObjectConfiguration(),
				new BoundInputManagedObjectConfiguration[] { boundInputConfiguration });
		this.recordReturn(boundInputConfiguration, boundInputConfiguration.getInputManagedObjectName(), "INPUT");
		this.recordReturn(boundInputConfiguration, boundInputConfiguration.getBoundManagedObjectSourceName(), null);
		this.record_issue("No bound Managed Object Source name for input Managed Object 'INPUT'");
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no bound {@link ManagedObjectSource} name for Input
	 * {@link ManagedObject}.
	 */
	public void testInputManagedObjectNameRegisteredMoreThanOnce() {

		// Record input Managed Object name registered more than once
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_registerManagedObjectSources();
		this.record_boundInputManagedObjects("SAME_INPUT", "FIRST", "SAME_INPUT", "SECOND");
		this.record_issue("Input Managed Object 'SAME_INPUT' bound more than once");
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects);
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.record_defaultOfficeClock();
		this.record_governance();
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		this.record_boundInputManagedObjects();
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this
				.record_processBoundManagedObjects(registeredManagedObjectSources, "BOUND_MO");
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects, "BOUND_MO");
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able construct Input {@link ManagedObject}.
	 */
	public void testConstructInputManagedObject() {

		final RawManagingOfficeMetaData<?> inputManagedObject = this.createMock(RawManagingOfficeMetaData.class);
		this.officeManagingManagedObjects.add(inputManagedObject);

		// Record affixing a process managed object
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_registerManagedObjectSources();
		this.record_boundInputManagedObjects();
		final Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this
				.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_linkFunctions();
		this.record_constructManagedObjectMetaData(processManagedObjects, "OFFICE_MO_0");
		inputManagedObject.manageByOffice(null, null, null, this.issues);
		this.control(inputManagedObject).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				OfficeMetaData officeMetaData = (OfficeMetaData) actual[0];
				assertEquals("Incorrect office", RawOfficeMetaDataTest.OFFICE_NAME, officeMetaData.getOfficeName());
				RawBoundManagedObjectMetaData[] boundMetaData = (RawBoundManagedObjectMetaData[]) actual[1];
				assertEquals("Incorrect number of bound meta-data", 1, boundMetaData.length);
				RawBoundManagedObjectMetaData expectedBoundMetaData = processManagedObjects.get("OFFICE_MO_0");
				assertEquals("Incorrect bound meta-data", expectedBoundMetaData, boundMetaData[0]);
				assertEquals("Incorrect responsible teams", RawOfficeMetaDataTest.this.officeTeams, actual[2]);
				assertEquals("Incorrect issues", RawOfficeMetaDataTest.this.issues, actual[3]);
				return true;
			}
		});
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		this.record_boundInputManagedObjects();
		this.record_processBoundManagedObjects(registeredManagedObjectSources);
		Map<String, RawBoundManagedObjectMetaData> threadManagedObjects = this
				.record_threadBoundManagedObjects(registeredManagedObjectSources, "BOUND_MO");
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(threadManagedObjects, "BOUND_MO");
		this.record_processContextListeners();
		this.record_noProfiler();

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
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		this.record_boundInputManagedObjects();
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this
				.record_processBoundManagedObjects(registeredManagedObjectSources, "PROCESS");
		Map<String, RawBoundManagedObjectMetaData> threadManagedObjects = this
				.record_threadBoundManagedObjects(registeredManagedObjectSources, "THREAD");
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(threadManagedObjects, "THREAD");
		this.record_constructManagedObjectMetaData(processManagedObjects, "PROCESS");
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Handles failure to construct {@link Governance}.
	 */
	public void testFailConstructGovernance() {

		final GovernanceConfiguration<?, ?> governanceConfiguration = this.createMock(GovernanceConfiguration.class);

		// Record creating a thread bound managed object
		this.record_enhanceOffice();
		Map<String, TeamManagement> teams = this.record_teams();
		this.record_defaultOfficeClock();

		// Record management of governance
		this.recordReturn(this.configuration, this.configuration.isManuallyManageGovernance(), false);

		// Record not creating governance meta-data
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new GovernanceConfiguration[] { governanceConfiguration });
		this.recordReturn(this.rawGovernanceFactory, this.rawGovernanceFactory
				.createRawGovernanceMetaData(governanceConfiguration, 0, teams, OFFICE_NAME, this.issues), null);
		this.recordReturn(governanceConfiguration, governanceConfiguration.getGovernanceName(), "GOVERNANCE");
		this.record_issue("Unable to configure governance 'GOVERNANCE'");

		// Configure remaining aspects
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		this.record_boundInputManagedObjects();
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this
				.record_processBoundManagedObjects(registeredManagedObjectSources);
		this.record_threadBoundManagedObjects(registeredManagedObjectSources);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_constructManagedObjectMetaData(processManagedObjects);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure the correct governance meta-data
		Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaDatas = rawOfficeMetaData.getGovernanceMetaData();
		assertEquals("Should be no governance", 0, rawGovernanceMetaDatas.size());

		// Ensure correct listing of governance meta-data
		ProcessMetaData processMetaData = rawOfficeMetaData.getOfficeMetaData().getProcessMetaData();
		GovernanceMetaData<?, ?>[] governanceMetaDatas = processMetaData.getThreadMetaData().getGovernanceMetaData();
		assertEquals("Incorrect number of governances", 1, governanceMetaDatas.length);
		assertNull("Should be no governance", governanceMetaDatas[0]);
	}

	/**
	 * Constructs with manual management of {@link Governance}.
	 */
	public void testManualManagementOfGovernance() {

		// Record manual management of governance
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance(true);
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources();
		this.record_boundInputManagedObjects();
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this
				.record_processBoundManagedObjects(registeredManagedObjectSources);
		this.record_threadBoundManagedObjects(registeredManagedObjectSources);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_governanceTasks();
		this.record_constructManagedObjectMetaData(processManagedObjects);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure correctly flag for manual management of governance
		assertTrue("Should be manually managing governance", rawOfficeMetaData.isManuallyManageGovernance());
	}

	/**
	 * Constructs the {@link GovernanceMetaData}.
	 */
	public void testConstructGovernance() {

		// Record creating a thread bound managed object
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		GovernanceMetaData<?, ?>[] expectedGovernances = this.record_governance("GOVERNANCE_ONE", "GOVERNANCE_TWO");
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this
				.record_registerManagedObjectSources("MO");
		this.record_boundInputManagedObjects();
		Map<String, RawBoundManagedObjectMetaData> processManagedObjects = this
				.record_processBoundManagedObjects(registeredManagedObjectSources);
		this.record_threadBoundManagedObjects(registeredManagedObjectSources);
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_governanceTasks("GOVERNANCE_ONE", "GOVERNANCE_TWO");
		this.record_constructManagedObjectMetaData(processManagedObjects);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure correctly flag for manual management of governance
		assertFalse("Should not be manually managing governance", rawOfficeMetaData.isManuallyManageGovernance());

		// Ensure the correct governance meta-data
		Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaDatas = rawOfficeMetaData.getGovernanceMetaData();
		assertNotNull("Ensure have first governance", rawGovernanceMetaDatas.get("GOVERNANCE_ONE"));
		assertNotNull("Ensure have second governance", rawGovernanceMetaDatas.get("GOVERNANCE_TWO"));

		// Obtain the thread meta-data
		ThreadMetaData threadMetaData = rawOfficeMetaData.getOfficeMetaData().getProcessMetaData().getThreadMetaData();

		// Ensure correct listing of governance meta-data
		GovernanceMetaData<?, ?>[] governanceMetaDatas = threadMetaData.getGovernanceMetaData();
		assertEquals("Incorrect number of governances", expectedGovernances.length, governanceMetaDatas.length);
		for (int i = 0; i < expectedGovernances.length; i++) {
			assertEquals("Incorrect governance meta-data for " + i, expectedGovernances[i], governanceMetaDatas[i]);
		}
	}

	/**
	 * Enable able to handle not constructing {@link ManagedFunction}.
	 */
	public void testNotConstructFunction() {

		// Record constructing function
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions("TEST", (RawManagedFunctionMetaData<?, ?>) null);
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Enable able to construct {@link ManagedFunction}.
	 */
	public void testConstructFunction() {

		final RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = this.createMock(RawManagedFunctionMetaData.class);

		// Record constructing work
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		ManagedFunctionMetaData<?, ?> functionMetaData = this.record_functions("TASK", rawFunctionMetaData)[0];
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_linkFunctions(rawFunctionMetaData);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify the office meta-data
		assertEquals("Incorrect office name", "OFFICE", officeMetaData.getOfficeName());
		ManagedFunctionMetaData<?, ?>[] returnedFunctionMetaData = officeMetaData.getManagedFunctionMetaData();
		assertEquals("Incorrect number of functions", 1, returnedFunctionMetaData.length);
		assertEquals("Incorrect function", functionMetaData, returnedFunctionMetaData[0]);
	}

	/**
	 * Enable issue if {@link OfficeStartupFunction} not found.
	 */
	public void testUnknownStartupFunction() {

		final RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = this.createMock(RawManagedFunctionMetaData.class);
		final ManagedFunctionReference startupTaskReference = this.createMock(ManagedFunctionReference.class);

		// Record adding startup task
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions("STARTUP", rawFunctionMetaData);
		this.recordReturn(this.configuration, this.configuration.getStartupFunctions(),
				new ManagedFunctionReference[] { startupTaskReference });
		this.record_noOfficeEscalationHandler();
		this.recordReturn(startupTaskReference, startupTaskReference.getFunctionName(), null);
		this.record_issue("No function name provided for Startup Function 0");
		this.record_linkFunctions(rawFunctionMetaData);
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify startup task not loaded
		OfficeStartupFunction[] startupTasks = officeMetaData.getStartupFunctions();
		assertEquals("Incorrect number of startup task entries", 1, startupTasks.length);
		assertNull("Should not have startup task loaded", startupTasks[0]);
	}

	/**
	 * Enable able to link in an {@link OfficeStartupFunction}.
	 */
	public void testConstructStartupFunction() {

		final RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = this.createMock(RawManagedFunctionMetaData.class);
		final ManagedFunctionReference startupTaskReference = this.createMock(ManagedFunctionReference.class);

		// Record adding startup task
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		ManagedFunctionMetaData<?, ?> taskMetaData = this.record_functions("TASK", rawFunctionMetaData)[0];
		this.recordReturn(this.configuration, this.configuration.getStartupFunctions(),
				new ManagedFunctionReference[] { startupTaskReference });
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();
		this.recordReturn(startupTaskReference, startupTaskReference.getFunctionName(), "TASK");
		this.recordReturn(startupTaskReference, startupTaskReference.getArgumentType(), null);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), null);
		this.record_linkFunctions(rawFunctionMetaData);

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify startup task loaded
		OfficeStartupFunction[] startupTasks = officeMetaData.getStartupFunctions();
		assertEquals("Incorrect number of startup tasks", 1, startupTasks.length);
		assertEquals("Incorrect startup task meta-data", taskMetaData,
				startupTasks[0].getFlowMetaData().getInitialFunctionMetaData());
	}

	/**
	 * Enable able to provide {@link Profiler}.
	 */
	public void testProvideProfiler() {

		final Profiler profiler = this.createMock(Profiler.class);

		// Record providing profiler
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions("TASK", (RawManagedFunctionMetaData<?, ?>) null);
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.recordReturn(this.configuration, this.configuration.getProfiler(), profiler);

		// Test
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// TODO verify profiler
	}

	/**
	 * Ensure issue if no type of cause for {@link Office}
	 * {@link EscalationFlow}.
	 */
	public void testNoTypeOfCauseForOfficeEscalation() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);

		// Record no type of cause
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.recordReturn(this.configuration, this.configuration.getEscalationConfiguration(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(this.rawOfficeFloorMetaData, this.rawOfficeFloorMetaData.getOfficeFloorEscalation(),
				this.officeFloorEscalation);
		this.record_processContextListeners();
		this.record_noProfiler();
		this.recordReturn(escalationConfiguration, escalationConfiguration.getTypeOfCause(), null);
		this.record_issue("Type of cause not provided for office escalation 0");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedFunction} for {@link Office}
	 * {@link EscalationFlow}.
	 */
	public void testUnknownFunctionForOfficeEscalation() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);
		final ManagedFunctionReference taskReference = this.createMock(ManagedFunctionReference.class);

		// Record unknown escalation task
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.recordReturn(this.configuration, this.configuration.getEscalationConfiguration(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(this.rawOfficeFloorMetaData, this.rawOfficeFloorMetaData.getOfficeFloorEscalation(),
				this.officeFloorEscalation);
		this.recordReturn(escalationConfiguration, escalationConfiguration.getTypeOfCause(), RuntimeException.class);
		this.recordReturn(escalationConfiguration, escalationConfiguration.getManagedFunctionReference(),
				taskReference);
		this.recordReturn(taskReference, taskReference.getFunctionName(), "TASK");
		this.record_issue("Can not find function meta-data TASK for Office Escalation 0");
		this.record_processContextListeners();
		this.record_noProfiler();

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link in an {@link Office} {@link EscalationFlow}.
	 */
	public void testConstructOfficeEscalation() {

		final RuntimeException failure = new RuntimeException("Escalation");

		final RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = this.createMock(RawManagedFunctionMetaData.class);
		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);
		final ManagedFunctionReference escalationTaskReference = this.createMock(ManagedFunctionReference.class);
		final Class<?> typeOfCause = failure.getClass();

		// Record adding office escalation
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		ManagedFunctionMetaData<?, ?> functionMetaData = this.record_functions("TASK", rawFunctionMetaData)[0];
		this.record_noOfficeStartupFunctions();
		this.recordReturn(this.configuration, this.configuration.getEscalationConfiguration(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(this.rawOfficeFloorMetaData, this.rawOfficeFloorMetaData.getOfficeFloorEscalation(),
				this.officeFloorEscalation);
		this.record_processContextListeners();
		this.record_noProfiler();
		this.recordReturn(escalationConfiguration, escalationConfiguration.getTypeOfCause(), typeOfCause);
		this.recordReturn(escalationConfiguration, escalationConfiguration.getManagedFunctionReference(),
				escalationTaskReference);
		this.recordReturn(escalationTaskReference, escalationTaskReference.getFunctionName(), "TASK");
		this.recordReturn(escalationTaskReference, escalationTaskReference.getArgumentType(), failure.getClass());
		this.recordReturn(functionMetaData, functionMetaData.getParameterType(), Exception.class);
		this.record_linkFunctions(rawFunctionMetaData);

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify office escalation loaded
		EscalationProcedure escalationProcedure = officeMetaData.getProcessMetaData().getThreadMetaData()
				.getOfficeEscalationProcedure();
		EscalationFlow escalation = escalationProcedure.getEscalation(failure);
		assertEquals("Incorrect escalation task meta-data", functionMetaData, escalation.getManagedFunctionMetaData());
	}

	/**
	 * Ensure able to create a {@link ProcessState} and obtain the
	 * {@link OfficeFloor} {@link EscalationFlow}
	 */
	public void testCreateProcessAndOfficeFloorEscalation() {

		final FlowMetaData flowMetaData = this.createMock(FlowMetaData.class);
		final ManagedFunction<?, ?> task = this.createMock(ManagedFunction.class);
		final ManagedFunctionMetaData<?, ?> taskMetaData = MetaDataTestInstanceFactory.createFunctionMetaData(task);

		// Record creating a process
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners();
		this.record_noProfiler();
		this.recordReturn(flowMetaData, flowMetaData.getInitialFunctionMetaData(), taskMetaData);

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		FunctionState function = officeMetaData.createProcess(flowMetaData, null, null, null);
		this.verifyMockObjects();

		// Verify the OfficeFloor escalation
		assertNotNull("Should create process", function);
		assertEquals("Incorrect office floor escalation", this.officeFloorEscalation,
				officeMetaData.getProcessMetaData().getThreadMetaData().getOfficeFloorEscalation());
	}

	/**
	 * Enable able to include {@link ProcessContextListener} instances.
	 */
	public void testRegisteredProcessContextListener() {

		final ProcessContextListener listener = this.createMock(ProcessContextListener.class);
		final FlowMetaData flowMetaData = this.createMock(FlowMetaData.class);
		final ManagedFunction<?, ?> task = this.createMock(ManagedFunction.class);
		final ManagedFunctionMetaData<?, ?> taskMetaData = MetaDataTestInstanceFactory.createFunctionMetaData(task);

		// Record registering Process Context Listener
		this.record_enhanceOffice();
		this.record_teams();
		this.record_defaultOfficeClock();
		this.record_governance();
		this.record_noManagedObjectsAndAdministrators();
		this.record_functions();
		this.record_noOfficeStartupFunctions();
		this.record_noOfficeEscalationHandler();
		this.record_processContextListeners(listener);
		this.record_noProfiler();

		// Record creating Process to validate Process Context Listener
		this.recordReturn(flowMetaData, flowMetaData.getInitialFunctionMetaData(), taskMetaData);

		// Obtain Process Identifier
		final Object[] processIdentifier = new Object[1];
		listener.processCreated(null);
		this.control(listener).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				processIdentifier[0] = actual[0];
				return true;
			}
		});

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData metaData = this.constructRawOfficeMetaData(true);

		// Verify registered Process Context Listener by creating Process
		FunctionState function = metaData.getOfficeMetaData().createProcess(flowMetaData, null, null, null);

		// Verify functionality
		this.verifyMockObjects();

		// Obtain the process state
		ProcessState processState = function.getThreadState().getProcessState();

		// Verify process identifiers
		assertNotNull("Must have Process Identifier", processIdentifier[0]);
		assertEquals("Incorrect Process Identifier", processState.getProcessIdentifier(), processIdentifier[0]);
	}

	/**
	 * Records obtaining the {@link Office} name and providing no
	 * {@link OfficeEnhancer} instances.
	 */
	private void record_enhanceOffice() {
		this.recordReturn(this.configuration, this.configuration.getOfficeName(), OFFICE_NAME);
		this.recordReturn(this.configuration, this.configuration.getMonitorOfficeInterval(), 1000);
		this.recordReturn(this.configuration, this.configuration.getOfficeEnhancers(), new OfficeEnhancer[0]);
	}

	/**
	 * Records no {@link Team} instances.
	 * 
	 * @param teamNames
	 *            Names of the {@link Team} instances bound to the
	 *            {@link Office}.
	 * @return Mapping of {@link TeamManagement} instances by their
	 *         {@link Office} bound names.
	 */
	private Map<String, TeamManagement> record_teams(String... teamNames) {

		// Create configuration for each team name
		LinkedTeamConfiguration[] teamConfigurations = new LinkedTeamConfiguration[teamNames.length];
		for (int i = 0; i < teamConfigurations.length; i++) {
			teamConfigurations[i] = this.createMock(LinkedTeamConfiguration.class);
		}

		// Record registering the teams
		this.officeTeams = new HashMap<String, TeamManagement>();
		this.recordReturn(this.configuration, this.configuration.getRegisteredTeams(), teamConfigurations);
		for (int i = 0; i < teamNames.length; i++) {
			LinkedTeamConfiguration teamConfiguration = teamConfigurations[i];
			String teamName = teamNames[i];
			String officeFloorTeamName = teamName + "-officefloor";
			RawTeamMetaData rawTeam = this.createMock(RawTeamMetaData.class);
			TeamManagement team = this.createMock(TeamManagement.class);

			// Record registering the team
			this.recordReturn(teamConfiguration, teamConfiguration.getOfficeTeamName(), teamName);
			this.recordReturn(teamConfiguration, teamConfiguration.getOfficeFloorTeamName(), officeFloorTeamName);
			this.recordReturn(this.rawOfficeFloorMetaData,
					this.rawOfficeFloorMetaData.getRawTeamMetaData(officeFloorTeamName), rawTeam);
			this.recordReturn(rawTeam, rawTeam.getTeamManagement(), team);

			// Register the team for return
			this.officeTeams.put(teamName, team);
		}

		// Record the no default team
		this.recordReturn(this.configuration, this.configuration.getOfficeDefaultTeamName(), null);

		// Return the registry of the teams
		return this.officeTeams;
	}

	/**
	 * Record using default {@link OfficeClock}.
	 */
	private void record_defaultOfficeClock() {
		this.recordReturn(this.configuration, this.configuration.getOfficeClock(), null);
	}

	/**
	 * Records creating the {@link Governance} for the {@link Office}.
	 */
	private GovernanceMetaData<?, ?>[] record_governance(String... governanceNames) {
		return this.record_governance(false, governanceNames);
	}

	/**
	 * Records creating the {@link Governance} for the {@link Office}.
	 */
	private GovernanceMetaData<?, ?>[] record_governance(boolean isManuallyManageGovernance,
			String... governanceNames) {

		// Create the listing of governance configuration
		GovernanceConfiguration<?, ?>[] governanceConfigurations = new GovernanceConfiguration[governanceNames.length];
		for (int i = 0; i < governanceConfigurations.length; i++) {
			governanceConfigurations[i] = this.createMock(GovernanceConfiguration.class);
		}

		// Record whether manually managed
		this.recordReturn(this.configuration, this.configuration.isManuallyManageGovernance(),
				isManuallyManageGovernance);

		// Record creating the governance meta-data
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				governanceConfigurations);
		GovernanceMetaData<?, ?>[] governanceMetaDatas = new GovernanceMetaData<?, ?>[governanceConfigurations.length];
		for (int i = 0; i < governanceConfigurations.length; i++) {
			GovernanceConfiguration<?, ?> governanceConfiguration = governanceConfigurations[i];
			String governanceName = governanceNames[i];

			final RawGovernanceMetaData<?, ?> rawGovernanceMetaData = this.createMock(RawGovernanceMetaData.class);
			final GovernanceMetaData<?, ?> governanceMetaData = this.createMock(GovernanceMetaData.class);
			governanceMetaDatas[i] = governanceMetaData;

			// Register the raw governance meta-data
			this.rawGovernanceMetaDatas.put(governanceName, rawGovernanceMetaData);

			// Record creating the governance meta-data
			this.recordReturn(this.rawGovernanceFactory, this.rawGovernanceFactory.createRawGovernanceMetaData(
					governanceConfiguration, i, this.officeTeams, OFFICE_NAME, this.issues), rawGovernanceMetaData);
			this.recordReturn(rawGovernanceMetaData, rawGovernanceMetaData.getGovernanceName(), governanceName);
			this.recordReturn(rawGovernanceMetaData, rawGovernanceMetaData.getGovernanceMetaData(), governanceMetaData);
		}

		// Return the governance meta-data
		return governanceMetaDatas;
	}

	/**
	 * Records the {@link Governance} {@link ManagedFunction} instances.
	 */
	private void record_governanceTasks(String... governanceNames) {

		// Record governance tasks
		for (int i = 0; i < governanceNames.length; i++) {
			String governanceName = governanceNames[i];

			// Ensure have raw governance meta-data
			RawGovernanceMetaData<?, ?> rawGovernanceMetaData = this.rawGovernanceMetaDatas.get(governanceName);
			assertNotNull("Missing raw Governance meta-data", rawGovernanceMetaData);

			// Link the Office meta-data
			rawGovernanceMetaData.loadOfficeMetaData(null, this.issues);
			this.control(rawGovernanceMetaData)
					.setMatcher(new TypeMatcher(OfficeMetaData.class, OfficeFloorIssues.class));
		}
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
			moConfigurations[i] = this.createMock(LinkedManagedObjectSourceConfiguration.class);
		}

		// Record registering the managed object sources and collect for return
		Map<String, RawManagedObjectMetaData<?, ?>> rawMoMetaDatas = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		this.recordReturn(this.configuration, this.configuration.getRegisteredManagedObjectSources(), moConfigurations);
		for (int i = 0; i < managedObjectNames.length; i++) {
			LinkedManagedObjectSourceConfiguration moConfiguration = moConfigurations[i];
			String managedObjectName = managedObjectNames[i];
			String managedObjectSourceName = managedObjectName + "-source";
			RawManagedObjectMetaData<?, ?> rawMoMetaData = this.createMock(RawManagedObjectMetaData.class);

			// Record registering the managed object
			this.recordReturn(moConfiguration, moConfiguration.getOfficeManagedObjectName(), managedObjectName);
			this.recordReturn(moConfiguration, moConfiguration.getOfficeFloorManagedObjectSourceName(),
					managedObjectSourceName);
			this.recordReturn(this.rawOfficeFloorMetaData,
					this.rawOfficeFloorMetaData.getRawManagedObjectMetaData(managedObjectSourceName), rawMoMetaData);

			// Load the managed object for return
			rawMoMetaDatas.put(managedObjectName, rawMoMetaData);
		}

		// Return the registered managed object meta-data
		return rawMoMetaDatas;
	}

	/**
	 * Records obtaining {@link BoundInputManagedObjectConfiguration}.
	 * 
	 * @param inputSourceNamePairs
	 *            Input {@link ManagedObject} to {@link ManagedObjectSource}
	 *            name pairs.
	 */
	private void record_boundInputManagedObjects(String... inputSourceNamePairs) {

		// Create the mock configurations
		BoundInputManagedObjectConfiguration[] configurations = new BoundInputManagedObjectConfiguration[inputSourceNamePairs.length
				/ 2];
		for (int i = 0; i < configurations.length; i++) {
			configurations[i] = this.createMock(BoundInputManagedObjectConfiguration.class);
		}

		// Record binding configuration of input managed objects
		this.recordReturn(this.configuration, this.configuration.getBoundInputManagedObjectConfiguration(),
				configurations);
		for (int i = 0; i < inputSourceNamePairs.length; i += 2) {
			String inputManagedObjectName = inputSourceNamePairs[i];
			String managedObjectSourceName = inputSourceNamePairs[i + 1];
			if (!this.boundInputManagedObjects.containsKey(inputManagedObjectName)) {
				this.boundInputManagedObjects.put(inputManagedObjectName, managedObjectSourceName);
			}
			BoundInputManagedObjectConfiguration configuration = configurations[i / 2];
			this.recordReturn(configuration, configuration.getInputManagedObjectName(), inputManagedObjectName);
			this.recordReturn(configuration, configuration.getBoundManagedObjectSourceName(), managedObjectSourceName);
		}
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
	private Map<String, RawBoundManagedObjectMetaData> record_processBoundManagedObjects(
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources, String... processBoundNames) {

		final String OFFICE_MANAGING_PREFIX = "OFFICE_MO_";

		// Create the mock objects to register the process bound managed objects
		final RawManagingOfficeMetaData<?>[] officeManagedObjects = this.officeManagingManagedObjects
				.toArray(new RawManagingOfficeMetaData[0]);
		final RawBoundManagedObjectMetaData[] officeBoundMoMetaDatas = new RawBoundManagedObjectMetaData[officeManagedObjects.length];
		final ManagedObjectConfiguration<?>[] moConfigurations = new ManagedObjectConfiguration[processBoundNames.length];
		final RawBoundManagedObjectMetaData[] rawBoundMoMetaDatas = new RawBoundManagedObjectMetaData[processBoundNames.length
				+ officeManagedObjects.length];
		final RawBoundManagedObjectMetaData[] processBoundMoMetaDatas = new RawBoundManagedObjectMetaData[rawBoundMoMetaDatas.length
				+ officeBoundMoMetaDatas.length];
		final Map<String, RawBoundManagedObjectMetaData> boundManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData>();
		for (int i = 0; i < processBoundNames.length; i++) {
			moConfigurations[i] = this.createMock(ManagedObjectConfiguration.class);
			processBoundMoMetaDatas[i] = this.createMock(RawBoundManagedObjectMetaData.class);
			rawBoundMoMetaDatas[i] = processBoundMoMetaDatas[i];
			boundManagedObjects.put(processBoundNames[i], rawBoundMoMetaDatas[i]);
		}
		for (int i = 0; i < officeBoundMoMetaDatas.length; i++) {
			officeBoundMoMetaDatas[i] = this.createMock(RawBoundManagedObjectMetaData.class);
			rawBoundMoMetaDatas[processBoundNames.length + i] = officeBoundMoMetaDatas[i];
			boundManagedObjects.put(OFFICE_MANAGING_PREFIX + i, rawBoundMoMetaDatas[i]);
		}

		// Provide default empty map if null registered
		if (registeredManagedObjectSources == null) {
			registeredManagedObjectSources = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		}

		// Record constructing process bound managed objects
		this.recordReturn(this.configuration, this.configuration.getProcessManagedObjectConfiguration(),
				moConfigurations);
		this.recordReturn(this.rawBoundManagedObjectFactory,
				this.rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(moConfigurations, this.issues,
						ManagedObjectScope.PROCESS, AssetType.OFFICE, OFFICE_NAME, null, registeredManagedObjectSources,
						null, officeManagedObjects, this.boundInputManagedObjects, null),
				rawBoundMoMetaDatas);
		this.constructBoundObjectsMatcher.addMatch(moConfigurations, ManagedObjectScope.PROCESS,
				registeredManagedObjectSources, null, officeManagedObjects);

		// Record creating map of the process bound managed objects
		for (int i = 0; i < processBoundNames.length; i++) {
			this.recordReturn(rawBoundMoMetaDatas[i], rawBoundMoMetaDatas[i].getBoundManagedObjectName(),
					processBoundNames[i]);
		}
		for (int i = 0; i < officeBoundMoMetaDatas.length; i++) {
			this.recordReturn(officeBoundMoMetaDatas[i], officeBoundMoMetaDatas[i].getBoundManagedObjectName(),
					OFFICE_MANAGING_PREFIX + i);
		}

		// Capture process bound managed objects for thread managed objects
		this.processManagedObjects = boundManagedObjects;

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
	 *            {@link ProcessState} bound {@link RawManagedObjectMetaData}.
	 * @param threadBoundNames
	 *            Names of the {@link ThreadState} bound names.
	 * @return Mapping of {@link RawBoundManagedObjectMetaData} by its bound
	 *         name.
	 */
	private Map<String, RawBoundManagedObjectMetaData> record_threadBoundManagedObjects(
			final Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources,
			String... threadBoundNames) {

		// Create the mock objects to register the thread bound managed objects
		final ManagedObjectConfiguration<?>[] moConfigurations = new ManagedObjectConfiguration[threadBoundNames.length];
		final RawBoundManagedObjectMetaData[] rawBoundMoMetaDatas = new RawBoundManagedObjectMetaData[threadBoundNames.length];
		final Map<String, RawBoundManagedObjectMetaData> boundManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData>();
		for (int i = 0; i < threadBoundNames.length; i++) {
			moConfigurations[i] = this.createMock(ManagedObjectConfiguration.class);
			rawBoundMoMetaDatas[i] = this.createMock(RawBoundManagedObjectMetaData.class);
			boundManagedObjects.put(threadBoundNames[i], rawBoundMoMetaDatas[i]);
		}

		// Record constructing thread bound managed objects
		this.recordReturn(this.configuration, this.configuration.getThreadManagedObjectConfiguration(),
				moConfigurations);
		if (threadBoundNames.length > 0) {
			this.recordReturn(this.rawBoundManagedObjectFactory,
					this.rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(moConfigurations, this.issues,
							ManagedObjectScope.THREAD, AssetType.OFFICE, OFFICE_NAME, null,
							registeredManagedObjectSources, this.processManagedObjects, null, null, null),
					rawBoundMoMetaDatas);
			this.constructBoundObjectsMatcher.addMatch(moConfigurations, ManagedObjectScope.THREAD,
					registeredManagedObjectSources, this.processManagedObjects, null);
		}
		for (int i = 0; i < threadBoundNames.length; i++) {
			this.recordReturn(rawBoundMoMetaDatas[i], rawBoundMoMetaDatas[i].getBoundManagedObjectName(),
					threadBoundNames[i]);
		}

		// Return the bound managed objects
		return boundManagedObjects;
	}

	/**
	 * Records no {@link ManagedObject} and {@link Administration} instances.
	 */
	private void record_noManagedObjectsAndAdministrators() {
		this.record_registerManagedObjectSources();
		this.record_boundInputManagedObjects();
		this.record_processBoundManagedObjects(null);
		this.record_threadBoundManagedObjects(null);
	}

	/**
	 * Records constructing the default {@link ManagedObjectMetaData} instances.
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
			Map<String, RawBoundManagedObjectMetaData> boundManagedObjects, String... constructManagedObjectNames) {
		ManagedObjectMetaData<?>[] moMetaDatas = new ManagedObjectMetaData[constructManagedObjectNames.length];
		for (int i = 0; i < constructManagedObjectNames.length; i++) {
			moMetaDatas[i] = this.createMock(ManagedObjectMetaData.class);
			RawBoundManagedObjectMetaData rawBoundMo = boundManagedObjects.get(constructManagedObjectNames[i]);

			// Record constructing default managed object meta-data
			this.recordReturn(rawBoundMo, rawBoundMo.getDefaultInstanceIndex(), 0);
			RawBoundManagedObjectInstanceMetaData<?> rawBoundMoInstance = this
					.createMock(RawBoundManagedObjectInstanceMetaData.class);
			this.recordReturn(rawBoundMo, rawBoundMo.getRawBoundManagedObjectInstanceMetaData(),
					new RawBoundManagedObjectInstanceMetaData[] { rawBoundMoInstance });
			this.recordReturn(rawBoundMoInstance, rawBoundMoInstance.getManagedObjectMetaData(), moMetaDatas[i]);
		}
		return moMetaDatas;
	}

	/**
	 * Records obtaining the {@link ProcessContextListener} instances.
	 * 
	 * @param listeners
	 *            {@link ProcessContextListener} instances.
	 */
	private void record_processContextListeners(ProcessContextListener... listeners) {
		this.recordReturn(this.rawOfficeFloorMetaData, this.rawOfficeFloorMetaData.getProcessContextListeners(),
				listeners);
	}

	/**
	 * Records no {@link Profiler}.
	 */
	private void record_noProfiler() {
		this.recordReturn(this.configuration, this.configuration.getProfiler(), null);
	}

	/**
	 * Records construction of {@link ManagedFunctionMetaData} instances.
	 * 
	 * @param nameToRawFunctionMetaDatas
	 *            Name and {@link RawManagedFunctionMetaData} instance pairings.
	 *            Provide <code>null</code> for a value should not construct
	 *            that {@link ManagedFunction}.
	 */
	private ManagedFunctionMetaData<?, ?>[] record_functions(Object... nameToRawFunctionMetaDatas) {

		// Create the managed function configurations
		ManagedFunctionConfiguration<?, ?>[] functionConfigurations = new ManagedFunctionConfiguration[nameToRawFunctionMetaDatas.length
				/ 2];
		for (int i = 0; i < functionConfigurations.length; i++) {
			functionConfigurations[i] = this.createMock(ManagedFunctionConfiguration.class);
		}

		// Record constructing the function instances
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionConfiguration(),
				functionConfigurations);
		List<ManagedFunctionMetaData<?, ?>> functionMetaDatas = new LinkedList<ManagedFunctionMetaData<?, ?>>();
		List<String> functionNames = new LinkedList<>();
		for (int i = 0; i < nameToRawFunctionMetaDatas.length; i += 2) {
			final ManagedFunctionConfiguration<?, ?> functionConfiguration = functionConfigurations[i / 2];
			String functionName = (String) nameToRawFunctionMetaDatas[i];
			RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = (RawManagedFunctionMetaData<?, ?>) nameToRawFunctionMetaDatas[i
					+ 1];

			// Record constructing the raw managed function
			this.recordReturn(this.rawTaskMetaDataFactory,
					this.rawTaskMetaDataFactory.constructRawManagedFunctionMetaData(functionConfiguration, null, null,
							this.rawBoundManagedObjectFactory, this.issues),
					rawFunctionMetaData, new AbstractMatcher() {
						@Override
						public boolean matches(Object[] e, Object[] a) {
							assertEquals("Incorrect function configuration", functionConfiguration, a[0]);
							assertTrue("Must have raw office meta-data", a[1] instanceof RawOfficeMetaData);
							assertTrue("Should be an asset manager factory", a[2] instanceof AssetManagerFactory);
							assertEquals("Incorrect bound managed object factory",
									RawOfficeMetaDataTest.this.rawBoundManagedObjectFactory, a[3]);
							assertEquals("Incorrect issues", RawOfficeMetaDataTest.this.issues, a[4]);
							return true;
						}
					});

			// Determine if construct function
			if (rawFunctionMetaData != null) {
				// Record constructing function and have registered for return
				ManagedFunctionMetaData<?, ?> functionMetaData = this.createMock(ManagedFunctionMetaData.class);
				this.recordReturn(rawFunctionMetaData, rawFunctionMetaData.getManagedFunctionMetaData(),
						functionMetaData);
				functionMetaDatas.add(functionMetaData);
				functionNames.add(functionName);
			}
		}

		// Record creating the function locator
		for (int i = 0; i < functionMetaDatas.size(); i++) {
			ManagedFunctionMetaData<?, ?> functionMetaData = functionMetaDatas.get(i);
			String functionName = functionNames.get(i);
			this.recordReturn(functionMetaData, functionMetaData.getFunctionName(), functionName);
		}

		// Return the function meta-data
		return functionMetaDatas.toArray(new ManagedFunctionMetaData[0]);
	}

	/**
	 * Links the {@link ManagedFunction} instances.
	 * 
	 * @param rawFunctionMetaDatas
	 *            {@link RawManagedFunctionMetaData} instances.
	 */
	private void record_linkFunctions(RawManagedFunctionMetaData<?, ?>... rawFunctionMetaDatas) {
		for (int i = 0; i < rawFunctionMetaDatas.length; i++) {
			rawFunctionMetaDatas[i].loadOfficeMetaData(null, null, this.officeTeams, this.issues);
			this.control(rawFunctionMetaDatas[i]).setMatcher(new TypeMatcher(OfficeMetaData.class,
					RawAdministrationMetaDataFactory.class, Map.class, OfficeFloorIssues.class));
		}
	}

	/**
	 * Records no {@link OfficeStartupFunction} instances.
	 */
	private void record_noOfficeStartupFunctions() {
		this.recordReturn(this.configuration, this.configuration.getStartupFunctions(), null);
	}

	/**
	 * Records no {@link Office} {@link EscalationHandler}.
	 */
	private void record_noOfficeEscalationHandler() {
		this.recordReturn(this.configuration, this.configuration.getEscalationConfiguration(),
				new ManagedFunctionEscalationConfiguration[0]);
		this.recordReturn(this.rawOfficeFloorMetaData, this.rawOfficeFloorMetaData.getOfficeFloorEscalation(),
				this.officeFloorEscalation);
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
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, issueDescription, cause);
	}

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            Flag indicating if should be constructed.
	 * @return Constructed {@link RawOfficeMetaData}.
	 */
	private RawOfficeMetaData constructRawOfficeMetaData(boolean isExpectConstruct) {

		// Obtain the office managing managed objects
		RawManagingOfficeMetaData<?>[] officeMos = this.officeManagingManagedObjects
				.toArray(new RawManagingOfficeMetaData[0]);

		// Construct the meta-data
		RawOfficeMetaData metaData = RawOfficeMetaDataImpl.getFactory().constructRawOfficeMetaData(this.configuration,
				this.issues, officeMos, this.rawOfficeFloorMetaData, this.rawBoundManagedObjectFactory,
				this.rawGovernanceFactory, this.rawBoundAdministratorFactory, this.rawTaskMetaDataFactory);
		if (isExpectConstruct) {
			assertNotNull("Meta-data should be constructed", metaData);
		} else {
			assertNull("Meta-data should NOT be constructed", metaData);
		}

		// Return the meta-data
		return metaData;
	}

	/**
	 * {@link ArgumentsMatcher} for constructing the
	 * {@link RawBoundManagedObjectMetaData} instances.
	 */
	private class ConstructBoundManagedObjectsMatcher extends AbstractMatcher {

		/**
		 * Flag indicating if this has been set as matcher.
		 */
		private boolean isMatcherSet = false;

		/**
		 * Listing of expected {@link ManagedObjectConfiguration} array matches.
		 */
		private List<ManagedObjectConfiguration<?>[]> moConfigurationsList = new LinkedList<ManagedObjectConfiguration<?>[]>();

		/**
		 * Listing of expected {@link ManagedObjectScope} matches.
		 */
		private List<ManagedObjectScope> managedObjectScopeList = new LinkedList<ManagedObjectScope>();

		/**
		 * Listing of expected {@link RawManagedObjectMetaData} matches.
		 */
		private List<Map<String, RawManagedObjectMetaData<?, ?>>> registeredManagedObjectSourcesList = new LinkedList<Map<String, RawManagedObjectMetaData<?, ?>>>();

		/**
		 * Listing of expected {@link RawBoundManagedObjectMetaData} matches.
		 */
		private List<Map<String, RawBoundManagedObjectMetaData>> scopeManagedObjectsList = new LinkedList<Map<String, RawBoundManagedObjectMetaData>>();

		/**
		 * Listing of input {@link ManagedObject} matches.
		 */
		private List<RawManagingOfficeMetaData<?>[]> inputManagedObjectsList = new LinkedList<RawManagingOfficeMetaData<?>[]>();

		/**
		 * Adds details for an expected match.
		 * 
		 * @param moConfigurations
		 *            {@link ManagedObjectConfiguration} array.
		 * @param managedObjectScope
		 *            {@link ManagedObjectScope}.
		 * @param registeredManagedObjectSources
		 *            {@link RawManagedObjectMetaData} instances by their
		 *            {@link Office} bound names.
		 * @param scopeMangedObjects
		 *            {@link RawBoundManagedObjectMetaData} in scope.
		 * @param inputManagedObjects
		 *            Input {@link ManagedObject} matches.
		 */
		public void addMatch(ManagedObjectConfiguration<?>[] moConfigurations, ManagedObjectScope managedObjectScope,
				Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources,
				Map<String, RawBoundManagedObjectMetaData> scopeMangedObjects,
				RawManagingOfficeMetaData<?>[] inputManagedObjects) {

			// Ensure the matcher is set
			if (!this.isMatcherSet) {
				RawOfficeMetaDataTest.this.control(RawOfficeMetaDataTest.this.rawBoundManagedObjectFactory)
						.setMatcher(this);
				this.isMatcherSet = true;
			}

			// Maintain details of the match
			this.moConfigurationsList.add(moConfigurations);
			this.managedObjectScopeList.add(managedObjectScope);
			this.registeredManagedObjectSourcesList.add(registeredManagedObjectSources);
			this.scopeManagedObjectsList.add(scopeMangedObjects);
			this.inputManagedObjectsList.add(inputManagedObjects);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean matches(Object[] expected, Object[] actual) {

			// Find the matching scope invocation
			ManagedObjectScope actualScope = (ManagedObjectScope) actual[2];
			int matchIndex = -1;
			for (int i = 0; i < this.managedObjectScopeList.size(); i++) {
				ManagedObjectScope expectedScope = this.managedObjectScopeList.get(i);
				if (actualScope.equals(expectedScope)) {
					matchIndex = i;
				}
			}
			assertTrue("Unexpected method call for scope: " + actualScope, (matchIndex >= 0));

			// Obtain the details for matching
			ManagedObjectConfiguration<?>[] moConfigurations = this.moConfigurationsList.get(matchIndex);
			ManagedObjectScope managedObjectScope = this.managedObjectScopeList.get(matchIndex);
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjectSources = this.registeredManagedObjectSourcesList
					.get(matchIndex);
			Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects = this.scopeManagedObjectsList
					.get(matchIndex);
			RawManagingOfficeMetaData<?>[] inputManagedObjects = this.inputManagedObjectsList.get(matchIndex);

			// Validate the match
			assertEquals("Incorrect managed object configurations", moConfigurations, actual[0]);
			assertEquals("Incorrect issues", RawOfficeMetaDataTest.this.issues, actual[1]);
			assertEquals("Incorrect managed object scope", managedObjectScope, actual[2]);
			assertEquals("Incorrect asset type", AssetType.OFFICE, actual[3]);
			assertEquals("Incorrect asset name", OFFICE_NAME, actual[4]);
			assertTrue("Should be asset manager factory", actual[5] instanceof AssetManagerFactory);
			assertEquals("Incorrect registered managed objects", registeredManagedObjectSources, actual[6]);
			assertEquals("Incorrect have scope managed objects", scopeManagedObjects, actual[7]);
			RawManagingOfficeMetaData<?>[] actualInputManagedObjects = (RawManagingOfficeMetaData<?>[]) actual[8];
			Map<String, String> actualInputSourceMappings = (Map<String, String>) actual[9];
			switch (managedObjectScope) {
			case PROCESS:
				assertEquals("Incorrect number of input Managed Objects", inputManagedObjects.length,
						actualInputManagedObjects.length);
				for (int i = 0; i < inputManagedObjects.length; i++) {
					assertEquals("Incorrect input managed object " + i, inputManagedObjects[i],
							actualInputManagedObjects[i]);
				}
				assertEquals("Incorrect input -> source mappings", RawOfficeMetaDataTest.this.boundInputManagedObjects,
						actualInputSourceMappings);
				break;
			default:
				assertNull("Should not have input configuration for non-process binding", actualInputManagedObjects);
				assertNull("Should only have input bindings for non-process binding", actualInputSourceMappings);
				break;
			}

			return true;
		}
	}
}