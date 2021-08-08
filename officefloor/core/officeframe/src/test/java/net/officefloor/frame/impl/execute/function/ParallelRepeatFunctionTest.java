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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to repeat invoking same {@link ManagedFunction} in parallel.
 *
 * @author Daniel Sagenschneider
 */
public class ParallelRepeatFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure avoid infinite loop on repeating same {@link ManagedFunction} with
	 * {@link FlowCallback}.
	 */
	public void testUnsetSequentialFunction() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder repeat = this.constructFunction(work, "repeat");
		repeat.buildFlow("repeat", null, false);

		// Ensure correct invocation
		this.invokeFunctionAndValidate("repeat", null, "repeat", "repeat");
		assertTrue("Should be called back", work.isCalledBack);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isRepeated = false;

		private boolean isCalledBack = false;

		public void repeat(ReflectiveFlow repeat) {

			// Invoke the repeat
			if (!this.isRepeated) {
				repeat.doFlow(null, (escalation) -> {
					assertFalse("Should only be called back once", this.isCalledBack);
					this.isCalledBack = true;
				});
				this.isRepeated = true;
			}
		}
	}

}
