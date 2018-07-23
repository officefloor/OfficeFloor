/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Executive.
 * 
 * @author Daniel Sagenschneider
 */
public interface Executive {

	/**
	 * Creates a new {@link ProcessState} identifier.
	 * 
	 * @return New {@link ProcessState} identifier.
	 */
	Object createProcessIdentifier();

	/**
	 * Obtains the {@link ExecutionStrategy} strategies.
	 * 
	 * @return {@link ExecutionStrategy} instances.
	 */
	ExecutionStrategy[] getExcutionStrategies();

	/**
	 * <p>
	 * Creates the {@link Team}.
	 * <p>
	 * This is expected to delegate to the {@link TeamSource} to create the
	 * {@link Team}. However, the {@link Executive} may decide to wrap the
	 * {@link Team} or provide multiple {@link Team} instances with assigning
	 * algorithm (such as taking advantage of {@link Thread} affinity). The choice
	 * is, however, ultimately left to the {@link Executive} to manage the
	 * {@link Team} instances.
	 *
	 * @param teamSource {@link TeamSource} to create the {@link Team}.
	 * @param context    {@link TeamSourceContext} to configure the creation of the
	 *                   {@link Team}.
	 * @return {@link Team}.
	 * @throws Exception If fails to configure the {@link TeamSource}.
	 */
	Team createTeam(TeamSource teamSource, TeamSourceContext context) throws Exception;

}