/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure executes {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_AdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure undertakes {@link Administration} before the
	 * {@link ManagedFunction}.
	 */
	public void testPreAdministrationFailure_handledByFunctionEscalationHandler() throws Exception {

		fail("TODO implement");

		// Construct the functions
		TestWork work = new TestWork(new Exception("TEST"));
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "handle");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.preAdminister("preTask").getBuilder().addEscalation(Exception.class, "handle");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "preTask", "handle");
		assertSame("Incorrect handle exception", work.exception, work.handledException);
	}

	/**
	 * Ensure undertakes {@link Administration} after the
	 * {@link ManagedFunction}.
	 */
	public void testPostAdministration() throws Exception {

		fail("TODO implement");

		// Construct the functions
		TestWork work = new TestWork(new Exception("TEST"));
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.postAdminister("postTask");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "task", "postTask", "complete");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final Exception exception;

		private Exception handledException = null;

		public TestWork(Exception exception) {
			this.exception = exception;
		}

		public void trigger() {
		}

		public void preTask(Object[] extensions) throws Exception {
			throw this.exception;
		}

		public void task() {
		}

		public void postTask(Object[] extensions) throws Exception {
			throw this.exception;
		}

		public void complete() {
		}

		public void handle(Exception exception) {
			this.handledException = exception;
		}
	}

}