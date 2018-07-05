/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
public class SequentialTaskStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(SequentialTaskStressTest.class);
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