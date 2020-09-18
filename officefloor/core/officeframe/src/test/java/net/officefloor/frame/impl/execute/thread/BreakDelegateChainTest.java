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

package net.officefloor.frame.impl.execute.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.SafeCompleteFlowCallback;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure breaks the delegate chain.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class BreakDelegateChainTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure the logic works without break chain complexities.
	 */
	@Test
	public void chainWorksWithoutBreak() throws Exception {
		this.construct.getOfficeBuilder().setMaximumFunctionStateChainLength(1000);
		this.doBreakDelegateChainTest(50, false);
	}

	/**
	 * Ensure break the {@link ThreadState} chain.
	 */
	@Test
	public void delegateStateChain() throws Exception {
		this.construct.getOfficeBuilder().setMaximumFunctionStateChainLength(10);
		this.doBreakDelegateChainTest(20, true);
	}

	/**
	 * Ensure break the {@link ThreadState} chain multiple times.
	 */
	@Test
	public void delegateStateChainMultipleTimes() throws Exception {
		this.construct.getOfficeBuilder().setMaximumFunctionStateChainLength(10);
		this.doBreakDelegateChainTest(60, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param delegateCount Number of delegates to invoke.
	 * @param isBreak       Indicates if should break the chain.
	 */
	private void doBreakDelegateChainTest(int delegateCount, boolean isBreak) throws Exception {

		// Construct the function
		TestWork work = new TestWork(delegateCount);
		ReflectiveFunctionBuilder delegate = this.construct.constructFunction(work, "delegate");
		delegate.buildParameter();
		delegate.buildFlow("delegate", Integer.class, false);

		// Invoke the recurse thread state chain
		SafeCompleteFlowCallback complete = new SafeCompleteFlowCallback();
		this.construct.triggerFunction("delegate", 1, complete);
		Thread completionThread = complete.waitUntilComplete(10000);

		// Should break chain, so complete in another thread
		Thread thisThread = Thread.currentThread();
		if (isBreak) {
			assertNotSame(thisThread, completionThread, "Breaking so should complete with in another Thread");
		} else {
			assertSame(thisThread, completionThread, "Non-breaking so should complete on same Thread");
		}

		// Ensure all delegates invoked
		assertEquals(delegateCount, work.invocationCount.intValue(), "Incorrect number of invoked delegates");

		// Ensure more than one thread involved in execution
		int threadsUsedCount = work.threads.keySet().size();
		if (isBreak) {
			assertTrue(threadsUsedCount > 1, "Ensure more than one thread used (as broken to another thread) - "
					+ threadsUsedCount + " used: " + work.threads);
		} else {
			assertEquals(1, threadsUsedCount, "No break, so only the one thread used: " + threadsUsedCount + " used");
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final int maxInvocations;

		private final AtomicInteger invocationCount = new AtomicInteger(0);

		private final ConcurrentHashMap<String, String> threads = new ConcurrentHashMap<>();

		public TestWork(int maxInvocations) {
			this.maxInvocations = maxInvocations;
		}

		public void delegate(Integer iteration, ReflectiveFlow delegate) {

			// Ensure correct iteration
			assertEquals(this.invocationCount.incrementAndGet(), iteration.intValue(), "Incorrect iteration");

			// Capture the thread
			this.recordThread("delegate-" + iteration);

			// Determine if complete
			if (iteration.intValue() >= this.maxInvocations) {
				return;
			}

			// Delegate again for another delegate depth
			Closure<Boolean> isCallbackInvoked = new Closure<>(false);
			delegate.doFlow(iteration.intValue() + 1, (escalation) -> {
				assertFalse(isCallbackInvoked.value, "Callback should only be invoked once");
				isCallbackInvoked.value = true;
				this.recordThread("callback-" + iteration);
			});
		}

		private void recordThread(String description) {
			String threadName = Thread.currentThread().getName();
			if (this.threads.get(threadName) == null) {
				this.threads.put(threadName, description);
			}
		}
	}

}
