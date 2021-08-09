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

import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link Executor} runs on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousExecutorAdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Executor} runs on another {@link Thread}.
	 */
	public void testExecutorOnAnotherThread() throws Exception {

		// Capture the current invoking thread
		Thread currentThread = Thread.currentThread();

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.preAdminister("preTask").buildAdministrationContext();

		// Run the function
		this.invokeFunction("function", null);

		// Should complete servicing
		assertTrue("Should complete servicing", work.isComplete);

		// Executor thread should be different
		assertNotEquals("Executor should be invoked by different thread", currentThread, work.executorThread);
	}

	public class TestWork {

		private volatile Thread executorThread = null;

		private volatile boolean isComplete = false;

		public void preTask(Object[] exections, AdministrationContext<?, ?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				this.executorThread = Thread.currentThread();
				flow.complete(null);
			});
		}

		public void function() {
			assertNotNull("Should have executor thread", this.executorThread);
			this.isComplete = true;
		}
	}

}
