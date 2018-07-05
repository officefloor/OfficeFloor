/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Executes the {@link FunctionState} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionLoop {

	/**
	 * Executes the {@link FunctionState} within the current {@link Thread}.
	 * 
	 * @param function
	 *            {@link FunctionState} to execute.
	 */
	void executeFunction(FunctionState function);

	/**
	 * Delegates the {@link FunctionState} to the appropriate {@link Team} to
	 * execute.
	 * 
	 * @param function
	 *            {@link FunctionState} to delegate to a {@link Team}.
	 */
	void delegateFunction(FunctionState function);

}