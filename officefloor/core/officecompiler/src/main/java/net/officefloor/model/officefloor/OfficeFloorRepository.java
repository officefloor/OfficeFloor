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
package net.officefloor.model.officefloor;

import net.officefloor.model.repository.ConfigurationItem;

/**
 * Repository of the {@link OfficeFloorModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorRepository {

	/**
	 * Retrieves the {@link OfficeFloorModel} from the {@link ConfigurationItem}
	 * .
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem} containing the
	 *            {@link OfficeFloorModel}.
	 * @return {@link OfficeFloorModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link OfficeFloorModel}.
	 */
	OfficeFloorModel retrieveOfficeFloor(ConfigurationItem configuration)
			throws Exception;

	/**
	 * Stores the {@link OfficeFloorModel} into the {@link ConfigurationItem}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloorModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} to contain the
	 *            {@link OfficeFloorModel}.
	 * @throws Exception
	 *             If fails to store the {@link OfficeFloorModel}.
	 */
	void storeOfficeFloor(OfficeFloorModel officeFloor,
			ConfigurationItem configuration) throws Exception;

}