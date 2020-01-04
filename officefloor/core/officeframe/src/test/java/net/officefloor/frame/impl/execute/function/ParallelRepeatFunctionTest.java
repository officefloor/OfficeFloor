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
