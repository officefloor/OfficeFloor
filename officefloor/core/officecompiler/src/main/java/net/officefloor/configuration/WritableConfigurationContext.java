/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Writable {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WritableConfigurationContext extends ConfigurationContext {

	/**
	 * Obtains the {@link WritableConfigurationItem} at the location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to obtain.
	 * @return {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If can not obtain a {@link WritableConfigurationItem} at the
	 *             location.
	 */
	WritableConfigurationItem getWritableConfigurationItem(String location) throws IOException;

	/**
	 * Creates a new {@link WritableConfigurationItem} at the relative location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to create.
	 * @param configuration
	 *            Configuration for the {@link WritableConfigurationItem}.
	 * @return The created {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If fails to create the {@link WritableConfigurationItem}.
	 */
	WritableConfigurationItem createConfigurationItem(String location, InputStream configuration) throws IOException;

	/**
	 * Deletes the {@link WritableConfigurationItem} at the relative location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to delete.
	 * @throws IOException
	 *             If can not delete the {@link WritableConfigurationItem} at
	 *             the relative location.
	 */
	void deleteConfigurationItem(String location) throws IOException;

}