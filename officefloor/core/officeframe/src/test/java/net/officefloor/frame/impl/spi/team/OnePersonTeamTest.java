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

import net.officefloor.frame.impl.spi.team.OnePersonTeam.OnePerson;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OnePersonTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamTest extends OfficeFrameTestCase {

	/**
	 * {@link OnePersonTeam} to test.
	 */
	protected OnePersonTeam team = new OnePersonTeam("TEST", 100);

	/**
	 * Ensures runs the tasks.
	 */
	public void testRunning() {

		// Start processing
		this.team.startWorking();

		// Assign task and wait on it to be started for execution
		MockJob task = new MockJob();
		task.assignJobToTeam(this.team, 10);

		// Obtain the person
		OnePerson person = this.team.person;

		// Stop processing (should block until person stops working)
		this.team.stopWorking();

		// Ensure person stopped working
		assertTrue("Person should be stopped working", person.finished);

		// Should have execute the task at least once
		assertTrue("Should have execute the task at least once", task.doTaskInvocationCount >= 1);
	}

}