/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.stress.function;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests running the same {@link ManagedFunction} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class RepeatFunctionStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(RepeatFunctionStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct functions
		RepeatTask repeat = new RepeatTask(context);
		ReflectiveFunctionBuilder function = this.constructFunction(repeat, "repeat");
		context.loadResponsibleTeam(function.getBuilder());
		function.buildFlow("repeat", null, false);

		// Run the repeats
		context.setInitialFunction("repeat", null);
	}

	/**
	 * Test functionality.
	 */
	public class RepeatTask {

		private final StressContext context;

		public RepeatTask(StressContext context) {
			this.context = context;
		}

		public void repeat(ReflectiveFlow repeat) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Repeat
			repeat.doFlow(null, null);
		}
	}

}
