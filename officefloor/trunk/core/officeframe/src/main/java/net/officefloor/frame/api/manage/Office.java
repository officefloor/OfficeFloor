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
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.execute.Work;

/**
 * Office within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Office {

	/**
	 * <p>
	 * Obtains the names of the {@link WorkManager} instances within this
	 * {@link Office}.
	 * <p>
	 * This allows to dynamically manage this {@link Office}.
	 * 
	 * @return Names of the {@link WorkManager} instances within this
	 *         {@link Office}.
	 */
	String[] getWorkNames();

	/**
	 * Obtains the {@link WorkManager} for the named {@link Work}.
	 * 
	 * @param name
	 *            Name of the {@link Work}.
	 * @return {@link WorkManager} for the named {@link Work}.
	 * @throws UnknownWorkException
	 *             If unknown {@link Work} name.
	 */
	WorkManager getWorkManager(String workName) throws UnknownWorkException;

}