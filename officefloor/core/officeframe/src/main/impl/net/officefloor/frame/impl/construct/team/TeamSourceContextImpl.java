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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.spi.team.source.ProcessContextListener;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;

/**
 * {@link TeamSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceContextImpl extends SourceContextImpl implements
		TeamSourceContext {

	/**
	 * Name of the {@link Team} to be created from the {@link TeamSource}.
	 */
	private final String teamName;

	/**
	 * {@link TeamIdentifier} of the {@link Team} to be created from the
	 * {@link TeamSource}.
	 */
	private final TeamIdentifier teamIdentifier;

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
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param teamName
	 *            Name of the {@link Team} to be created from the
	 *            {@link TeamSource}.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier} of the {@link Team} to be created from
	 *            the {@link TeamSource}.
	 * @param properties
	 *            {@link SourceProperties} to initialise the {@link TeamSource}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 */
	public TeamSourceContextImpl(boolean isLoadingType, String teamName,
			TeamIdentifier teamIdentifier, SourceProperties properties,
			SourceContext sourceContext) {
		super(isLoadingType, sourceContext, properties);
		this.teamName = teamName;
		this.teamIdentifier = teamIdentifier;
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
	public TeamIdentifier getTeamIdentifier() {
		return this.teamIdentifier;
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