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

package net.officefloor.frame.impl.execute.administration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.ReflectiveAdministrationBuilder;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure can use {@link AsynchronousFlow} for {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _timeout_AsynchronousAdministrationTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure undertakes {@link Administration} with {@link AsynchronousFlow}.
	 */
	@Test
	public void asynchronousAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.construct.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");

		// Construct the administration
		ReflectiveAdministrationBuilder admin = task.preAdminister("preTask");
		admin.buildAsynchronousFlow();
		admin.getBuilder().setAsynchronousFlowTimeout(10);

		// Trigger the flow
		Closure<Throwable> escalation = new Closure<Throwable>();
		this.construct.triggerFunction("trigger", null, (error) -> escalation.value = error);
		assertNull(escalation.value, "Should be no failure: " + escalation.value);
		assertFalse(work.isTaskInvoked, "Should not invoke task");

		// Time out administration
		this.construct.adjustCurrentTimeMillis(100);
		this.officeManager.getOfficeManager(0).runAssetChecks();

		// Ensure timed out
		assertTrue(escalation.value instanceof AsynchronousFlowTimedOutEscalation,
				"Should timeout asynchronous flow: " + escalation.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isTaskInvoked = false;

		public void trigger() {
			// testing
		}

		public void preTask(Object[] extensions, AsynchronousFlow flow) {
			// testing
		}

		public void task() {
			this.isTaskInvoked = true;
		}
	}

}
