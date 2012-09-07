/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Tests the continuing of the current {@link Team} {@link Thread} when the same
 * {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public class ContinueTeamThreadJobContainerTest extends
		AbstractJobContainerTest {

	/**
	 * Ensure able to continue the current {@link Thread} for the next
	 * {@link Job} if same {@link Team}.
	 */
	public void testContinueTeamThread() {
		fail("TODO implement");
	}

}