/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.SafeCompleteFlowCallback;

/**
 * Ensure breaks the {@link ThreadState} recursive chain.
 *
 * @author Daniel Sagenschneider
 */
public class BreakThreadStateChainTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the logic works without break chain complexities.
	 */
	public void testChainWorksWithoutBreak() throws Exception {
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(1000);
		this.doBreakThreadStateChainTest(50, false);
	}

	/**
	 * Ensure break the {@link ThreadState} chain.
	 */
	public void testBreakThreadStateChain() throws Exception {
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(20);
		this.doBreakThreadStateChainTest(15, true);
	}

	/**
	 * Ensure break the {@link ThreadState} chain multiple times.
	 */
	public void testBreakThreadStateChainMultipleTimes() throws Exception {
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(20);
		this.doBreakThreadStateChainTest(60, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param spawnCount
	 *            Number of {@link ThreadState} instances to spawn.
	 * @param isBreak
	 *            Indicates if should break the chain.
	 */
	private void doBreakThreadStateChainTest(int spawnCount, boolean isBreak) throws Exception {

		// Construct the function
		TestWork work = new TestWork(spawnCount);
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.buildParameter();
		spawn.buildFlow("spawn", Integer.class, true);

		// Invoke the recurse thread state chain
		SafeCompleteFlowCallback complete = new SafeCompleteFlowCallback();
		this.triggerFunction("spawn", 1, complete);

		// Should break chain, so return but not complete
		if (isBreak) {
			complete.assertNotComplete();
		} else {
			complete.assertComplete();
		}

		// Wait for completion
		complete.waitUntilComplete(10000);

		// Ensure all thread states spawned
		assertEquals("Incorrect number of spawned thread states", spawnCount, work.spawnCount.intValue());

		// Ensure more than one thread involved in execution
		int threadsUsedCount = work.threads.keySet().size();
		if (isBreak) {
			assertTrue("Ensure more than one thread used (as broken to another thread): " + threadsUsedCount + " used",
					threadsUsedCount > 1);
		} else {
			assertEquals("No break, so only the one thread used: " + threadsUsedCount + " used", 1, threadsUsedCount);
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final int maxSpawns;

		private final AtomicInteger spawnCount = new AtomicInteger(0);

		private final ConcurrentHashMap<String, Integer> threads = new ConcurrentHashMap<>();

		public TestWork(int maxSpawns) {
			this.maxSpawns = maxSpawns;
		}

		public void spawn(Integer spawnIteration, ReflectiveFlow spawn) {

			// Ensure correct spawn iteration
			assertEquals("Incorrect spawn iteration", this.spawnCount.incrementAndGet(), spawnIteration.intValue());

			// Ensure include the thread invoking this
			String threadName = Thread.currentThread().getName();
			if (this.threads.get(threadName) == null) {
				this.threads.put(threadName, spawnIteration);
			}

			// Determine if complete
			if (spawnIteration.intValue() >= this.maxSpawns) {
				return;
			}

			// Spawn again for another thread state depth
			// (Note: no responsible team so current thread will recurse into
			// thread state to execute it)
			spawn.doFlow(spawnIteration.intValue() + 1, (escalation) -> {
			});
		}
	}

}