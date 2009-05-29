/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.internal.configuration;

import java.util.Properties;

import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

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
	 * Obtains the {@link Properties} to initialise the {@link TeamSource}.
	 * 
	 * @return {@link Properties} to initialise the {@link TeamSource}.
	 */
	Properties getProperties();
}
