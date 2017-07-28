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
package net.officefloor.compile.impl.team;

import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link OfficeFloorTeamSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTeamSourceTypeImpl implements OfficeFloorTeamSourceType {

	/**
	 * Name of {@link Team}.
	 */
	private final String name;

	/**
	 * Properties for the {@link Team}.
	 */
	private final OfficeFloorTeamSourcePropertyType[] properties;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of {@link Team}.
	 * @param properties
	 *            Properties for the {@link Team}.
	 */
	public OfficeFloorTeamSourceTypeImpl(String name,
			OfficeFloorTeamSourcePropertyType[] properties) {
		this.name = name;
		this.properties = properties;
	}

	/*
	 * ===================== OfficeFloorTeamSourceType ========================
	 */

	@Override
	public String getOfficeFloorTeamSourceName() {
		return this.name;
	}

	@Override
	public OfficeFloorTeamSourcePropertyType[] getOfficeFloorTeamSourcePropertyTypes() {
		return this.properties;
	}

}