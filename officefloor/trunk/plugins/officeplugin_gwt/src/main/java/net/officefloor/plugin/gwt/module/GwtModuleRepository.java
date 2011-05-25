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

import java.io.InputStream;

import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.repository.ConfigurationContext;
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
	 * Stores the GWT Module.
	 * 
	 * @param module
	 *            {@link GwtModuleModel} containing the details of the required
	 *            GWT Module.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param existingGwtModulePath
	 *            Existing GWT Module path.
	 * @throws Exception
	 *             If fails to store the GWT Module.
	 */
	void storeGwtModule(GwtModuleModel module, ConfigurationContext context,
			String existingGwtModulePath) throws Exception;

	/**
	 * Creates new GWT Module content.
	 * 
	 * @param module
	 *            {@link GwtModuleModel} for the new GWT Module.
	 * @return {@link InputStream} to the new GWT Module content.
	 * @throws Exception
	 *             If fails to create the GWT Module.
	 */
	InputStream createGwtModule(GwtModuleModel module) throws Exception;

	/**
	 * Updates existing GWT Module content.
	 * 
	 * @param module
	 *            {@link GwtModuleModel} with details to update the existing GWT
	 *            Module.
	 * @param existingContent
	 *            Existing GWT Module content.
	 * @return {@link InputStream} to the updated GWT Module content.
	 * @throws Exception
	 *             If fails to update the GWT Module.
	 */
	InputStream updateGwtModule(GwtModuleModel module,
			InputStream existingContent) throws Exception;

}