/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;

/**
 * {@link LinkedTeamConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkedTeamConfigurationImpl implements LinkedTeamConfiguration {

	/**
	 * {@link Team} name within the {@link Office}.
	 */
	private final String officeTeamName;

	/**
	 * {@link Team} name within the {@link OfficeFloor}.
	 */
	private final String officeFloorTeamName;

	/**
	 * Initiate.
	 * 
	 * @param officeTeamName
	 *            {@link Team} name within the {@link Office}.
	 * @param officeFloorTeamName
	 *            {@link Team} name within the {@link OfficeFloor}.
	 */
	public LinkedTeamConfigurationImpl(String officeTeamName,
			String officeFloorTeamName) {
		this.officeTeamName = officeTeamName;
		this.officeFloorTeamName = officeFloorTeamName;
	}

	/*
	 * ==================== LinkedTeamConfiguration ==========================
	 */

	@Override
	public String getOfficeTeamName() {
		return this.officeTeamName;
	}

	@Override
	public String getOfficeFloorTeamName() {
		return this.officeFloorTeamName;
	}

}