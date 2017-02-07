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

import net.officefloor.frame.api.team.Job;

/**
 * Executes {@link Job} instances to enable access to the invoking
 * {@link ProcessState} {@link Thread} {@link ThreadLocal} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareExecutor {

	/**
	 * <p>
	 * Runs the {@link ProcessState} within context to enable the
	 * {@link ThreadLocal} instances of the current {@link Thread} to be
	 * available.
	 * <p>
	 * This will block the current {@link Thread} until the {@link ProcessState}
	 * and all subsequent {@link ProcessState} instances invoked by the current
	 * {@link Thread} are complete.
	 * 
	 * @param function
	 *            Initial {@link ManagedFunctionContainer} of the
	 *            {@link ProcessState}.
	 * @param loop
	 *            {@link FunctionLoop}.
	 */
	void runInContext(ManagedFunctionContainer function, FunctionLoop loop);

	/**
	 * Executes the {@link Job} by the {@link Thread} registered to its
	 * {@link ProcessState}.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	void execute(Job job);

	/**
	 * Flags the {@link ProcessState} as complete.
	 * 
	 * @param processState
	 *            {@link ProcessState}.
	 */
	void processComplete(ProcessState processState);

}