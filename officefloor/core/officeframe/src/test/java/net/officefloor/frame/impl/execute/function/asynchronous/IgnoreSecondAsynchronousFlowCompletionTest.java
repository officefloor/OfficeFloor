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
