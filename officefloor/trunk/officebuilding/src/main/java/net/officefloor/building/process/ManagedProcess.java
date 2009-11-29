/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process;

import java.io.Serializable;

/**
 * <p>
 * Provides hooks for starting/stopping and processing commands.
 * <p>
 * This object must be {@link Serializable} to allow its state to be sent to the
 * {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedProcess extends Serializable {

	/**
	 * <p>
	 * Runs the functionality of this managed process.
	 * <p>
	 * This should be a blocking call. Once this method returns the process is
	 * considered finished.
	 * 
	 * @param commandArguments
	 *            Command arguments for the managed process functionality.
	 * @throws Throwable
	 *             If fails to start.
	 */
	void run(ManagedProcessContext context) throws Throwable;

	/**
	 * <p>
	 * Does the particular command.
	 * <p>
	 * A new {@link Thread} is used for each invocation. This enables:
	 * <ol>
	 * <li>Commands to be executed in parallel</li>
	 * <li>Commands not need to wait for previous commands to complete</li>
	 * </ol>
	 * <p>
	 * This however requires the implementation of this method to be
	 * {@link Thread} safe.
	 * 
	 * @param command
	 *            Command known by the implementing {@link ManagedProcess}.
	 * @return Response from the command.
	 * @throws Throwable
	 *             If command fails.
	 */
	Object doCommand(Object command) throws Throwable;

}