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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure invokes the next {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public class NextFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can invoke next {@link ManagedFunction} without a parmaeter.
	 */
	public void testInvokeNextFunction() throws Exception {
		this.doInvokeNextFunctionTest(null);
	}

	/**
	 * Ensure can invoke next {@link ManagedFunction} with a parameter.
	 */
	public void testInvokeNextFunctionWithParameter() throws Exception {
		this.doInvokeNextFunctionTest("TEST");
	}

	/**
	 * Undertakes the next {@link ManagedFunction} test.
	 * 
	 * @param parameter
	 *            Parameter.
	 */
	public void doInvokeNextFunctionTest(String parameter) throws Exception {
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.setNextFunction("next");
		this.constructFunction(work, "next").buildParameter();
		this.invokeFunction("task", parameter);
		assertTrue("Next function should be invoked", work.isNextInvoked);
		assertEquals("Incorrect next parameter", parameter, work.nextParameter);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		public boolean isNextInvoked = false;

		public String nextParameter = null;

		public String task(String parameter) {
			return parameter;
		}

		public void next(String parameter) {
			this.isNextInvoked = true;
			this.nextParameter = parameter;
		}
	}

}
