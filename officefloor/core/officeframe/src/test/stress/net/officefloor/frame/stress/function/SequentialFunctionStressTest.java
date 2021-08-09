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

package net.officefloor.frame.stress.function;

import junit.framework.TestSuite;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests invoking sequential {@link Flow} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class SequentialFunctionStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(SequentialFunctionStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 10000000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Register the sequential function
		TestWork sequential = new TestWork(context);
		ReflectiveFunctionBuilder functionOne = this.constructFunction(sequential, "sequentialOne");
		functionOne.buildParameter();
		functionOne.buildFlow("sequentialTwo", Integer.class, false);
		context.loadResponsibleTeam(functionOne.getBuilder());
		ReflectiveFunctionBuilder functionTwo = this.constructFunction(sequential, "sequentialTwo");
		functionTwo.buildParameter();
		functionTwo.buildFlow("sequentialOne", Integer.class, false);
		context.loadOtherTeam(functionTwo.getBuilder());

		// Run the repeats
		context.setInitialFunction("sequentialOne", 1);
	}

	/**
	 * Functionality for test.
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void sequentialOne(Integer iteration, ReflectiveFlow flow) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete(iteration)) {
				return;
			}

			// Invoke another sequential task
			flow.doFlow(iteration.intValue() + 1, null);
		}

		public void sequentialTwo(Integer iteration, ReflectiveFlow flow) {
			this.sequentialOne(iteration, flow);
		}
	}

}
