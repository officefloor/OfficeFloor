/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.execute.Work;

/**
 * Office floor where {@link Work} is done within {@link Office} instances.
 * 
 * @author Daniel
 */
public interface OfficeFloor {

	/**
	 * Opens the floor and starts all {@link Work}.
	 * 
	 * @throws Exception
	 *             If fails to open the floor.
	 */
	void openOfficeFloor() throws Exception;

	/**
	 * Closes the floor. This stops all {@link Work} within the {@link Office}
	 * instances and releases all resources.
	 */
	void closeOfficeFloor();

	/**
	 * Obtains the {@link Office} for the input office name.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return Specified {@link Office}.
	 */
	Office getOffice(String officeName);

}