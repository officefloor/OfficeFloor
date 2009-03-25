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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests running the same {@link Task} many times.
 * 
 * @author Daniel
 */
public class RepeatSingleTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress repeating a {@link Task}.
	 */
	@StressTest
	public void testStressRepeat() throws Exception {

		int REPEAT_COUNT = 1000000;
		int MAX_RUN_TIME = 10;
		this.setVerbose(true);

		// Create the repeat
		RepeatTask repeat = new RepeatTask(REPEAT_COUNT);

		// Register the repeat task
		this.constructWork(repeat, "work", "repeat")
				.buildTask("repeat", "TEAM").buildTaskContext();
		this.constructTeam("TEAM", new OnePersonTeam(100));

		// Run the repeats
		this.invokeWork("work", null, MAX_RUN_TIME);

		// Ensure correct number of repeats
		assertEquals("Incorrect number of repeats", REPEAT_COUNT, repeat
				.getRepeatCount());
	}

	/**
	 * {@link Work}.
	 */
	public class RepeatTask {

		/**
		 * Number of times to repeat.
		 */
		private final int maxRepeatCalls;

		/**
		 * Number of times repeat called.
		 */
		private int repeatCount;

		/**
		 * Initiate.
		 * 
		 * @param maxRepeatCalls
		 *            Number of times to repeat.
		 */
		public RepeatTask(int maxRepeatCalls) {
			this.maxRepeatCalls = maxRepeatCalls;
		}

		/**
		 * Obtains the number of times repeated.
		 */
		public synchronized int getRepeatCount() {
			return this.repeatCount;
		}

		/**
		 * Repeating task.
		 * 
		 * @param context
		 *            {@link TaskContext}.
		 */
		public synchronized void repeat(TaskContext<?, ?, ?> context) {

			// Determine if repeated enough
			if (this.repeatCount >= this.maxRepeatCalls) {
				return; // repeated enough
			}

			// Set up to repeat again
			this.repeatCount++;
			context.setComplete(false);

			// Output heap sizes after garbage collection
			if ((this.repeatCount % 100000) == 0) {
				RepeatSingleTaskStressTest.this.printHeapMemoryDiagnostics();
			}
		}
	}

}