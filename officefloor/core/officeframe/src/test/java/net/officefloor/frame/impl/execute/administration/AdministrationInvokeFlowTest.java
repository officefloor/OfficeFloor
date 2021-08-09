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
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure a {@link Administration} can invoke a {@link Flow}.
 *
 * @author Daniel Sagenschneider
 */
public class AdministrationInvokeFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure invoked {@link Flow} is completed before the
	 * {@link ManagedFunction} is invoked.
	 */
	public void testAdministrationInvokeFlow() throws Exception {

		// Build functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.buildFlow("flow", null, false);
		this.constructFunction(work, "flow");

		// Build administration
		task.preAdminister("preTask").buildFlow("flow", null, false);

		// Test
		this.invokeFunctionAndValidate("task", false, "preTask", "flow", "task");
	}

	/**
	 * Ensure {@link Flow} for {@link Administration} is complete before
	 * {@link ManagedFunction} is invoked.
	 */
	public void testPreAdministrationCompletesFlowBeforeFunction() throws Exception {

		// Build functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.buildFlow("flow", null, false);
		this.constructFunction(work, "flow").setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Build administration
		task.preAdminister("preTask").buildFlow("flow", null, false);

		// Test
		this.invokeFunctionAndValidate("task", false, "preTask", "flow", "complete", "task");
	}

	/**
	 * Ensure {@link Flow} for {@link Administration} is complete before
	 * {@link ManagedFunction} invoked {@link Flow} is undertaken.
	 */
	public void testPostAdministrationCompletesBeforeInvokedFlow() throws Exception {

		// Build functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.buildFlow("next", null, false);
		this.constructFunction(work, "flow").setNextFunction("complete");
		this.constructFunction(work, "complete");
		this.constructFunction(work, "next");

		// Build administration
		task.postAdminister("postTask").buildFlow("flow", null, false);

		// Test
		this.invokeFunctionAndValidate("task", true, "task", "postTask", "flow", "complete", "next");
	}

	/**
	 * Ensure {@link Flow} for {@link Administration} is complete before next
	 * {@link ManagedFunction} is invoked.
	 */
	public void testPostAdministrationCompletesBeforeNextFunction() throws Exception {

		// Build functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.buildFlow("next", null, false);
		task.setNextFunction("next");
		this.constructFunction(work, "flow").setNextFunction("complete");
		this.constructFunction(work, "complete");
		this.constructFunction(work, "next");

		// Build administration
		task.postAdminister("postTask").buildFlow("flow", null, false);

		// Test
		this.invokeFunctionAndValidate("task", false, "task", "postTask", "flow", "complete", "next");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void preTask(Object[] extensions, ReflectiveFlow flow) {
			flow.doFlow(null, null);
		}

		public void task(Boolean isInvokeFlow, ReflectiveFlow flow) {
			if (isInvokeFlow) {
				flow.doFlow(null, null);
			}
		}

		public void postTask(Object[] extensions, ReflectiveFlow flow) {
			flow.doFlow(null, null);
		}

		public void flow() {
		}

		public void complete() {
		}

		public void next() {
		}
	}

}
