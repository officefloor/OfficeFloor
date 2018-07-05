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

import net.officefloor.compile.properties.PropertyList;

/**
 * Writable {@link ConfigurationItem}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WritableConfigurationItem extends ConfigurationItem {

	/**
	 * Obtains the {@link InputStream} to the raw configuration (no
	 * {@link PropertyList} replacement).
	 * 
	 * @return {@link InputStream} to the raw configuration.
	 * @throws IOException
	 *             If fails to load the raw configuration.
	 */
	InputStream getRawConfiguration() throws IOException;

	/**
	 * Specifies the configuration that this is to represent.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws IOException
	 *             If fails to set the configuration.
	 */
	void setConfiguration(InputStream configuration) throws IOException;

	/**
	 * Obtains the {@link WritableConfigurationContext} for this
	 * {@link WritableConfigurationItem}.
	 * 
	 * @return {@link WritableConfigurationContext} for this
	 *         {@link WritableConfigurationItem}.
	 */
	WritableConfigurationContext getContext();

}