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
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Avoid infinite loop on sequential function being invoked from a
 * {@link ManagedFunction} that is repeated.
 *
 * @author Daniel Sagenschneider
 */
public class UnsetSequentialFuntionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure avoid infinite loop on invoking both a parallel and sequential
	 * {@link ManagedFunction}.
	 */
	public void testUnsetSequentialFunction() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder repeat = this.constructFunction(work, "repeat");
		repeat.buildFlow("parallel", null, false);
		repeat.buildFlow("repeat", null, false);
		ReflectiveFunctionBuilder parallel = this.constructFunction(work, "parallel");
		parallel.buildParameter();
		parallel.buildFlow("parallel", null, false);

		// Ensure correct invocation
		this.invokeFunctionAndValidate("repeat", null, "repeat", "parallel", "parallel", "parallel", "repeat",
				"parallel", "parallel", "parallel");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isRepeated = false;

		public void repeat(ReflectiveFlow parallel, ReflectiveFlow repeat) {

			// Invoke the parallel flow
			parallel.doFlow(0, (escalation) -> {
			});

			// Invoke the repeat
			if (!this.isRepeated) {
				repeat.doFlow(null, null);
				this.isRepeated = true;
			}
		}

		public void parallel(int iteration, ReflectiveFlow flow) {
			if (iteration < 2) {
				flow.doFlow(iteration + 1, null);
			}
		}
	}

}
