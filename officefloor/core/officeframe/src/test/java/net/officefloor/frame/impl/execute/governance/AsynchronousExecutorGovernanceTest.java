/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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