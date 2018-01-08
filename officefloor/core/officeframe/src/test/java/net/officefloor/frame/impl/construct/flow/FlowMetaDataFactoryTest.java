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
package net.officefloor.frame.impl.construct.flow;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link FlowMetaDataFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class FlowMetaDataFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link FlowConfiguration}.
	 */
	private final FlowConfiguration<?> configuration = this.createMock(FlowConfiguration.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this.createMock(OfficeMetaData.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link ManagedFunctionReference}.
	 */
	private final ManagedFunctionReference functionReference = this.createMock(ManagedFunctionReference.class);

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator = this.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> functionMetaData = this.createMock(ManagedFunctionMetaData.class);

	/**
	 * Ensure issue if no {@link Flow} {@link ManagedFunctionReference}.
	 */
	public void testNoFlowFunctionReference() {

		// Record no task node reference
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getInitialFunction(), null);
		this.record_issue("No function referenced for flow index 0");

		// Attempt to construct flows
		this.replayMockObjects();
		this.constructFlowMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} name for {@link Flow}.
	 */
	public void testNoFlowFunctionName() {

		// Record flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getInitialFunction(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), null);
		this.record_issue("No function name provided for flow index 0");

		// Attempt to construct flows
		this.replayMockObjects();
		this.constructFlowMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} for flow.
	 */
	public void testNoFlowFunction() {

		// Record flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getInitialFunction(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), null);
		this.record_issue("Can not find function meta-data TASK for flow index 0");

		// Attempt to construct flows
		this.replayMockObjects();
		this.constructFlowMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incorrect parameter for flow.
	 */
	public void testFlowIncorrectParameter() {

		// Record incorrect parameter for flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getInitialFunction(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"),
				this.functionMetaData);
		this.recordReturn(this.functionReference, this.functionReference.getArgumentType(), String.class);
		this.recordReturn(this.functionMetaData, this.functionMetaData.getParameterType(), Integer.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + String.class.getName()
				+ ", parameter=" + Integer.class.getName() + ", function=TASK) for flow index 0");

		// Attempt to construct flows
		this.replayMockObjects();
		this.constructFlowMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to create {@link FlowMetaData}.
	 */
	public void testCreateFlowMetaData() {

		// Record incorrect parameter for flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getInitialFunction(), this.functionReference);
		this.recordReturn(this.functionReference, this.functionReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"),
				this.functionMetaData);
		this.recordReturn(this.functionReference, this.functionReference.getArgumentType(), Integer.class);
		this.recordReturn(this.functionMetaData, this.functionMetaData.getParameterType(), Integer.class);
		this.recordReturn(this.configuration, this.configuration.isSpawnThreadState(), true);

		// Attempt to construct flows
		this.replayMockObjects();
		FlowMetaData flow = this.constructFlowMetaData(true)[0];
		this.verifyMockObjects();

		// Ensure correct information on flows
		assertSame("Incorrect function meta-data", this.functionMetaData, flow.getInitialFunctionMetaData());
		assertTrue("Should spawn throw", flow.isSpawnThreadState());
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.s
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.FUNCTION, "FUNCTION", issueDescription);
	}

	/**
	 * Constructs the {@link FlowMetaData}.
	 * 
	 * @param isCreate
	 *            Indicates if should create the {@link FlowMetaData}.
	 * @return {@link FlowMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private FlowMetaData[] constructFlowMetaData(boolean isCreate) {
		FlowMetaDataFactory factory = new FlowMetaDataFactory();
		FlowMetaData[] flowMetaData = factory.createFlowMetaData(new FlowConfiguration[] { this.configuration },
				this.officeMetaData, AssetType.FUNCTION, "FUNCTION", this.issues);
		if (isCreate) {
			assertNotNull("Should have create the flow meta-data", flowMetaData);
			assertEquals("Incorrect number of flow meta-data", 1, flowMetaData.length);
		} else {
			assertNull("Should no create the flow meta-data", flowMetaData);
		}
		return flowMetaData;
	}

}