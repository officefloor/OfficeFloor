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
package net.officefloor.model.repository;

import java.io.IOException;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.Model;

/**
 * Repository to the {@link Model} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelRepository {

	/**
	 * Configures the {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be configured.
	 * @param configuration
	 *            {@link ConfigurationItem} containing configuration of the
	 *            {@link Model}.
	 * @throws IOException
	 *             If fails to configure the {@link Model}.
	 */
	void retrieve(Object model, ConfigurationItem configuration) throws IOException;

	/**
	 * Stores the {@link Model} within the {@link WritableConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be stored.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the {@link Model}.
	 * @throws IOException
	 *             If fails to store the {@link Model}.
	 */
	void store(Object model, WritableConfigurationItem configuration) throws IOException;

}