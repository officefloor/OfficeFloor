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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Configuration of a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamConfiguration<TS extends TeamSource> {

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the {@link Class} of the {@link TeamSource}.
	 * 
	 * @return {@link Class} of the {@link TeamSource}.
	 */
	Class<TS> getTeamSourceClass();

	/**
	 * Obtains the {@link SourceProperties} for initialising the
	 * {@link TeamSource}.
	 * 
	 * @return {@link SourceProperties} for initialising the {@link TeamSource}.
	 */
	SourceProperties getProperties();

}
