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
public class _fail_AdministrationInvokeFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure invoked {@link Flow} is completed before the
	 * {@link ManagedFunction} is invoked.
	 */
	public void testAdministrationFlowEscalation_handledByFlowCallback() throws Exception {

		// Build functions
		TestWork work = new TestWork(new Exception("TEST"));
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		this.constructFunction(work, "flow");

		// Build administration
		task.preAdminister("preTask").buildFlow("flow", null, false);

		// Test
		this.invokeFunctionAndValidate("task", false, "preTask", "flow", "task");
		assertSame("Incorrect handled exception", work.exception, work.handledException);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final Exception exception;

		private Throwable handledException = null;

		public TestWork(Exception exception) {
			this.exception = exception;
		}

		public void preTask(Object[] extensions, ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> this.handledException = escalation);
		}

		public void task() {
		}

		public void flow() throws Exception {
			throw this.exception;
		}
	}

}
