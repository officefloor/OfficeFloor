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