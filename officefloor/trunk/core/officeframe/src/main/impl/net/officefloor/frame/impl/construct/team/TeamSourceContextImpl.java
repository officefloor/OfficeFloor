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

package net.officefloor.frame.impl.construct.team;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;
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
	 * Name of the {@link Team} to be created from the {@link TeamSource}.
	 */
	private final String teamName;

	/**
	 * {@link Properties} to initialise the {@link TeamSource}.
	 */
	private final Properties properties;

	/**
	 * <p>
	 * Registered {@link ProcessContextListener} instances.
	 * <p>
	 * <code>volatile</code> to ensure threading of {@link Team} sees the lock
	 * (null list).
	 */
	private volatile List<ProcessContextListener> processContextListeners = new LinkedList<ProcessContextListener>();

	/**
	 * Initialise.
	 * 
	 * @param teamName
	 *            Name of the {@link Team} to be created from the
	 *            {@link TeamSource}.
	 * @param properties
	 *            {@link Properties} to initialise the {@link TeamSource}.
	 */
	public TeamSourceContextImpl(String teamName, Properties properties) {
		this.teamName = teamName;
		this.properties = properties;
	}

	/**
	 * Locks from adding further {@link ProcessContextListener} instances and
	 * returns the listing of the registered {@link ProcessContextListener}
	 * instances.
	 * 
	 * @return Listing of the registered {@link ProcessContextListener}
	 *         instances.
	 */
	public ProcessContextListener[] lockAndGetProcessContextListeners() {

		// Obtain the registered Process Context Listeners
		ProcessContextListener[] registeredListeners = this.processContextListeners
				.toArray(new ProcessContextListener[0]);

		// Lock by releasing list
		this.processContextListeners = null;

		// Return the registered listeners
		return registeredListeners;
	}

	/*
	 * ===================== TeamSourceContext =========================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

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

	@Override
	public void registerProcessContextListener(
			ProcessContextListener processContextListener) {

		// Ensure not locked
		if (this.processContextListeners == null) {
			throw new IllegalStateException("May only register "
					+ ProcessContextListener.class.getSimpleName()
					+ " instances during init (team " + this.teamName + ")");
		}

		// Register the listener
		this.processContextListeners.add(processContextListener);
	}

}