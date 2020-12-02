/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.governance;

import java.sql.SQLException;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawOfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
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
	 * {@link Governance} name.
	 */
	private final String GOVERNANCE_NAME = "GOVERNANCE";

	/**
	 * {@link GovernanceConfiguration}.
	 */
	private GovernanceBuilderImpl<?, ?> configuration = new GovernanceBuilderImpl<>(GOVERNANCE_NAME, Object.class,
			() -> null);

	/**
	 * {@link Governance} index within the {@link ProcessState}.
	 */
	private final int GOVERNANCE_INDEX = 3;

	/**
	 * {@link Office} name.
	 */
	private final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaDataMockBuilder rawOfficeMetaData = MockConstruct.mockRawOfficeMetaData(OFFICE_NAME);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData(OFFICE_NAME);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if no {@link GovernanceSource} name.
	 */
	public void testNoGovernanceName() {

		// Record
		this.configuration = new GovernanceBuilderImpl<>(null, Object.class, () -> null);
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, "Governance added without a name");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension type.
	 */
	public void testNoExtensionType() {

		// Record no extension type
		this.configuration = new GovernanceBuilderImpl<>(GOVERNANCE_NAME, null, () -> null);
		this.record_issue("No extension type provided for governance " + GOVERNANCE_NAME);

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link GovernanceFactory}.
	 */
	public void testNoGovernanceFactory() {

		// Record no factory
		this.configuration = new GovernanceBuilderImpl<>(GOVERNANCE_NAME, Object.class, null);
		this.record_issue("No GovernanceFactory provided for governance " + GOVERNANCE_NAME);

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can have no {@link Team} name (results in using any {@link Team}).
	 */
	public void testNoTeamName() {

		// Record
		this.configuration.setResponsibleTeam(null);

		// Construct
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

		// Record
		this.configuration.setResponsibleTeam("UNKNOWN");
		this.record_issue("Can not find Team by name 'UNKNOWN' for governance " + GOVERNANCE_NAME);

		// Construct
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle simple {@link Governance} without {@link Flow}.
	 */
	public void testGovernanceWithTeam() {

		// Record
		this.configuration.setResponsibleTeam("TEAM");
		TeamManagement responsibleTeam = this.rawOfficeMetaData.addTeam("TEAM");

		// Construct
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.fullyConstructRawGovernanceMetaData(true);
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME, rawMetaData.getGovernanceName());
		assertEquals("Incorrect extension type", Object.class, rawMetaData.getExtensionType());

		// Verify governance meta-data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME, governanceMetaData.getGovernanceName());
		assertSame("Incorrect responsible team", responsibleTeam, governanceMetaData.getResponsibleTeam());
	}

	/**
	 * Ensure handle no {@link FlowMetaData}.
	 */
	public void testIssueWithFlows() {

		// Record
		this.configuration.linkFlow(0, "FUNCTION", null, false);
		this.record_issue("Can not find function meta-data FUNCTION for flow index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		this.fullyConstructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to configure a flow.
	 */
	public void testGovernanceFlow() {

		// Record
		this.configuration.linkFlow(0, "ONE", null, false);
		this.configuration.linkFlow(1, "TWO", null, false);
		ManagedFunctionMetaData<?, ?> one = this.officeMetaData.addManagedFunction("ONE", null);
		ManagedFunctionMetaData<?, ?> two = this.officeMetaData.addManagedFunction("TWO", null);

		// Construct
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this.fullyConstructRawGovernanceMetaData(true);
		this.verifyMockObjects();

		// Verify the flows
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData.getGovernanceMetaData();

		// Verify the first flow
		FlowMetaData flowOne = governanceMetaData.getFlow(0);
		assertSame("Incorrect first flow", one, flowOne.getInitialFunctionMetaData());

		// Verify the second flow
		FlowMetaData flowTwo = governanceMetaData.getFlow(1);
		assertSame("Incorrect second flow", two, flowTwo.getInitialFunctionMetaData());

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

		// Record
		this.configuration.addEscalation(null, null);
		this.record_issue("No escalation type for escalation index 0");

		// Construct
		this.replayMockObjects();
		this.fullyConstructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load an {@link EscalationFlow}.
	 */
	public void testEscalationFlow() {

		// Record
		this.configuration.addEscalation(Throwable.class, "FUNCTION");
		ManagedFunctionMetaData<?, ?> function = this.officeMetaData.addManagedFunction("FUNCTION", Throwable.class);

		// Construct
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawGovernance = this.fullyConstructRawGovernanceMetaData(true);

		// Ensure correct escalation flow
		EscalationFlow flow = rawGovernance.getGovernanceMetaData().getEscalationProcedure()
				.getEscalation(new SQLException());
		assertSame("Incorrect escalation flow", function, flow.getManagedFunctionMetaData());

		this.verifyMockObjects();
	}

	/**
	 * Records an issue for the {@link Governance}.
	 * 
	 * @param issueDescription Issue description.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.GOVERNANCE, GOVERNANCE_NAME, issueDescription);
	}

	/**
	 * Creates the {@link RawGovernanceMetaData}.
	 * 
	 * @param isCreated Indicates if expected to create the
	 *                  {@link RawGovernanceMetaData}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	private RawGovernanceMetaData<?, ?> constructRawGovernanceMetaData(boolean isCreated) {

		// Create the raw governance meta-data
		RawGovernanceMetaData<?, ?> rawGovernanceMetaData = new RawGovernanceMetaDataFactory(OFFICE_NAME,
				this.rawOfficeMetaData.build().getTeams()).createRawGovernanceMetaData(this.configuration,
						GOVERNANCE_INDEX, new AssetManagerRegistry(null, null), 1, this.issues);
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
	 * @param isSuccessful Indicates if expect to be successful in loading
	 *                     {@link OfficeMetaData}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	private RawGovernanceMetaData<?, ?> fullyConstructRawGovernanceMetaData(boolean isSuccessful) {
		RawGovernanceMetaData<?, ?> governanceMetaData = this.constructRawGovernanceMetaData(true);
		boolean isLoaded = governanceMetaData.loadOfficeMetaData(this.officeMetaData.build(),
				new FlowMetaDataFactory(this.officeMetaData.build()),
				new EscalationFlowFactory(this.officeMetaData.build()), this.issues);
		assertEquals("Incorrectly loaded", isSuccessful, isLoaded);
		return governanceMetaData;
	}

}
