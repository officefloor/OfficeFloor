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
package net.officefloor.model.objects;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Repository for obtaining the {@link AutoWireObjectsModel} for auto-wiring
 * into an {@link AutoWireApplication}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireObjectsRepository {

	/**
	 * Retrieves the {@link AutoWireObjectsModel} from the
	 * {@link ConfigurationItem}.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return {@link AutoWireObjectsModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link AutoWireObjectsModel}.
	 */
	AutoWireObjectsModel retrieveAutoWireObjects(ConfigurationItem configuration)
			throws Exception;

	/**
	 * Stores the {@link AutoWireObjectsModel} within the
	 * {@link ConfigurationItem}.
	 * 
	 * @param objects
	 *            {@link AutoWireObjectsModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link AutoWireObjectsModel}.
	 */
	void storeAutoWireObjects(AutoWireObjectsModel objects,
			ConfigurationItem configuration) throws Exception;

}