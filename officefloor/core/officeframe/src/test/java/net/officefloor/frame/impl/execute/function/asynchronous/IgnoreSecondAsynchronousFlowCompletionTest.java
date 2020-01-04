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

package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure ignores the second {@link AsynchronousFlow} completion.
 * 
 * @author Daniel Sagenschneider
 */
public class IgnoreSecondAsynchronousFlowCompletionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure ignores the second {@link AsynchronousFlow} completion.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the object
		TestObject object = new TestObject();
		this.constructManagedObject(object, "MO", this.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.buildObject("MO", ManagedObjectScope.THREAD);

		// Ensure only uses first completion
		this.invokeFunction("triggerAsynchronousFlow", null);
		assertEquals("Should only run the first completion", "first only", object.value);
	}

	public class TestObject {
		private String value;
	}

	public class TestWork {

		public void triggerAsynchronousFlow(AsynchronousFlow flow, TestObject object) {

			// Only first completion used
			flow.complete(() -> object.value = "first only");

			// Remaining completions are ignored
			for (int i = 2; i < 10; i++) {
				final int index = i;
				flow.complete(() -> object.value = "Not use " + index);
			}
		}
	}

}
