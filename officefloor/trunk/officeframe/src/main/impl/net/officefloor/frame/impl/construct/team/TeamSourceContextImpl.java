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
package net.officefloor.frame.impl.construct.team;

import java.util.Properties;

import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceUnknownPropertyError;

/**
 * {@link TeamSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceContextImpl implements TeamSourceContext {

	/**
	 * {@link Properties} to initialise the {@link TeamSource}.
	 */
	private final Properties properties;

	/**
	 * Initialise.
	 * 
	 * @param properties
	 *            {@link Properties} to initialise the {@link TeamSource}.
	 */
	public TeamSourceContextImpl(Properties properties) {
		this.properties = properties;
	}

	/*
	 * ===================== TeamSourceContext =========================
	 */

	@Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public String getProperty(String name)
			throws TeamSourceUnknownPropertyError {

		// Ensure have value
		String value = this.properties.getProperty(name);
		if (value == null) {
			throw new TeamSourceUnknownPropertyError("Unknown property '"
					+ name + "'", name);
		}

		// Return the value
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		// Obtain the value
		String value = this.properties.getProperty(name);

		// Return value (or default if no value)
		return (value != null ? value : defaultValue);
	}

}