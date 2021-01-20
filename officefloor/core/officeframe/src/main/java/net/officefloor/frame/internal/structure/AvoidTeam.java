/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
 * Provides details to avoid execution with a particular {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AvoidTeam {

	/**
	 * Obtains the {@link FunctionState} to continue execution avoiding the
	 * specified {@link Team}.
	 * 
	 * @return {@link FunctionState} to continue execution avoiding the specified
	 *         {@link Team}.
	 */
	FunctionState getFunctionState();

	/**
	 * Flags to stop avoiding the {@link Team}.
	 */
	void stopAvoidingTeam();

}
