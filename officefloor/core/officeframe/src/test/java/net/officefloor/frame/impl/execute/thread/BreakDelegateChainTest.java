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
package net.officefloor.frame.impl.execute.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.SafeCompleteFlowCallback;

/**
 * Ensure breaks the delegate chain.
 *
 * @author Daniel Sagenschneider
 */
public class BreakDelegateChainTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the logic works without break chain complexities.
	 */
	public void testChainWorksWithoutBreak() throws Exception {
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(1000);
		this.doBreakDelegateChainTest(50, false);
	}

	/**
	 * Ensure break the {@link ThreadState} chain.
	 */
	public void testDelegateStateChain() throws Exception {
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(10);
		this.doBreakDelegateChainTest(20, true);
	}

	/**
	 * Ensure break the {@link ThreadState} chain multiple times.
	 */
	public void testDelegateStateChainMultipleTimes() throws Exception {
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(10);
		this.doBreakDelegateChainTest(60, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param delegateCount
	 *            Number of delegates to invoke.
	 * @param isBreak
	 *            Indicates if should break the chain.
	 */
	private void doBreakDelegateChainTest(int delegateCount, boolean isBreak) throws Exception {

		// Construct the function
		TestWork work = new TestWork(delegateCount);
		ReflectiveFunctionBuilder delegate = this.constructFunction(work, "delegate");
		delegate.buildParameter();
		delegate.buildFlow("delegate", Integer.class, false);

		// Invoke the recurse thread state chain
		SafeCompleteFlowCallback complete = new SafeCompleteFlowCallback();
		this.triggerFunction("delegate", 1, complete);

		// Should break chain, so return but not complete
		if (isBreak) {
			complete.assertNotComplete();
		} else {
			complete.assertComplete();
		}

		// Wait for completion
		complete.waitUntilComplete(10000);
		
		// Ensure all delegates invoked
		assertEquals("Incorrect number of invoked delegates", delegateCount, work.invocationCount.intValue());

		// Ensure more than one thread involved in execution
		int threadsUsedCount = work.threads.keySet().size();
		if (isBreak) {
			assertTrue("Ensure more than one thread used (as broken to another thread) - " + threadsUsedCount
					+ " used: " + work.threads, threadsUsedCount > 1);
		} else {
			assertEquals("No break, so only the one thread used: " + threadsUsedCount + " used", 1, threadsUsedCount);
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
			assertEquals("Incorrect iteration", this.invocationCount.incrementAndGet(), iteration.intValue());

			// Capture the thread
			this.recordThread("delegate-" + iteration);

			// Determine if complete
			if (iteration.intValue() >= this.maxInvocations) {
				return;
			}

			// Delegate again for another delegate depth
			Closure<Boolean> isCallbackInvoked = new Closure<>(false);
			delegate.doFlow(iteration.intValue() + 1, (escalation) -> {
				assertFalse("Callback should only be invoked once", isCallbackInvoked.value);
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