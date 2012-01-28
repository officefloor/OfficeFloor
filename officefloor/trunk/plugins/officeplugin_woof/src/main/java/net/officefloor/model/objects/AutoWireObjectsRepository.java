/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.service.ServicesModel;

/**
 * Repository for obtaining the objects model for auto-wiring into an
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireObjectsRepository {

	/**
	 * Retrieves the {@link ServicesModel} from the {@link ConfigurationItem}.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return {@link ServicesModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link ServicesModel}.
	 */
	ServicesModel retrieveServices(ConfigurationItem configuration)
			throws Exception;

	/**
	 * Stores the {@link ServicesModel} within the {@link ConfigurationItem}.
	 * 
	 * @param services
	 *            {@link ServicesModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link ServicesModel}.
	 */
	void storeServices(ServicesModel services, ConfigurationItem configuration)
			throws Exception;

}