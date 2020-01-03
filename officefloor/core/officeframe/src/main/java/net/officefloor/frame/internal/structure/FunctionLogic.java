/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Encapsulates simple logic for a {@link FunctionState}.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionLogic {

	/**
	 * Obtains the responsible {@link TeamManagement} for this
	 * {@link FunctionLogic}.
	 * 
	 * @return {@link TeamManagement} responsible for this
	 *         {@link FunctionLogic}. May be <code>null</code> to use any
	 *         {@link Team}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null;
	}

	/**
	 * Indicates if the {@link FunctionLogic} requires {@link ThreadState}
	 * safety.
	 * 
	 * @return <code>true</code> should {@link FunctionLogic} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Executes the logic.
	 * 
	 * @param flow
	 *            {@link Flow} that contains this {@link FunctionLogic}.
	 * @return Optional {@link FunctionState} to execute next.
	 * @throws Throwable
	 *             If logic fails.
	 */
	FunctionState execute(Flow flow) throws Throwable;

}
