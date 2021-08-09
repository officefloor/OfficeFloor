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

package net.officefloor.frame.impl.construct.flow;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.function.FlowConfigurationImpl;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionReferenceImpl;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
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
	 * Name of {@link Flow}.
	 */
	private static final String FLOW_NAME = "FLOW";

	/**
	 * Name of the referenced {@link ManagedFunction}.
	 */
	private static final String FUNCTION_NAME = "FUNCTION";

	/**
	 * {@link FlowConfiguration}.
	 */
	private FlowConfiguration<?> configuration = new FlowConfigurationImpl<>(FLOW_NAME,
			new ManagedFunctionReferenceImpl(FUNCTION_NAME, null), false, 0, null);

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
	 * Ensure issue if no {@link Flow} {@link ManagedFunctionReference}.
	 */
	public void testNoFlowFunctionReference() {

		// Record
		this.configuration = new FlowConfigurationImpl<>(FLOW_NAME, null, false, 0, null);
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

		// Record
		this.configuration = new FlowConfigurationImpl<>(FLOW_NAME, new ManagedFunctionReferenceImpl(null, null), false,
				0, null);
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

		// Record
		this.record_issue("Can not find function meta-data " + FUNCTION_NAME + " for flow index 0");

		// Attempt to construct flows
		this.replayMockObjects();
		this.constructFlowMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incorrect parameter for flow.
	 */
	public void testFlowIncorrectParameter() {

		// Record
		this.configuration = new FlowConfigurationImpl<>(FLOW_NAME,
				new ManagedFunctionReferenceImpl(FUNCTION_NAME, String.class), false, 0, null);
		this.officeMetaData.addManagedFunction(FUNCTION_NAME, Integer.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + String.class.getName()
				+ ", parameter=" + Integer.class.getName() + ", function=" + FUNCTION_NAME + ") for flow index 0");

		// Attempt to construct flows
		this.replayMockObjects();
		this.constructFlowMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to create {@link FlowMetaData}.
	 */
	public void testCreateFlowMetaData() {

		// Record
		this.configuration = new FlowConfigurationImpl<>(FLOW_NAME,
				new ManagedFunctionReferenceImpl(FUNCTION_NAME, null), true, 0, null);
		ManagedFunctionMetaData<?, ?> function = this.officeMetaData.addManagedFunction(FUNCTION_NAME, null);

		// Attempt to construct flows
		this.replayMockObjects();
		FlowMetaData flow = this.constructFlowMetaData(true)[0];
		this.verifyMockObjects();

		// Ensure correct information on flows
		assertSame("Incorrect function meta-data", function, flow.getInitialFunctionMetaData());
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
		FlowMetaDataFactory factory = new FlowMetaDataFactory(this.officeMetaData.build());
		FlowMetaData[] flowMetaData = factory.createFlowMetaData(new FlowConfiguration[] { this.configuration },
				AssetType.FUNCTION, "FUNCTION", this.issues);
		if (isCreate) {
			assertNotNull("Should have create the flow meta-data", flowMetaData);
			assertEquals("Incorrect number of flow meta-data", 1, flowMetaData.length);
		} else {
			assertNull("Should no create the flow meta-data", flowMetaData);
		}
		return flowMetaData;
	}

}
