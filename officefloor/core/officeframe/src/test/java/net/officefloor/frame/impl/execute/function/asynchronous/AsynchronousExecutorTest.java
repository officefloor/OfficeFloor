/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.function.asynchronous;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure {@link Executor} runs on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class AsynchronousExecutorTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure {@link Executor} runs on another {@link Thread}.
	 */
	@Test
	public void executorOnAnotherThread() throws Exception {

		// Capture the current invoking thread
		Thread currentThread = Thread.currentThread();

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "triggerExecutor");
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("servicingComplete");
		this.construct.constructFunction(work, "servicingComplete");

		// Run the function
		this.construct.invokeFunction("triggerExecutor", null);

		// Should complete servicing
		assertTrue(work.isComplete, "Should complete servicing");

		// Executor thread should be different
		assertNotEquals(currentThread, work.executorThread, "Executor should be invoked by different thread");
	}

	public class TestWork {

		private volatile Thread executorThread = null;

		private volatile boolean isTriggerComplete = false;

		private volatile boolean isComplete = false;

		public void triggerExecutor(ManagedFunctionContext<?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				this.executorThread = Thread.currentThread();
				flow.complete(() -> {
					this.isTriggerComplete = true;
				});
			});
		}

		public void servicingComplete() {
			assertNotNull(this.executorThread, "Should have executor thread");
			assertTrue(this.isTriggerComplete, "Should trigger async flow complete");
			this.isComplete = true;
		}
	}

}
