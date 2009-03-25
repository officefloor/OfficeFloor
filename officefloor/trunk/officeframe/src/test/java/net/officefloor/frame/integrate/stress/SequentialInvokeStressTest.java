/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.integrate.stress;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests invoking {@link FlowInstigationStrategyEnum#SEQUENTIAL} many times.
 * 
 * @author Daniel
 */
public class SequentialInvokeStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Number of sequential calls made.
	 */
	private volatile int sequentialCallCount = 0;

	/**
	 * Ensures no issues arising in stress sequentially invoking {@link Flow}
	 * instances.
	 */
	public void testStressSequentialCalls() throws Exception {

		int SEQUENTIAL_COUNT = 10000000;

		// Create the sequential
		SequentialInvokeTask sequential = new SequentialInvokeTask(
				SEQUENTIAL_COUNT);

		// Register the sequential task
		ReflectiveTaskBuilder task = this.constructWork(sequential, "work",
				"sequential").buildTask("sequential", "TEAM");
		task.buildParameter();
		task.buildFlow("sequential", FlowInstigationStrategyEnum.SEQUENTIAL,
				Integer.class);
		this.constructTeam("TEAM", new OnePersonTeam(100));

		// Run the repeats
		this.invokeWork("work", new Integer(0), 60);

		// Ensure is complete
		assertEquals("Did not complete all sequential calls", SEQUENTIAL_COUNT,
				this.sequentialCallCount);
	}

	/**
	 * {@link Work}.
	 */
	public class SequentialInvokeTask {

		/**
		 * Number of times to make a sequential call.
		 */
		private final int maxSequentialCalls;

		/**
		 * Initiate.
		 * 
		 * @param maxSequentialCalls
		 *            Number of times to make a sequential call.
		 */
		public SequentialInvokeTask(int maxSequentialCalls) {
			this.maxSequentialCalls = maxSequentialCalls;
		}

		/**
		 * Sequential invoke task.
		 * 
		 * @param callCount
		 *            Number of sequential calls so far.
		 * @param flow
		 *            {@link ReflectiveFlow} to invoke the sequential
		 *            {@link Flow}.
		 */
		public synchronized void sequential(Integer callCount,
				ReflectiveFlow flow) {

			// Indicate the number of sequential calls made
			SequentialInvokeStressTest.this.sequentialCallCount = callCount
					.intValue();

			// Output heap sizes after garbage collection
			if ((callCount.intValue() % 100000) == 0) {
				MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
				memoryBean.gc();
				memoryBean.gc();
				MemoryUsage heap = memoryBean.getHeapMemoryUsage();
				System.out.println("HEAP: "
						+ (heap.getUsed() / (double) heap.getMax())
						+ " of max " + heap.getMax());
			}

			// Determine if enough sequential calls
			if (callCount.intValue() >= this.maxSequentialCalls) {
				// enough sequential calls
				return;
			}

			// Increment the call count
			Integer nextCallCount = new Integer(callCount + 1);

			// Make the sequential call
			flow.doFlow(nextCallCount);
		}
	}

}