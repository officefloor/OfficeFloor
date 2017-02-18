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
	 * Ensure break the {@link ThreadState} chain.
	 */
	public void testBreakThreadStateChain() throws Exception {
		
		// FIXME 
		fail("TODO fix up test to pass");
		
		final int SPAWN_COUNT = 2000;

		// Construct the function
		TestWork work = new TestWork(SPAWN_COUNT);
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.buildParameter();
		spawn.buildFlow("spawn", Integer.class, true);

		// Invoke the recurse thread state chain
		SafeCompleteFlowCallback complete = new SafeCompleteFlowCallback();
		this.triggerFunction("spawn", 1, complete);

		 // Should break chain, so return but not complete
		 complete.assertNotComplete();

		// Wait for completion
		complete.waitUntilComplete(100);

		// Ensure all thread states spawned
		assertEquals("Incorrect number of spawned thread states", SPAWN_COUNT, work.spawnCount.intValue());

		// Ensure more than one thread involved in execution
		int threadsUsedCount = work.threads.keySet().size();
		assertTrue("Ensure more than one thread used (as broken to another thread): " + threadsUsedCount + " used",
				threadsUsedCount > 1);
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
			if (spawnIteration.intValue() > this.maxSpawns) {
				return;
			}

			// Spawn again for another thread state depth
			spawn.doFlow(spawnIteration.intValue() + 1, null);
		}
	}

}