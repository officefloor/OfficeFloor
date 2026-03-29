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
public class AdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure undertakes {@link Administration} before the
	 * {@link ManagedFunction}.
	 */
	public void testPreAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.preAdminister("preTask");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "preTask", "task", "complete");
	}

	/**
	 * Ensure undertakes {@link Administration} after the
	 * {@link ManagedFunction}.
	 */
	public void testPostAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
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
	 * Ensure undertakes {@link Administration} before and after the
	 * {@link ManagedFunction}.
	 */
	public void testPreAndPostAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.preAdminister("preTask");
		task.postAdminister("postTask");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "preTask", "task", "postTask", "complete");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void trigger() {
		}

		public void preTask(Object[] extensions) {
		}

		public void task() {
		}

		public void postTask(Object[] extensions) {
		}

		public void complete() {
		}
	}

}
