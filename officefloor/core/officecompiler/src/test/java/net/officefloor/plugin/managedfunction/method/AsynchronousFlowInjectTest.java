/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.MockAsynchronousFlow;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests injecting {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure can inject {@link AsynchronousFlow}.
	 */
	public void testAsynchronousFlow() {
		AsynchronousFlowFunction instance = new AsynchronousFlowFunction();
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(instance, "method", null, null);
		MockAsynchronousFlow[] asyncFlows = result.getAsynchronousFlows();
		assertEquals("Should have async flow", 1, asyncFlows.length);
		assertSame("Incorrect async flow", asyncFlows[0], instance.asyncFlow);
	}

	public static class AsynchronousFlowFunction {

		public AsynchronousFlow asyncFlow;

		public void method(AsynchronousFlow asyncFlow) {
			this.asyncFlow = asyncFlow;
		}
	}

}
