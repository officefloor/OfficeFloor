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
 * {@link TeamFactory} for a {@link LeaderFollowerTeam}.
 * 
 * @author Daniel
 */
public class LeaderFollowerTeamFactory implements TeamFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TeamFactory#createTeam(java.util.Properties)
	 */
	@Override
	public Team createTeam(Properties properties) throws Exception {

		// Obtain the configuration
		String teamName = properties.getProperty("name",
				LeaderFollowerTeam.class.getSimpleName());
		int teamSize = Integer.parseInt(properties.getProperty("size", "10"));
		long waitTime = Long.parseLong(properties.getProperty("wait.time",
				"100"));

		// Create and return the team
		return new LeaderFollowerTeam(teamName, teamSize, waitTime);
	}
}
