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

import java.util.Properties;

import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamFactory;

/**
 * Factory for the {@link net.officefloor.frame.spi.team.impl.OnePersonTeam}.
 * 
 * @author Daniel
 */
public class OnePersonTeamFactory implements TeamFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.TeamFactory#createTeam(java.util.Properties)
	 */
	public Team createTeam(Properties properties) throws Exception {

		// Obtain the wait time
		long waitTime;
		String waitTimeText = properties.getProperty("wait");
		if ((waitTimeText == null) || (waitTimeText.trim().length() == 0)) {
			// Default
			waitTime = 100;
		} else {
			// Specify from properties
			waitTime = Long.parseLong(waitTimeText);
		}

		// Return the one person team
		return new OnePersonTeam(waitTime);
	}
}
