/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Context for executing a {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadContext {

	/**
	 * Executes the {@link FunctionState} returning the next
	 * {@link FunctionState} to execute.
	 * 
	 * @param function
	 *            {@link FunctionState} to be executed.
	 * @return Next {@link FunctionState} to be executed. May be
	 *         <code>null</code>.
	 * @throws Throwable
	 *             Possible failure in executing the {@link FunctionState}.
	 */
	FunctionState executeFunction(FunctionState function) throws Throwable;

	/**
	 * Creates the {@link FunctionState} to handle the {@link Escalation}.
	 * 
	 * @param function
	 *            {@link FunctionState} triggering the {@link Escalation} that
	 *            should now be responsible for handling the {@link Escalation}.
	 * @param escalation
	 *            {@link Escalation} to be handled.
	 * @return {@link FunctionState} to handle the {@link Escalation}.
	 */
	FunctionState handleEscalation(FunctionState function, Throwable escalation);
}