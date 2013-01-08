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
package net.officefloor.plugin.objects;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.model.objects.AutoWireObjectsModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Loads the {@link AutoWireObjectsModel} configuration to the
 * {@link AutoWireApplication}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireObjectsLoader {

	/**
	 * Loads the {@link AutoWireObjectsModel} configuration to the
	 * {@link AutoWireApplication}.
	 * 
	 * @param objectsConfiguration
	 *            {@link ConfigurationItem} containing the
	 *            {@link AutoWireObjectsModel} configuration.
	 * @param application
	 *            {@link AutoWireApplication}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadAutoWireObjectsConfiguration(
			ConfigurationItem objectsConfiguration,
			AutoWireApplication application) throws Exception;

}