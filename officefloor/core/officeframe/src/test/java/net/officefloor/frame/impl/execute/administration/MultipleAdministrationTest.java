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