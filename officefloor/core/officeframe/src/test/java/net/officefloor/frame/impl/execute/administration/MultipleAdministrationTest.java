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
 * Ensure can provide multiple {@link Administration} on a
 * {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public class MultipleAdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure undertakes {@link Administration} before the
	 * {@link ManagedFunction}.
	 */
	public void testMultiplePreAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administrations
		task.preAdminister("preTaskOne");
		task.preAdminister("preTaskTwo");
		task.preAdminister("preTaskThree");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "preTaskOne", "preTaskTwo", "preTaskThree", "task",
				"complete");
	}

	/**
	 * Ensure undertakes {@link Administration} after the
	 * {@link ManagedFunction}.
	 */
	public void testMultiplePostAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.postAdminister("postTaskOne");
		task.postAdminister("postTaskTwo");
		task.postAdminister("postTaskThree");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "task", "postTaskOne", "postTaskTwo",
				"postTaskThree", "complete");
	}

	/**
	 * Ensure undertakes {@link Administration} before and after the
	 * {@link ManagedFunction}.
	 */
	public void testMultiplePreAndPostAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.preAdminister("preTaskOne");
		task.preAdminister("preTaskTwo");
		task.preAdminister("preTaskThree");
		task.postAdminister("postTaskOne");
		task.postAdminister("postTaskTwo");
		task.postAdminister("postTaskThree");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "preTaskOne", "preTaskTwo", "preTaskThree", "task",
				"postTaskOne", "postTaskTwo", "postTaskThree", "complete");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void trigger() {
		}

		public void preTaskOne(Object[] extensions) {
		}

		public void preTaskTwo(Object[] extensions) {
		}

		public void preTaskThree(Object[] extensions) {
		}

		public void task() {
		}

		public void postTaskOne(Object[] extensions) {
		}

		public void postTaskTwo(Object[] extensions) {
		}

		public void postTaskThree(Object[] extensions) {
		}

		public void complete() {
		}
	}

}
