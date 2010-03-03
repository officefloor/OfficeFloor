/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.internal.construct;

import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;

/**
 * Raw meta-data for a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawTeamMetaData {

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the {@link Team}.
	 * 
	 * @return {@link Team}.
	 */
	Team getTeam();

	/**
	 * Obtains the {@link ProcessContextListener} instances for the
	 * {@link TeamSource}.
	 * 
	 * @return {@link ProcessContextListener} instances for the
	 *         {@link TeamSource}.
	 */
	ProcessContextListener[] getProcessContextListeners();

}