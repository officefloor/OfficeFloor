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
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.construct.EscalationFlowFactory;
import net.officefloor.frame.internal.construct.FlowMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
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
	 * {@link FlowMetaDataFactory}.
	 */
	private final FlowMetaDataFactory flowMetaDataFactory = this.createMock(FlowMetaDataFactory.class);

	/**
	 * {@link EscalationFlowFactory}.
	 */
	private final EscalationFlowFactory escalationFlowFactory = this.createMock(EscalationFlowFactory.class);

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
	 * Ensure can have no {@link Team} name (results in using any {@link Team}).
	 */
	public void testNoTeamName() {

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), null);
		this.record_flows(0);
		this.record_escalations(0);

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.fullyConstructRawGovernanceMetaData(true);
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();
		this.verifyMockObjects();

		// Ensure any team
		assertNull("Should allow any team", governanceMetaData.getResponsibleTeam());
	}

	/**
	 * Ensure issue if no corresponding {@link Team}.
	 */
	public void testUnknownTeam() {

		// No team
		this.responsibleTeam = null;

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), TEAM_NAME);
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
		this.record_flows(0);
		this.record_escalations(0);

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.fullyConstructRawGovernanceMetaData(true);
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME, rawMetaData.getGovernanceName());
		assertEquals("Incorrect extension interface type", String.class, rawMetaData.getExtensionInterfaceType());

		// Verify governance meta-data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME, governanceMetaData.getGovernanceName());
		assertSame("Incorrect responsible team", this.responsibleTeam, governanceMetaData.getResponsibleTeam());
	}

	/**
	 * Ensure handle no {@link FlowMetaData}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testIssueWithFlows() {

		final FlowConfiguration[] flowConfigurations = new FlowConfiguration<?>[0];

		// Record failure in obtaining flows
		this.record_initGovernance();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfigurations);
		this.recordReturn(this.flowMetaDataFactory, this.flowMetaDataFactory.createFlowMetaData(flowConfigurations,
				this.officeMetaData, AssetType.GOVERNANCE, GOVERNANCE_NAME, this.issues), null);

		// Attempt to construct governance
		this.replayMockObjects();
		this.fullyConstructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to configure a flow.
	 */
	public void testGovernanceFlow() {

		// Record flows for governance
		this.record_initGovernance();
		FlowMetaData[] flowMetaDatas = this.record_flows(2);
		this.record_escalations(0);

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.fullyConstructRawGovernanceMetaData(true);
		this.verifyMockObjects();

		// Verify the flows
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();

		// Verify the first flow
		FlowMetaData flowOne = governanceMetaData.getFlow(0);
		assertSame("Incorrect first flow", flowMetaDatas[0], flowOne);

		// Verify the second flow
		FlowMetaData flowTwo = governanceMetaData.getFlow(1);
		assertSame("Incorrect second flow", flowMetaDatas[1], flowTwo);

		// Should not be a third flow
		try {
			governanceMetaData.getFlow(2);
			fail("Should not be successful");
		} catch (ArrayIndexOutOfBoundsException ex) {
			// Correctly have no third flow
		}
	}

	/**
	 * Ensure handle issue with {@link EscalationFlow} construction.
	 */
	public void testIssueWithEscalation() {

		final EscalationConfiguration[] escalations = new EscalationConfiguration[] {
				this.createMock(EscalationConfiguration.class) };

		// Record issue with escalation
		this.record_initGovernance();
		this.record_flows(0);
		this.recordReturn(this.configuration, this.configuration.getEscalations(), escalations);
		this.recordReturn(this.escalationFlowFactory, this.escalationFlowFactory.createEscalationFlows(escalations,
				this.officeMetaData, AssetType.GOVERNANCE, GOVERNANCE_NAME, this.issues), null);

		// Attempt to construct governance
		this.replayMockObjects();
		this.fullyConstructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load an {@link EscalationFlow}.
	 */
	public void testEscalationFlow() {

		// Record escalation flow
		this.record_initGovernance();
		this.record_flows(0);
		EscalationFlow escalationFlow = this.record_escalations(1)[0];
		this.recordReturn(escalationFlow, escalationFlow.getTypeOfCause(), SQLException.class);

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawGovernance = this.fullyConstructRawGovernanceMetaData(true);

		// Ensure correct escalation flow
		EscalationFlow flow = rawGovernance.getGovernanceMetaData().getEscalationProcedure()
				.getEscalation(new SQLException());
		assertSame("Incorrect escalation flow", escalationFlow, flow);

		this.verifyMockObjects();
	}

	/**
	 * Records initialising the {@link GovernanceSource}.
	 */
	private void record_initGovernance() {

		// Record instantiating governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration, this.configuration.getGovernanceFactory(), this.governanceFactory);
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), TEAM_NAME);
	}

	/**
	 * Records the flows.
	 * 
	 * @param flowCount
	 *            Number of {@link Flow} instances.
	 * @return {@link FlowMetaData} for the {@link Flow} instances.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private FlowMetaData[] record_flows(int flowCount) {

		// Create the listing of mocks
		final FlowConfiguration[] flowConfigurations = new FlowConfiguration<?>[flowCount];
		final FlowMetaData[] flowMetaData = new FlowMetaData[flowCount];
		for (int i = 0; i < flowCount; i++) {
			flowConfigurations[i] = this.createMock(FlowConfiguration.class);
			flowMetaData[i] = this.createMock(FlowMetaData.class);
		}

		// Record creation of the flow meta-data
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfigurations);
		this.recordReturn(this.flowMetaDataFactory, this.flowMetaDataFactory.createFlowMetaData(flowConfigurations,
				this.officeMetaData, AssetType.GOVERNANCE, GOVERNANCE_NAME, this.issues), flowMetaData);

		// Return the flow meta-data
		return flowMetaData;
	}

	/**
	 * Records the {@link EscalationProcedure}.
	 * 
	 * @param escalationCount
	 *            Number of {@link EscalationFlow} instances.
	 * @return {@link EscalationFlow} for the {@link Throwable} instances.
	 */
	private EscalationFlow[] record_escalations(int escalationCount) {

		// Create the mocks for the exceptions
		EscalationConfiguration[] escalations = new EscalationConfiguration[escalationCount];
		EscalationFlow[] flows = new EscalationFlow[escalationCount];
		for (int i = 0; i < escalationCount; i++) {
			escalations[i] = this.createMock(EscalationConfiguration.class);
			flows[i] = this.createMock(EscalationFlow.class);
		}

		// Record obtain the escalations
		this.recordReturn(this.configuration, this.configuration.getEscalations(), escalations);
		this.recordReturn(this.escalationFlowFactory, this.escalationFlowFactory.createEscalationFlows(escalations,
				this.officeMetaData, AssetType.GOVERNANCE, GOVERNANCE_NAME, this.issues), flows);

		// Return the flows
		return flows;
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

	/**
	 * Fully creates the {@link RawGovernanceMetaData}.
	 * 
	 * @param isSuccessful
	 *            Indicates if expect to be successful in loading
	 *            {@link OfficeMetaData}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	@SuppressWarnings("rawtypes")
	private RawGovernanceMetaData fullyConstructRawGovernanceMetaData(boolean isSuccessful) {
		RawGovernanceMetaData governanceMetaData = this.constructRawGovernanceMetaData(true);
		boolean isLoaded = governanceMetaData.loadOfficeMetaData(this.officeMetaData, this.flowMetaDataFactory,
				this.escalationFlowFactory, this.issues);
		assertEquals("Incorrectly loaded", isSuccessful, isLoaded);
		return governanceMetaData;
	}

}