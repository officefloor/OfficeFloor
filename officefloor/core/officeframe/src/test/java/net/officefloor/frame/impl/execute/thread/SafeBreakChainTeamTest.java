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

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * <p>
 * Ensures fails start if unsafe break chain team.
 * <p>
 * Break chain team is checked for safety by running {@link Job} and confirming
 * run on different {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class SafeBreakChainTeamTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure default {@link Team} is safe.
	 */
	public void testSafeDefaultTeam() throws Exception {
		this.constructOfficeFloor().openOfficeFloor();
	}

	/**
	 * Ensure provided safe {@link Team} is ok.
	 */
	public void testSafeBreakChainTeam() throws Exception {
		this.getOfficeFloorBuilder().setBreakChainTeam(ExecutorCachedTeamSource.class);
		this.constructOfficeFloor().openOfficeFloor();
	}

	/**
	 * Ensure provided unsafe {@link Team} fails startup.
	 */
	public void testUnsafeBreakChainTeam() throws Exception {

		// Should be safe to compile
		this.getOfficeFloorBuilder().setBreakChainTeam(PassiveTeamSource.class);
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// However, on startup should fail due to unsafe break chain team
		try {
			officeFloor.openOfficeFloor();
			fail("Should not successfully open");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Break chain Team is not safe.  The configured Team must not re-use the current Thread to run Jobs.",
					ex.getMessage());
		}
	}

}
