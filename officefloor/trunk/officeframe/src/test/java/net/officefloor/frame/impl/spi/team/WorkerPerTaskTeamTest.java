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
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.util.OfficeFrameTestCase;

/**
 * Tests the {@link net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam}.
 * 
 * @author Daniel
 */
public class WorkerPerTaskTeamTest extends OfficeFrameTestCase {

	/**
	 * {@link WorkerPerTaskTeam} to test.
	 */
	protected WorkerPerTaskTeam team = new WorkerPerTaskTeam("test");

	/**
	 * Ensures runs the tasks.
	 */
	public void testRunning() {
		
		// Start processing
		this.team.startWorking();

		// Assign task and wait on it to be started for execution
		MockTaskContainer task = new MockTaskContainer();
		task.assignTaskToTeam(this.team, 10);

		// Allow some time for processing
		this.sleep(1);

		// Stop processing
		this.team.stopWorking();

		// Ensure task run at least twice
		assertTrue("Task must be run at least twice",
				(task.doTaskInvocationCount >= 2));

		// Indicate number of invocations
		this.printMessage("invocations=" + task.doTaskInvocationCount);
	}

}
