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
package net.officefloor.frame.impl.construct.escalation;

import java.sql.SQLException;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.EscalationFlowFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
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
	 * {@link EscalationConfiguration}.
	 */
	private final EscalationConfiguration configuration = this.createMock(EscalationConfiguration.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this.createMock(OfficeMetaData.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator = this.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link ManagedFunctionReference}.
	 */
	private final ManagedFunctionReference functionReference = this.createMock(ManagedFunctionReference.class);

	/**
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> functionMetaData = this.createMock(ManagedFunctionMetaData.class);

	/**
	 * Ensure issue if no cause type for {@link Escalation}.
	 */
	public void testNoEscalationTypeOfCause() {

		// Record simple governance
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getTypeOfCause(), null);
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

		// Record simple governance
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getTypeOfCause(), SQLException.class);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionReference(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), null);
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

		final String TASK_NAME = "TASK";

		// Record simple governance
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getTypeOfCause(), SQLException.class);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionReference(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), TASK_NAME);
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(TASK_NAME), null);
		this.record_issue("Can not find function meta-data " + TASK_NAME + " for escalation index 0");

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

		final String TASK_NAME = "TASK";

		// Record simple governance
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getTypeOfCause(), SQLException.class);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionReference(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), TASK_NAME);
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(TASK_NAME),
				this.functionMetaData);
		this.recordReturn(this.functionReference, this.functionReference.getArgumentType(), SQLException.class);
		this.recordReturn(this.functionMetaData, this.functionMetaData.getParameterType(), RuntimeException.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + SQLException.class.getName()
				+ ", parameter=" + RuntimeException.class.getName() + ", function=" + TASK_NAME
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

		final String TASK_NAME = "TASK";

		// Record simple governance
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getTypeOfCause(), SQLException.class);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionReference(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), TASK_NAME);
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(TASK_NAME),
				this.functionMetaData);
		this.recordReturn(this.functionReference, this.functionReference.getArgumentType(), SQLException.class);
		this.recordReturn(this.functionMetaData, this.functionMetaData.getParameterType(), SQLException.class);

		// Attempt to construct governance
		this.replayMockObjects();
		EscalationFlow[] escalationFlows = this.constructEscalationFlows(true);
		this.verifyMockObjects();

		// Verify escalation
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalationFlows);
		EscalationFlow flow = escalationProcedure.getEscalation(exception);
		assertEquals("Incorrect type of cause", SQLException.class, flow.getTypeOfCause());
		assertEquals("Incorrect task meta-data", this.functionMetaData, flow.getManagedFunctionMetaData());

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
		EscalationFlowFactory factory = new EscalationFlowFactoryImpl();
		EscalationFlow[] flows = factory.createEscalationFlows(new EscalationConfiguration[] { this.configuration },
				officeMetaData, AssetType.FUNCTION, "FUNCTION", this.issues);

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