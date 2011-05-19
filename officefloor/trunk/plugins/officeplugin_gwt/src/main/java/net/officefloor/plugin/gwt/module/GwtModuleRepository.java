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
package net.officefloor.plugin.gwt.module;

import net.officefloor.model.repository.ConfigurationItem;

/**
 * Repository for GWT modules.
 * 
 * @author Daniel Sagenschneider
 */
public interface GwtModuleRepository {

	/**
	 * Retrieves the {@link GwtModuleModel} from the {@link ConfigurationItem}.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem} containing the
	 *            {@link GwtModuleModel} configuration.
	 * @return {@link GwtModuleModel} for the {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link GwtModuleModel}.
	 */
	GwtModuleModel retrieveGwtModule(ConfigurationItem configuration)
			throws Exception;

	/**
	 * Creates a new GWT Module.
	 * 
	 * @param module
	 *            {@link GwtModuleModel} for the new GWT Module.
	 * @param configuration
	 *            {@link ConfigurationItem} to receive the GWT Module
	 *            configuration.
	 * @throws Exception
	 *             If fails to create the GWT Module.
	 */
	void createGwtModule(GwtModuleModel module, ConfigurationItem configuration)
			throws Exception;

	/**
	 * Updates an existing GWT Module.
	 * 
	 * @param module
	 *            {@link GwtModuleModel} with details to update the existing GWT
	 *            Module.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the existing GWT Module
	 *            configuration and also used to update its configuration.
	 * @throws Exception
	 *             If fails to update the GWT Module.
	 */
	void updateGwtModule(GwtModuleModel module, ConfigurationItem configuration)
			throws Exception;

}