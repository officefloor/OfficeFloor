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
package net.officefloor.frame.spi.team;

/**
 * Team of workers to execute the assigned {@link Job} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface Team {

	/**
	 * Indicates for the {@link Team} to start working.
	 */
	void startWorking();

	/**
	 * Assigns a {@link Job} to be executed by this {@link Team}.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	void assignJob(Job job);

	/**
	 * <p>
	 * Indicates for the {@link Team} to stop working.
	 * <p>
	 * This method should block and only return control when the {@link Team}
	 * has stopped working and is no longer assigned {@link Job} instances to
	 * complete.
	 */
	void stopWorking();

}