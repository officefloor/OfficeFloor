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
package net.officefloor.frame.impl.execute.function.asynchronous;

import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link Executor} runs on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousExecutorTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Executor} runs on another {@link Thread}.
	 */
	public void testExecutorOnAnotherThread() throws Exception {

		// Capture the current invoking thread
		Thread currentThread = Thread.currentThread();

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerExecutor");
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "servicingComplete");

		// Run the function
		this.invokeFunction("triggerExecutor", null);

		// Should complete servicing
		assertNotNull("Should complete servicing", work.completeThread);

		// Executor thread should be different
		assertNotEquals("Executor should be invoked by different thread", currentThread, work.executorThread);
		assertSame("As default Team passive, should continue with Executor thread", work.executorThread,
				work.completeThread);
	}

	public class TestWork {

		private volatile Thread executorThread = null;

		private volatile Thread completeThread = null;

		public void triggerExecutor(ManagedFunctionContext<?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				this.executorThread = Thread.currentThread();
				flow.complete(null);
			});
		}

		public void servicingComplete() {
			assertNotNull("Should have executor thread", this.executorThread);
			this.completeThread = Thread.currentThread();
		}
	}

}