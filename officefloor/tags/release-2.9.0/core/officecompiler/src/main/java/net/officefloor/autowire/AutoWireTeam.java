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
package net.officefloor.autowire;

import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Team for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireTeam extends AutoWireProperties {

	/**
	 * Obtains the name of the {@link OfficeFloorTeam}.
	 * 
	 * @return Name of the {@link OfficeFloorTeam}.
	 */
	String getTeamName();

	/**
	 * <p>
	 * Obtains the {@link TeamSource} class name.
	 * <p>
	 * May be an alias.
	 * 
	 * @return {@link TeamSource} class name.
	 */
	String getTeamSourceClassName();

	/**
	 * Obtains the {@link AutoWire} instances for this {@link AutoWireTeam}.
	 * 
	 * @return {@link AutoWire} instances for this {@link AutoWireTeam}.
	 */
	AutoWire[] getAutoWiring();

}