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