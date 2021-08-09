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

import net.officefloor.frame.api.build.FlowBuilder;
import net.officefloor.frame.api.build.FunctionBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuilderImplTest extends OfficeFrameTestCase {

	/**
	 * {@link Office} name.
	 */
	private final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeBuilderImpl}.
	 */
	private OfficeBuilderImpl officeBuilder = new OfficeBuilderImpl(OFFICE_NAME);

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedFunctionFactory<Indexed, Indexed> functionFactory = this.createMock(ManagedFunctionFactory.class);

	/**
	 * Ensure able to get the {@link FunctionBuilder}.
	 */
	public void testGetFlowBuilder() {

		// Name spaced function name
		String namespacedFunction = OfficeBuilderImpl.getNamespacedName("namespace", "function");

		// Add a function
		ManagedFunctionBuilder<Indexed, Indexed> functionBuilder = this.officeBuilder
				.addManagedFunction(namespacedFunction, this.functionFactory);

		// Ensure can get function as flow node builder
		FlowBuilder<?> flowBuilder = this.officeBuilder.getFlowBuilder("namespace", "function");
		assertEquals("Incorrect flow node builder", functionBuilder, flowBuilder);
	}

}
