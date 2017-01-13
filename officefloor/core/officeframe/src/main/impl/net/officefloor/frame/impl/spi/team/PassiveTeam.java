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

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * <p>
 * Passive {@link Team} which uses the invoking {@link Thread} to execute the
 * {@link Job}.
 * <p>
 * Note that using this team will block the invoking {@link Thread} until the
 * {@link Job} is complete.
 * 
 * @author Daniel Sagenschneider
 */
public class PassiveTeam implements Team {

	/*
	 * ==================== Team =====================================
	 */

	@Override
	public void startWorking() {
		// No workers as passive
	}

	@Override
	public void assignJob(Job job) {
		job.run();
	}

	@Override
	public void stopWorking() {
	}

}