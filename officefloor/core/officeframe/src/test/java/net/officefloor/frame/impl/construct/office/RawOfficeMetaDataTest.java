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

package net.officefloor.frame.impl.construct.office;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.FunctionBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawManagingOfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawOfficeFloorMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawOfficeMetaData}.
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
	private OfficeBuilderImpl configuration = new OfficeBuilderImpl(OFFICE_NAME);

	/**
	 * {@link RawOfficeFloorMetaData}.
	 */
	private final RawOfficeFloorMetaDataMockBuilder rawOfficeFloorMetaData = MockConstruct.mockRawOfficeFloorMetaData();

	/**
	 * {@link RawManagingOfficeMetaData} instances.
	 */
	private final List<RawManagingOfficeMetaDataMockBuilder<?>> officeManagingManagedObjects = new LinkedList<>();

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensure issue if no {@link Office} name.
	 */
	public void testNoOfficeName() {

		// Record no office name
		this.configuration = new OfficeBuilderImpl(null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OfficeFloor", "Office registered without name");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if non-positive {@link AsynchronousFlow} timeout.
	 */
	public void testNonPositiveAsynchronousFlowTimeout() {

		// Record non-positive asynchronous flow timeout
		this.configuration.setDefaultAsynchronousFlowTimeout(0);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Office default AsynchronousFlow timeout must be positive (0)");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to override the {@link MonitorClock}.
	 */
	public void testProvideOfficeClock() {

		// Record
		final MonitorClock clock = this.createMock(MonitorClock.class);
		this.configuration.setMonitorClock(clock);

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure override clock
		assertSame("Should override office clock", clock, rawOfficeMetaData.getOfficeMetaData().getMonitorClock());
	}

	/**
	 * Ensure able to default the {@link MonitorClock}.
	 */
	public void testDefaultOfficeClock() {

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure override clock
		assertNotNull("Should default office clock", rawOfficeMetaData.getOfficeMetaData().getMonitorClock());
	}

	/**
	 * Ensure issue if negative monitor {@link Office} interval.
	 */
	public void testNegativeMonitorOfficeInterval() {

		// Record negative monitor office interval
		this.configuration.setMonitorOfficeInterval(-1);
		this.record_issue("Monitor office interval can not be negative");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if non-positivie functionst monitor {@link Office} interval.
	 */
	public void testNonPositiiveMaximumFunctionStateChainLength() {

		// Record non-positive monitor office interval
		this.configuration.setMaximumFunctionStateChainLength(0);
		this.record_issue("Maximum FunctionState chain length must be positive");

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
		this.configuration.addOfficeEnhancer((context) -> {
			throw failure;
		});
		this.record_issue("Failure in enhancing office", failure);

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no flow to be enhanced.
	 */
	public void testNoFlowForOfficeEnhancing() {

		// Record
		this.configuration.addOfficeEnhancer((context) -> {
			context.getFlowBuilder("FUNCTION");
			fail("Should not successfully obtain flow");
		});
		this.record_issue("ManagedFunction 'FUNCTION' of namespace '[none]' not available for enhancement");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedObject} {@link ManagedFunction} in
	 * {@link Office}.
	 */
	public void testNoManagedObjectFunctionForOfficeEnhancing() {

		// Record
		this.configuration.addOfficeEnhancer((context) -> {
			context.getFlowBuilder("MANAGED_OBJECT", "FUNCTION");
			fail("Should not successfully obtain flow");
		});
		this.record_issue("ManagedFunction 'FUNCTION' of namespace 'MANAGED_OBJECT' not available for enhancement");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain {@link FunctionBuilder} for an {@link OfficeEnhancer}.
	 */
	public void testGetFlowNodeBuilderForOfficeEnhancing() {

		// Record
		this.configuration.addOfficeEnhancer((context) -> {
			assertNotNull("Should obtain flow", context.getFlowBuilder("FUNCTION"));
		});
		this.configuration.addManagedFunction("FUNCTION", () -> null);

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Office} {@link Team} name.
	 */
	public void testNoRegisteredTeamName() {

		// Record
		this.configuration.registerTeam(null, "TEAM");
		this.record_issue("Team registered to Office without name");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link OfficeFloor} {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record
		this.configuration.registerTeam("OFFICE_TEAM", null);
		this.record_issue("No OfficeFloor Team name for Office Team 'OFFICE_TEAM'");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record
		this.configuration.registerTeam("OFFICE_TEAM", "OFFICE_FLOOR_TEAM");
		this.record_issue("Unknown Team 'OFFICE_FLOOR_TEAM' not available to register to Office");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown default {@link Team}.
	 */
	public void testUnknownDefaultTeam() {

		this.configuration.setDefaultTeam("UNKNOWN");
		this.record_issue("No default team UNKNOWN linked to Office");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Enable able to include {@link ThreadLocalAwareTeam} instances.
	 */
	public void testRegisteredThreadLocalAwareTeam() {

		// Record
		Team team = this.createMock(Team.class);
		this.rawOfficeFloorMetaData.registerTeam("OF_TEAM", team, true);
		this.configuration.registerTeam("TEAM", "OF_TEAM");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Enable able to include {@link ThreadLocalAwareTeam} instance as default
	 * {@link Team}.
	 */
	public void testDefaultThreadLocalAwareTeam() {

		// Record
		Team team = this.createMock(Team.class);
		this.rawOfficeFloorMetaData.registerTeam("OF_TEAM", team, true);
		this.configuration.registerTeam("TEAM", "OF_TEAM");
		this.configuration.setDefaultTeam("TEAM");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Office} {@link ManagedObject} name.
	 */
	public void testNoRegisteredManagedObjectName() {

		// Record
		this.configuration.registerManagedObjectSource(null, "MOS");
		this.record_issue("Managed Object registered to Office without name");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link OfficeFloor} {@link ManagedObjectSource} name.
	 */
	public void testNoManagedObjectSourceName() {

		// Record
		this.configuration.registerManagedObjectSource("MO", null);
		this.record_issue("No Managed Object Source name for Office Managed Object 'MO'");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedObjectSource}.
	 */
	public void testUnknownManagedObjectSource() {

		// Record
		this.configuration.registerManagedObjectSource("MO", "MOS");
		this.record_issue("Unknown Managed Object Source 'MOS' not available to register to Office");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no Input {@link ManagedObject} name.
	 */
	public void testNoInputManagedObjectName() {

		// Record
		this.configuration.setBoundInputManagedObject(null, "MOS");
		this.record_issue("No input Managed Object name for binding");

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

		// Record
		this.configuration.setBoundInputManagedObject("INPUT", null);
		this.record_issue("No bound Managed Object Source name for input Managed Object 'INPUT'");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if same bound {@link ManagedObjectSource} name for Input
	 * {@link ManagedObject}.
	 */
	public void testInputManagedObjectNameRegisteredMoreThanOnce() {

		// Record
		this.configuration.setBoundInputManagedObject("SAME_INPUT", "FIRST");
		this.configuration.setBoundInputManagedObject("SAME_INPUT", "SECOND");
		this.record_issue("Input Managed Object 'SAME_INPUT' bound more than once");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct a {@link ProcessState} bound {@link ManagedObject}.
	 */
	public void testConstructProcessBoundManagedObject() {

		// Record
		this.configuration.registerManagedObjectSource("MO", "MOS");
		this.rawOfficeFloorMetaData.registerManagedObjectSource("MOS");
		this.configuration.addProcessManagedObject("BOUND_MO", "MO");

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData office = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure have process bound managed object
		assertEquals("Should only be the one process bound managed object", 1,
				office.getProcessBoundManagedObjects().length);
		assertEquals("Incorrect proccess bound managed object", "BOUND_MO",
				office.getProcessBoundManagedObjects()[0].getBoundManagedObjectName());
	}

	/**
	 * Ensure able construct Input {@link ManagedObject}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testConstructInputManagedObject() {

		// Record
		RawManagedObjectMetaDataMockBuilder<?, ?> mo = this.rawOfficeFloorMetaData.registerManagedObjectSource("MOS");
		RawManagingOfficeMetaDataMockBuilder managingOffice = this.mockRawManagingOfficeMetaData("MOS");
		managingOffice.getBuilder().setInputManagedObjectName("INPUT");
		managingOffice.build().setRawManagedObjectMetaData(mo.build(managingOffice.build()));

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData office = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Confirm input managed object
		RawBoundManagedObjectMetaData input = office.getProcessBoundManagedObjects()[0];
		assertEquals("Incorrect managed object", "INPUT", input.getBoundManagedObjectName());
	}

	/**
	 * Ensure able to construct a {@link ThreadState} bound {@link ManagedObject}.
	 */
	public void testConstructThreadBoundManagedObject() {

		// Record
		this.configuration.registerManagedObjectSource("MO", "MOS");
		this.rawOfficeFloorMetaData.registerManagedObjectSource("MOS");
		this.configuration.addThreadManagedObject("BOUND_MO", "MO");

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData office = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure have process bound managed object
		assertEquals("Should only be the one process bound managed object", 1,
				office.getThreadBoundManagedObjects().length);
		assertEquals("Incorrect proccess bound managed object", "BOUND_MO",
				office.getThreadBoundManagedObjects()[0].getBoundManagedObjectName());
	}

	/**
	 * Ensure able to construct {@link ThreadState} and {@link ProcessState} bound
	 * {@link ManagedObject} instances.
	 */
	public void testConstructProcessAndThreadBoundManagedObjects() {

		// Record
		this.configuration.registerManagedObjectSource("MO", "MOS");
		this.rawOfficeFloorMetaData.registerManagedObjectSource("MOS");
		this.configuration.addProcessManagedObject("PROCESS", "MO");
		this.configuration.addThreadManagedObject("THREAD", "MO");

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData office = this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();

		// Ensure have process and thread managed objects
		assertEquals("Should only be one process managed object", 1, office.getProcessBoundManagedObjects().length);
		assertEquals("Incorrect process managed object", "PROCESS",
				office.getProcessBoundManagedObjects()[0].getBoundManagedObjectName());
		assertEquals("Should only be one thread managed object", 1, office.getThreadBoundManagedObjects().length);
		assertEquals("Incorrect thread managed object", "THREAD",
				office.getThreadBoundManagedObjects()[0].getBoundManagedObjectName());
	}

	/**
	 * Handles failure to construct {@link Governance}.
	 */
	public void testFailConstructGovernance() {

		// Record
		this.configuration.addGovernance(null, null, null);
		this.record_issue("Governance added without a name");
		this.record_issue("Unable to configure governance 'null'");

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

		// Record
		this.configuration.setManuallyManageGovernance(true);

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

		// Record
		this.configuration.addGovernance("GOVERNANCE_ONE", Object.class, () -> null);
		this.configuration.addGovernance("GOVERNANCE_TWO", Object.class, () -> null);

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
		assertEquals("Incorrect number of governances", 2, governanceMetaDatas.length);
		assertEquals("Incorrect first governance", "GOVERNANCE_ONE", governanceMetaDatas[0].getGovernanceName());
		assertEquals("Incorrect second governance", "GOVERNANCE_TWO", governanceMetaDatas[1].getGovernanceName());
	}

	/**
	 * Enable able to handle not constructing {@link ManagedFunction}.
	 */
	public void testNotConstructFunction() {

		// Record
		this.configuration.addManagedFunction(null, null);
		this.record_issue("ManagedFunction added without name");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Enable able to construct {@link ManagedFunction}.
	 */
	public void testConstructFunction() {

		// Record
		this.configuration.addManagedFunction("FUNCTION", () -> null);

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify the office meta-data
		assertEquals("Incorrect office name", "OFFICE", officeMetaData.getOfficeName());
		ManagedFunctionMetaData<?, ?>[] returnedFunctionMetaData = officeMetaData.getManagedFunctionMetaData();
		assertEquals("Incorrect number of functions", 1, returnedFunctionMetaData.length);
		assertEquals("Incorrect function", "FUNCTION", returnedFunctionMetaData[0].getFunctionName());
	}

	/**
	 * Enable issue if {@link OfficeStartupFunction} not named.
	 */
	public void testNoStartupFunctionName() {

		// Record
		this.configuration.addStartupFunction(null, "PARAMETER");
		this.record_issue("No function name provided for Startup Function 0");

		// Construct
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
	 * Enable issue if {@link OfficeStartupFunction} not found.
	 */
	public void testUnknownStartupFunction() {

		// Record
		this.configuration.addStartupFunction("UNKNOWN", null);
		this.record_issue("Can not find function meta-data UNKNOWN for Startup Function 0");

		// Construct
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

		// Record
		Object parameter = new Object();
		this.configuration.addStartupFunction("FUNCTION", parameter);
		this.configuration.addManagedFunction("FUNCTION", () -> null);

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Verify startup function loaded
		OfficeStartupFunction[] startupFunctions = officeMetaData.getStartupFunctions();
		assertEquals("Incorrect number of startup functions", 1, startupFunctions.length);
		assertEquals("Incorrect startup function meta-data", "FUNCTION",
				startupFunctions[0].getFlowMetaData().getInitialFunctionMetaData().getFunctionName());
		assertEquals("Incorrect startup parameter", parameter, startupFunctions[0].getParameter());
	}

	/**
	 * Enable able to provide {@link Profiler}.
	 */
	public void testProvideProfiler() throws Throwable {

		// Record
		Closure<ProfiledProcessState> process = new Closure<>();
		final Profiler profiler = (state) -> process.value = state;
		this.configuration.setProfiler(profiler);

		// Function
		OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData(OFFICE_NAME);
		ManagedFunctionMetaData<?, ?> function = officeMetaData.addManagedFunction("FUNCTION", null);
		officeMetaData.build();

		// Test
		this.replayMockObjects();
		RawOfficeMetaData office = this.constructRawOfficeMetaData(true);

		// Ensure can create process with profiler
		FunctionState register = office.getOfficeMetaData().createProcess(new FlowMetaDataImpl(false, function), null,
				null, null);
		FunctionStateContext context = new FunctionStateContext() {
			@Override
			public FunctionState executeDelegate(FunctionState delegate) throws Throwable {
				return delegate.execute(this);
			}
		};
		while (register != null) {
			register = register.execute(context);
		}

		this.verifyMockObjects();

		// Ensure have profiled process state
		assertNotNull("Should have profiled process state", process.value);
	}

	/**
	 * Ensure issue if no type of cause for {@link Office} {@link EscalationFlow}.
	 */
	public void testNoTypeOfCauseForOfficeEscalation() {

		// Record
		this.configuration.addEscalation(null, "FUNCTION");
		this.record_issue("Type of cause not provided for office escalation 0");

		// Construct the office
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} for {@link Office}
	 * {@link EscalationFlow}.
	 */
	public void testNoFunctionForOfficeEscalation() {

		// Record
		this.configuration.addEscalation(Throwable.class, null);
		this.record_issue("No function name provided for Office Escalation 0");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedFunction} for {@link Office}
	 * {@link EscalationFlow}.
	 */
	public void testUnknownFunctionForOfficeEscalation() {

		// Record
		this.configuration.addEscalation(Throwable.class, "FUNCTION");
		this.record_issue("Can not find function meta-data FUNCTION for Office Escalation 0");

		// Construct
		this.replayMockObjects();
		this.constructRawOfficeMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link in an {@link Office} {@link EscalationFlow}.
	 */
	public void testConstructOfficeEscalation() {

		final RuntimeException failure = new RuntimeException("Escalation");

		// Record
		this.configuration.addEscalation(Exception.class, "FUNCTION");
		this.configuration.addManagedFunction("FUNCTION", () -> (context) -> {
		});

		// Construct the office
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		this.verifyMockObjects();

		// Obtain the function meta-data
		ManagedFunctionMetaData<?, ?> functionMetaData = officeMetaData.getManagedFunctionLocator()
				.getManagedFunctionMetaData("FUNCTION");
		assertNotNull("Ensure have function", functionMetaData);

		// Verify office escalation loaded
		EscalationProcedure escalationProcedure = officeMetaData.getProcessMetaData().getThreadMetaData()
				.getOfficeEscalationProcedure();
		EscalationFlow escalation = escalationProcedure.getEscalation(failure);
		assertEquals("Incorrect escalation function meta-data", functionMetaData,
				escalation.getManagedFunctionMetaData());
	}

	/**
	 * Ensure able to create a {@link ProcessState} and obtain the
	 * {@link OfficeFloor} {@link EscalationFlow}
	 */
	public void testCreateProcessAndOfficeFloorEscalation() {

		// Record
		EscalationFlow handler = this.createMock(EscalationFlow.class);
		this.rawOfficeFloorMetaData.setOfficeFloorEscalation(handler);

		// Function
		OfficeMetaDataMockBuilder mock = MockConstruct.mockOfficeMetaData(OFFICE_NAME);
		ManagedFunctionMetaData<?, ?> function = mock.addManagedFunction("FUNCTION", null);
		mock.build();

		// Construct
		this.replayMockObjects();
		RawOfficeMetaData rawOfficeMetaData = this.constructRawOfficeMetaData(true);
		OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
		FunctionState process = officeMetaData.createProcess(new FlowMetaDataImpl(false, function), null, null, null);
		this.verifyMockObjects();

		// Verify the OfficeFloor escalation
		assertNotNull("Should create process", process);
		assertEquals("Incorrect OfficeFloor escalation", handler,
				officeMetaData.getProcessMetaData().getThreadMetaData().getOfficeFloorEscalation());
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, issueDescription, cause);
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaData} for this {@link Office}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @return {@link RawManagingOfficeMetaDataMockBuilder}.
	 */
	private RawManagingOfficeMetaDataMockBuilder<?> mockRawManagingOfficeMetaData(String managedObjectSourceName) {
		RawManagingOfficeMetaDataMockBuilder<?> managingOffice = MockConstruct
				.mockRawManagingOfficeMetaData(OFFICE_NAME, managedObjectSourceName);
		this.officeManagingManagedObjects.add(managingOffice);
		return managingOffice;
	}

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param isExpectConstruct Flag indicating if should be constructed.
	 * @return Constructed {@link RawOfficeMetaData}.
	 */
	private RawOfficeMetaData constructRawOfficeMetaData(boolean isExpectConstruct) {

		// Obtain the office managing managed objects
		RawManagingOfficeMetaData<?>[] officeMos = new RawManagingOfficeMetaData[this.officeManagingManagedObjects
				.size()];
		for (int i = 0; i < officeMos.length; i++) {
			officeMos[i] = this.officeManagingManagedObjects.get(i).build();
		}

		// Construct the meta-data
		RawOfficeMetaData metaData = new RawOfficeMetaDataFactory(this.rawOfficeFloorMetaData.build())
				.constructRawOfficeMetaData(this.configuration, officeMos, this.issues);
		if (isExpectConstruct) {
			assertNotNull("Meta-data should be constructed", metaData);
		} else {
			assertNull("Meta-data should NOT be constructed", metaData);
		}

		// Return the meta-data
		return metaData;
	}

}
