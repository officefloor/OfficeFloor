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
package net.officefloor.model.desk;

import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Repository of {@link DeskModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface DeskRepository {

	/**
	 * Retrieves the {@link DeskModel} from the {@link ConfigurationItem}.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link DeskModel}.
	 * @return {@link DeskModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link DeskModel}.
	 */
	DeskModel retrieveDesk(ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link DeskModel} into the {@link ConfigurationItem}.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} to contain the {@link DeskModel}.
	 * @throws Exception
	 *             If fails to store the {@link DeskModel}.
	 */
	void storeDesk(DeskModel desk, ConfigurationItem configuration)
			throws Exception;

}