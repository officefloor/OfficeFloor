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
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WorkerPerTaskTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerTaskTeamTest extends OfficeFrameTestCase {

	/**
	 * {@link WorkerPerTaskTeam} to test.
	 */
	private WorkerPerTaskTeam team = new WorkerPerTaskTeam("test",
			MockTeamSource.createTeamIdentifier());

	/**
	 * Ensures runs the tasks.
	 */
	public void testRunning() {

		// Start processing
		this.team.startWorking();

		// Assign task and wait on it to be started for execution
		MockTaskContainer task = new MockTaskContainer();
		task.assignJobToTeam(this.team, 10);

		// Flag tasks to stop working
		task.stopProcessing = true;

		// Stop processing
		this.team.stopWorking();
	}

}
