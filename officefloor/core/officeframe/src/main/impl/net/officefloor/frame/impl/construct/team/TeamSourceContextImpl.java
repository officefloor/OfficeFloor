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

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link TeamSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceContextImpl extends SourceContextImpl implements TeamSourceContext {

	/**
	 * Name of the {@link Team} to be created from the {@link TeamSource}.
	 */
	private final String teamName;

	/**
	 * <p>
	 * Registered {@link ThreadLocalAwareTeam} instances.
	 * <p>
	 * <code>volatile</code> to ensure threading of {@link Team} sees the lock
	 * (null list).
	 */
	private volatile List<ThreadLocalAwareTeam> processContextListeners = new LinkedList<ThreadLocalAwareTeam>();

	/**
	 * Initialise.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param teamName
	 *            Name of the {@link Team} to be created from the
	 *            {@link TeamSource}.
	 * @param properties
	 *            {@link SourceProperties} to initialise the {@link TeamSource}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 */
	public TeamSourceContextImpl(boolean isLoadingType, String teamName, SourceProperties properties,
			SourceContext sourceContext) {
		super(isLoadingType, sourceContext, properties);
		this.teamName = teamName;
	}

	/**
	 * Locks from adding further {@link ThreadLocalAwareTeam} instances and
	 * returns the listing of the registered {@link ThreadLocalAwareTeam}
	 * instances.
	 * 
	 * @return Listing of the registered {@link ThreadLocalAwareTeam} instances.
	 */
	public ThreadLocalAwareTeam[] lockAndGetProcessContextListeners() {

		// Obtain the registered Process Context Listeners
		ThreadLocalAwareTeam[] registeredListeners = this.processContextListeners.toArray(new ThreadLocalAwareTeam[0]);

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

}