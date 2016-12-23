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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.team.Team;

/**
 * Node within the graph of {@link FunctionState} instances to execute.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionState {

	/**
	 * Enable {@link Promise} like functionality.
	 * 
	 * @param thenFunction
	 *            {@link FunctionState} to execute after this
	 *            {@link FunctionState} chain is complete.
	 * @return {@link FunctionState} to execute the current
	 *         {@link FunctionState} then the input {@link FunctionState}.
	 */
	default FunctionState then(FunctionState thenFunction) {
		return Promise.then(this, thenFunction);
	}

	/**
	 * <p>
	 * Obtains the {@link TeamManagement} responsible for this
	 * {@link FunctionState}.
	 * <p>
	 * By default, {@link FunctionState} may be executed by any
	 * {@link TeamManagement}.
	 * 
	 * @return {@link TeamManagement} responsible for this
	 *         {@link FunctionState}. May be <code>null</code> to indicate any
	 *         {@link Team} may execute the {@link FunctionState}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null;
	}

	/**
	 * Obtains the {@link ThreadState} for this {@link FunctionState}.
	 * 
	 * @return {@link ThreadState} for this {@link FunctionState}.
	 */
	@Deprecated // use getFlow and have flow have handleFailure(Throwable) with
				// execute() throws Throwable
	ThreadState getThreadState();

	/**
	 * Indicates if the {@link FunctionState} requires {@link ThreadState}
	 * safety.
	 * 
	 * @return <code>true</code> should {@link FunctionState} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Executes the {@link FunctionState}.
	 * 
	 * @return Next {@link FunctionState} to be executed. May be
	 *         <code>null</code> to indicate no further {@link FunctionState}
	 *         instances to execute.
	 */
	FunctionState execute();

}