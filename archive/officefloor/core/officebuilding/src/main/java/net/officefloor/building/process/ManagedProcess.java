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
package net.officefloor.building.process;

import java.io.Serializable;

/**
 * <p>
 * Provides hooks for managing the {@link Process}.
 * <p>
 * This object must be {@link Serializable} to allow its state to be sent to the
 * {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedProcess extends Serializable {

	/**
	 * <p>
	 * Initialises this {@link ManagedProcess}.
	 * <p>
	 * All MBean instances registered before this method returns will be made
	 * available to the {@link ProcessManager} before it is returned on starting
	 * the {@link Process}.
	 * 
	 * @param context
	 *            {@link ManagedProcessContext}.
	 * @throws Throwable
	 *             If fails to initialise.
	 */
	void init(ManagedProcessContext context) throws Throwable;

	/**
	 * <p>
	 * Runs the functionality.
	 * <p>
	 * This should be a blocking call. Once this method returns the process is
	 * considered finished.
	 * 
	 * @throws Throwable
	 *             If fails.
	 */
	void main() throws Throwable;

}