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
package net.officefloor.frame.impl.construct.team;

import junit.framework.TestCase;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;

/**
 * Adapter providing empty {@link TeamSource} methods.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class TeamSourceAdapter implements TeamSource, Team {

	/*
	 * ==================== TeamSource ==================================
	 */

	@Override
	public TeamSourceSpecification getSpecification() {
		return null;
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ==================== TeamSource ==================================
	 */

	@Override
	public void startWorking() {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void assignJob(Job job, TeamIdentifier assignerTeam) {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void stopWorking() {
		TestCase.fail("Should not be invoked");
	}

}