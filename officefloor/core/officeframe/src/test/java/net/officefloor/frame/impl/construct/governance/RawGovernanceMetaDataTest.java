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
package net.officefloor.frame.impl.construct.governance;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceEscalationConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawGovernanceMetaDataFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link GovernanceConfiguration}.
	 */
	private final GovernanceConfiguration<?, ?> configuration = this.createMock(GovernanceConfiguration.class);

	/**
	 * {@link Governance} index within the {@link ProcessState}.
	 */
	private final int GOVERNANCE_INDEX = 3;

	/**
	 * {@link Governance} name.
	 */
	private final String GOVERNANCE_NAME = "GOVERNANCE";

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<?, ?> governanceFactory = this.createMock(GovernanceFactory.class);

	/**
	 * {@link Office} name.
	 */
	private final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link Team} name.
	 */
	private final String TEAM_NAME = "TEAM";

	/**
	 * {@link TeamManagement}. May be set to <code>null</code> to not be
	 * available.
	 */
	private TeamManagement responsibleTeam = this.createMock(TeamManagement.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this.createMock(OfficeMetaData.class);

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator = this.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if no {@link GovernanceSource} name.
	 */
	public void testNoGovernanceSourceName() {

		// Record no name
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), null);
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, "Governance added without a name");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link GovernanceFactory}.
	 */
	public void testNoGovernanceFactory() {

		// Record no class
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), null);
		this.record_issue("No GovernanceFactory provided");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension interface.
	 */
	public void testNoExtensionInterface() {

		// Record no class
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), null);
		this.record_issue("No extension interface type provided");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getTeamName(), null);
		this.record_issue("Must specify Team responsible for Governance activities");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no corresponding {@link Team}.
	 */
	public void testNoTeam() {

		// No team
		this.responsibleTeam = null;

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
		this.record_issue("Can not find Team by name '" + TEAM_NAME + "'");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle simple {@link Governance} without {@link Flow}.
	 */
	public void testSimpleGovernance() {

		// Record simple governance
		this.record_initGovernance();
		this.record_flows();
		this.record_escalations();

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME, rawMetaData.getGovernanceName());
		assertEquals("Incorrect extension interface type", String.class, rawMetaData.getExtensionInterfaceType());

		// Verify governance meta-data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME, governanceMetaData.getGovernanceName());
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} name for {@link Flow}.
	 */
	public void testNoFlowFunctionName() {

		final GovernanceFlowConfiguration<?> flowConfiguration = this.createMock(GovernanceFlowConfiguration.class);
		final ManagedFunctionReference taskNode = this.createMock(ManagedFunctionReference.class);

		// Record initiate
		this.record_initGovernance();

		// Record flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new GovernanceFlowConfiguration<?>[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getInitialFunction(), taskNode);
		this.recordReturn(taskNode, taskNode.getFunctionName(), null);
		this.record_issue("No function name provided for flow index 0");

		// Record no escalations
		this.record_escalations();

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} for flow.
	 */
	public void testNoFlowFunction() {

		final GovernanceFlowConfiguration<?> flowConfiguration = this.createMock(GovernanceFlowConfiguration.class);
		final ManagedFunctionReference taskNode = this.createMock(ManagedFunctionReference.class);

		// Record initiate
		this.record_initGovernance();

		// Record flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new GovernanceFlowConfiguration<?>[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getInitialFunction(), taskNode);
		this.recordReturn(taskNode, taskNode.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), null);
		this.record_issue("Can not find function meta-data TASK for flow index 0");

		// Record no escalations
		this.record_escalations();

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incorrect parameter for flow.
	 */
	public void testFlowIncorrectParameter() {

		final GovernanceFlowConfiguration<?> flowConfiguration = this.createMock(GovernanceFlowConfiguration.class);
		final ManagedFunctionReference taskNode = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record initiate
		this.record_initGovernance();

		// Record incorrect parameter for flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new GovernanceFlowConfiguration<?>[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getInitialFunction(), taskNode);
		this.recordReturn(taskNode, taskNode.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), taskMetaData);
		this.recordReturn(taskNode, taskNode.getArgumentType(), String.class);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), Integer.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + String.class.getName()
				+ ", parameter=" + Integer.class.getName() + ", function=TASK) for flow index 0");

		// Record no escalations
		this.record_escalations();

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to configure a flow.
	 */
	public void testGovernanceFlow() {

		// Record flows for governance
		this.record_initGovernance();
		ManagedFunctionMetaData<?, ?>[] taskMetaDatas = this.record_flows(false, true);
		this.record_escalations();

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();

		// Verify the flows
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();

		// Verify the first flow
		FlowMetaData flowOne = governanceMetaData.getFlow(0);
		assertNotNull("Should have first flow", flowOne);
		assertEquals("Incorrect task meta-data for first flow", taskMetaDatas[0], flowOne.getInitialFunctionMetaData());
		assertFalse("Should not spawn thread for first flow", flowOne.isSpawnThreadState());

		// Verify the second flow
		FlowMetaData flowTwo = governanceMetaData.getFlow(1);
		assertNotNull("Should have second flow", flowTwo);
		assertEquals("Incorrect task meta-data for second flow", taskMetaDatas[1],
				flowTwo.getInitialFunctionMetaData());
		assertTrue("Should spawn thread for second flow", flowTwo.isSpawnThreadState());

		// Should not be a third flow
		try {
			governanceMetaData.getFlow(2);
			fail("Should not be successful");
		} catch (ArrayIndexOutOfBoundsException ex) {
			// Correctly have no third flow
		}
	}

	/**
	 * Ensure issue if no cause type for {@link Escalation}.
	 */
	public void testNoEscalationTypeOfCause() {

		final GovernanceEscalationConfiguration escalation = this.createMock(GovernanceEscalationConfiguration.class);

		// Record simple governance
		this.record_initGovernance();
		this.record_flows();
		this.recordReturn(this.configuration, this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(), null);
		this.record_issue("No escalation type for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} name for {@link Escalation}.
	 */
	public void testNoEscalationFunctionName() {

		final GovernanceEscalationConfiguration escalation = this.createMock(GovernanceEscalationConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);

		// Record simple governance
		this.record_initGovernance();
		this.record_flows();
		this.recordReturn(this.configuration, this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(), SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), null);
		this.record_issue("No function name provided for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} for {@link Escalation}.
	 */
	public void testNoEscalationFunction() {

		final GovernanceEscalationConfiguration escalation = this.createMock(GovernanceEscalationConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);
		final String TASK_NAME = "TASK";

		// Record simple governance
		this.record_initGovernance();
		this.record_flows();
		this.recordReturn(this.configuration, this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(), SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), TASK_NAME);
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(TASK_NAME), null);
		this.record_issue("Can not find function meta-data " + TASK_NAME + " for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if parameter type {@link ManagedFunction} is incorrect for
	 * {@link Escalation}.
	 */
	public void testEscalationIncorrectParameter() {

		final GovernanceEscalationConfiguration escalation = this.createMock(GovernanceEscalationConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);
		final String TASK_NAME = "TASK";
		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record simple governance
		this.record_initGovernance();
		this.record_flows();
		this.recordReturn(this.configuration, this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(), SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), TASK_NAME);
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(TASK_NAME),
				taskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference.getArgumentType(), SQLException.class);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), RuntimeException.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + SQLException.class.getName()
				+ ", parameter=" + RuntimeException.class.getName() + ", function=" + TASK_NAME
				+ ") for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure configure in {@link EscalationProcedure} for the
	 * {@link Governance}.
	 */
	public void testGovernanceEscalation() {

		final SQLException exception = new SQLException("TEST");

		// Record simple governance
		this.record_initGovernance();
		this.record_flows();
		ManagedFunctionMetaData<?, ?> taskMetaData = this.record_escalations(exception)[0];

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.constructRawGovernanceMetaData(true);
		rawMetaData.loadOfficeMetaData(this.officeMetaData, this.issues);
		this.verifyMockObjects();

		// Verify escalation
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();
		EscalationProcedure escalationProcedure = governanceMetaData.getEscalationProcedure();
		EscalationFlow flow = escalationProcedure.getEscalation(exception);
		assertEquals("Incorrect type of cause", SQLException.class, flow.getTypeOfCause());
		assertEquals("Incorrect task meta-data", taskMetaData, flow.getManagedFunctionMetaData());

		// Ensure not handle unknown escalation
		assertNull("Should not handle unknown escalation",
				escalationProcedure.getEscalation(new RuntimeException("UNKNOWN")));
	}

	/**
	 * Records initialising the {@link GovernanceSource}.
	 */
	private void record_initGovernance() {

		// Record instantiating governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getTeamName(), TEAM_NAME);
	}

	/**
	 * Records the flows.
	 * 
	 * @param isSpawnThreadStates
	 *            Flags for whether to spawn {@link ThreadState} instances for
	 *            the respective flows.
	 * @return {@link ManagedFunctionMetaData} for the flow instances.
	 */
	private ManagedFunctionMetaData<?, ?>[] record_flows(Boolean... isSpawnThreadStates) {

		// Create the listing of mocks
		final GovernanceFlowConfiguration<?>[] flowConfigurations = new GovernanceFlowConfiguration<?>[isSpawnThreadStates.length];
		final ManagedFunctionReference[] taskNodes = new ManagedFunctionReference[isSpawnThreadStates.length];
		final ManagedFunctionMetaData<?, ?>[] taskMetaDatas = new ManagedFunctionMetaData<?, ?>[isSpawnThreadStates.length];
		for (int i = 0; i < isSpawnThreadStates.length; i++) {
			flowConfigurations[i] = this.createMock(GovernanceFlowConfiguration.class);
			taskNodes[i] = this.createMock(ManagedFunctionReference.class);
			taskMetaDatas[i] = this.createMock(ManagedFunctionMetaData.class);
		}

		// Record configuration for flows
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfigurations);

		// Record configuring each flow
		for (int i = 0; i < isSpawnThreadStates.length; i++) {
			GovernanceFlowConfiguration<?> flowConfiguration = flowConfigurations[i];
			ManagedFunctionReference taskNode = taskNodes[i];
			ManagedFunctionMetaData<?, ?> taskMetaData = taskMetaDatas[i];
			boolean isSpawnThreadState = isSpawnThreadStates[i];

			String taskName = "TASK" + i;

			// Record configuring the flow
			this.recordReturn(flowConfiguration, flowConfiguration.getInitialFunction(), taskNode);
			this.recordReturn(taskNode, taskNode.getFunctionName(), taskName);
			this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(taskName),
					taskMetaData);
			this.recordReturn(taskNode, taskNode.getArgumentType(), String.class);
			this.recordReturn(taskMetaData, taskMetaData.getParameterType(), String.class);
			this.recordReturn(flowConfiguration, flowConfiguration.isSpawnThreadState(), isSpawnThreadState);
		}

		// Return the task meta-data
		return taskMetaDatas;
	}

	/**
	 * Records the {@link EscalationProcedure}.
	 * 
	 * @param exceptions
	 *            {@link Throwable} instances to be handled.
	 * @return {@link ManagedFunctionMetaData} for the {@link Throwable}
	 *         instances.
	 */
	private ManagedFunctionMetaData<?, ?>[] record_escalations(Throwable... exceptions) {

		// Create the mocks for the exceptions
		GovernanceEscalationConfiguration[] escalations = new GovernanceEscalationConfiguration[exceptions.length];
		ManagedFunctionMetaData<?, ?>[] taskMetaDatas = new ManagedFunctionMetaData<?, ?>[exceptions.length];
		for (int i = 0; i < exceptions.length; i++) {
			escalations[i] = this.createMock(GovernanceEscalationConfiguration.class);
			taskMetaDatas[i] = this.createMock(ManagedFunctionMetaData.class);
		}

		// Record obtain the escalations
		this.recordReturn(this.configuration, this.configuration.getEscalations(), escalations);

		// Record configuring each escalation
		for (int i = 0; i < exceptions.length; i++) {
			Throwable exception = exceptions[i];
			GovernanceEscalationConfiguration escalation = escalations[i];
			ManagedFunctionMetaData<?, ?> taskMetaData = taskMetaDatas[i];

			final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);
			final String TASK_NAME = "TASK" + i;

			// Record configuring the escalation
			this.recordReturn(escalation, escalation.getTypeOfCause(), exception.getClass());
			this.recordReturn(escalation, escalation.getTaskNodeReference(), taskNodeReference);
			this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), TASK_NAME);
			this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(TASK_NAME),
					taskMetaData);
			this.recordReturn(taskNodeReference, taskNodeReference.getArgumentType(), exception.getClass());
			this.recordReturn(taskMetaData, taskMetaData.getParameterType(), exception.getClass());
		}

		// Return the task meta-data
		return taskMetaDatas;
	}

	/**
	 * Records an issue for the {@link Governance}.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.GOVERNANCE, GOVERNANCE_NAME, issueDescription);
	}

	/**
	 * Creates the {@link RawGovernanceMetaData}.
	 * 
	 * @param isCreated
	 *            Indicates if expected to create the
	 *            {@link RawGovernanceMetaData}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RawGovernanceMetaData constructRawGovernanceMetaData(boolean isCreated) {

		// Create the map of office teams
		Map<String, TeamManagement> officeTeams = new HashMap<String, TeamManagement>();
		if (this.responsibleTeam != null) {
			officeTeams.put(TEAM_NAME, this.responsibleTeam);
		}

		// Create the raw governance meta-data
		RawGovernanceMetaData rawGovernanceMetaData = RawGovernanceMetaDataImpl.getFactory()
				.createRawGovernanceMetaData((GovernanceConfiguration) this.configuration, GOVERNANCE_INDEX,
						officeTeams, OFFICE_NAME, this.issues);
		if (!isCreated) {
			// Ensure not created
			assertNull("Should not create the Raw Governance Meta-Data", rawGovernanceMetaData);

		} else {
			// Ensure created with correct index
			assertNotNull("Raw Governance Meta-Data should be created", rawGovernanceMetaData);
			assertEquals("Incorrect index for Governance", GOVERNANCE_INDEX,
					rawGovernanceMetaData.getGovernanceIndex());
		}

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

}