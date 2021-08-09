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

package net.officefloor.frame.impl.execute.governance;

import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;

/**
 * Ensure {@link Executor} runs on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousExecutorGovernanceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Executor} runs on another {@link Thread}.
	 */
	public void testExecutorOnAnotherThread() throws Exception {

		// Capture the current invoking thread
		Thread currentThread = Thread.currentThread();

		// Construct the functions
		TestGovernance govern = new TestGovernance();
		TestWork work = new TestWork(govern);
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.getBuilder().addGovernance("GOVERNANCE");
		function.setNextFunction("complete");

		// Provide completion function (after governance enforced)
		this.constructFunction(work, "complete");

		// Provide governance
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce").buildGovernanceContext();

		// Run the function
		this.invokeFunction("function", null);

		// Should complete servicing
		assertTrue("Should complete servicing", work.isComplete);

		// Should trigger function on same thread
		assertSame("Should trigger function on same thread", currentThread, work.triggerThread);

		// Executor thread should be different
		assertNotEquals("Executor should be invoked by different thread", currentThread, govern.executorThread);
	}

	public class TestWork {

		private final TestGovernance governance;

		private volatile Thread triggerThread = null;

		private volatile boolean isComplete = false;

		public TestWork(TestGovernance governance) {
			this.governance = governance;
		}

		public void function() {
			this.triggerThread = Thread.currentThread();
		}

		public void complete() {
			assertNotNull("Should have executor thread", this.governance.executorThread);
			this.isComplete = true;
		}
	}

	public class TestGovernance {

		private volatile Thread executorThread = null;

		public void enforce(Object[] exections, GovernanceContext<?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				this.executorThread = Thread.currentThread();
				flow.complete(null);
			});
		}
	}

}
