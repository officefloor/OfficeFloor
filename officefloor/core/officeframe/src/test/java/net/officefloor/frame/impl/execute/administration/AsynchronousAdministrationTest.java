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
package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure can use {@link AsynchronousFlow} for {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public class AsynchronousAdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure undertakes {@link Administration} with {@link AsynchronousFlow}.
	 */
	public void testAsynchronousAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.preAdminister("preTask").buildAsynchronousFlow();
		task.postAdminister("postTask").buildAsynchronousFlow();

		// Trigger the flow
		Closure<Throwable> escalation = new Closure<Throwable>();
		Closure<Boolean> isComplete = new Closure<Boolean>(false);
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		this.triggerFunction("trigger", null, (error) -> {
			escalation.value = error;
			isComplete.value = true;
		});
		assertNull("Should be no failure: " + escalation.value, escalation.value);
		assertFalse("Should not invoke task", work.isTaskInvoked);
		assertFalse("Should not be complete", isComplete.value);

		// Continue from pre-administration
		work.preFlow.complete(null);
		assertTrue("Should invoke task", work.isTaskInvoked);
		assertFalse("Should not invoke complete", work.isComplete);
		assertFalse("Should not be complete", isComplete.value);

		// Continue from post-administration
		work.postFlow.complete(null);
		assertTrue("Should invoke complete", work.isComplete);
		assertTrue("Should now be complete", isComplete.value);

		// Ensure undertakes administration before
		this.validateReflectiveMethodOrder("trigger", "preTask", "task", "postTask", "complete");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isTaskInvoked = false;

		private boolean isComplete = false;

		private AsynchronousFlow preFlow;

		private AsynchronousFlow postFlow;

		public void trigger() {
			// testing
		}

		public void preTask(Object[] extensions, AsynchronousFlow flow) {
			this.preFlow = flow;
		}

		public void task() {
			this.isTaskInvoked = true;
		}

		public void postTask(Object[] extensions, AsynchronousFlow flow) {
			this.postFlow = flow;
		}

		public void complete() {
			this.isComplete = true;
		}
	}

}