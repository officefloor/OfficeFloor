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

package net.officefloor.frame.impl.construct.escalation;

import java.sql.SQLException;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.function.EscalationConfigurationImpl;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link EscalationFlowFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class EscalationFlowFactoryTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private static final String FUNCTION_NAME = "FUNCTION";

	/**
	 * {@link EscalationConfiguration}.
	 */
	private EscalationConfiguration configuration = new EscalationConfigurationImpl(SQLException.class,
			new ManagedFunctionReferenceImpl(FUNCTION_NAME, SQLException.class));

	/**
	 * Name of the {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData(OFFICE_NAME);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensure issue if no cause type for {@link Escalation}.
	 */
	public void testNoEscalationTypeOfCause() {

		// Record
		this.configuration = new EscalationConfigurationImpl(null,
				new ManagedFunctionReferenceImpl(FUNCTION_NAME, null));
		this.record_issue("No escalation type for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructEscalationFlows(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} name for {@link Escalation}.
	 */
	public void testNoEscalationFunctionName() {

		// Record
		this.configuration = new EscalationConfigurationImpl(Throwable.class,
				new ManagedFunctionReferenceImpl(null, null));
		this.record_issue("No function name provided for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructEscalationFlows(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} for {@link Escalation}.
	 */
	public void testNoEscalationFunction() {

		// Record
		this.record_issue("Can not find function meta-data " + FUNCTION_NAME + " for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructEscalationFlows(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if parameter type {@link ManagedFunction} is incorrect for
	 * {@link Escalation}.
	 */
	public void testEscalationIncorrectParameter() {

		// Record
		this.officeMetaData.addManagedFunction(FUNCTION_NAME, RuntimeException.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + SQLException.class.getName()
				+ ", parameter=" + RuntimeException.class.getName() + ", function=" + FUNCTION_NAME
				+ ") for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructEscalationFlows(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure configure in {@link EscalationProcedure} for the
	 * {@link Governance}.
	 */
	public void testGovernanceEscalation() {

		final SQLException exception = new SQLException("TEST");

		// Record
		ManagedFunctionMetaData<?, ?> function = this.officeMetaData.addManagedFunction(FUNCTION_NAME,
				SQLException.class);

		// Attempt to construct governance
		this.replayMockObjects();
		EscalationFlow[] escalationFlows = this.constructEscalationFlows(true);
		this.verifyMockObjects();

		// Verify escalation
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalationFlows);
		EscalationFlow flow = escalationProcedure.getEscalation(exception);
		assertEquals("Incorrect type of cause", SQLException.class, flow.getTypeOfCause());
		assertEquals("Incorrect function meta-data", function, flow.getManagedFunctionMetaData());

		// Ensure not handle unknown escalation
		assertNull("Should not handle unknown escalation",
				escalationProcedure.getEscalation(new RuntimeException("UNKNOWN")));
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.FUNCTION, "FUNCTION", issueDescription);
	}

	/**
	 * Constructs the {@link EscalationFlow} instances.
	 * 
	 * @param isCreate
	 *            Indicates if expected to create the {@link EscalationFlow}
	 *            instances.
	 * @return {@link EscalationFlow} instances.
	 */
	private EscalationFlow[] constructEscalationFlows(boolean isCreate) {

		// Construct the escalation flows
		EscalationFlowFactory factory = new EscalationFlowFactory(this.officeMetaData.build());
		EscalationFlow[] flows = factory.createEscalationFlows(new EscalationConfiguration[] { this.configuration },
				AssetType.FUNCTION, "FUNCTION", this.issues);

		// Ensure appropriately created (or not)
		if (isCreate) {
			assertNotNull("Should have created the escalation flows", flows);
			assertEquals("Incorrect number of escalation flows", 1, flows.length);
		} else {
			assertNull("Should not have created escalation flows", flows);
		}

		// Return the escalation flows
		return flows;
	}

}
