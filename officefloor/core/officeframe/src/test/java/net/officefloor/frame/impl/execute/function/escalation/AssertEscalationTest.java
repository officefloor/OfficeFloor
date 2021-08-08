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

package net.officefloor.frame.impl.execute.function.escalation;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure {@link Escalation} from {@link Assert} propagated back to
 * {@link TestCase}.
 *
 * @author Daniel Sagenschneider
 */
public class AssertEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Assert} failure is propagated to {@link TestCase}.
	 */
	public void test_Assertion_PropagatedTo_TestCase() throws Exception {

		// Construct function
		TestWork work = new TestWork();
		this.constructFunction(work, "task");

		// Ensure propagate failure
		try {
			this.invokeFunction("task", null);
			fail("Should not be successful");
		} catch (AssertionError ex) {
			assertEquals("Incorrect assertion failure", "TEST FAILURE", ex.getMessage());
		}

	}

	public class TestWork {
		public void task() {
			fail("TEST FAILURE");
		}
	}

}
