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
package net.officefloor.model.impl.repository.classloader;

import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ReadOnlyConfigurationException;

/**
 * {@link ConfigurationContext} for a {@link ClassLoader} class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderConfigurationContext implements ConfigurationContext {

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public ClassLoaderConfigurationContext(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/*
	 * ================== ConfigurationContext ===============================
	 */

	@Override
	public String getLocation() {
		return "CLASS LOADER";
	}

	@Override
	public ConfigurationItem getConfigurationItem(String location)
			throws Exception {

		// Obtain the resource on the class path
		InputStream resource = this.classLoader.getResourceAsStream(location);
		if (resource == null) {
			// Not found
			return null;
		}

		// Create the configuration item
		ConfigurationItem item = new ClassLoaderConfigurationItem(location,
				this.classLoader, this, resource);

		// Return the configuration item
		return item;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public ConfigurationItem createConfigurationItem(String id,
			InputStream configuration) throws ReadOnlyConfigurationException {
		throw new ReadOnlyConfigurationException(
				"Can not create items on the class path from a "
						+ this.getClass().getSimpleName());
	}

	@Override
	public void deleteConfigurationItem(String relativeLocation)
			throws Exception, ReadOnlyConfigurationException {
		throw new ReadOnlyConfigurationException(
				"Can not delete items on the class path from a "
						+ this.getClass().getSimpleName());
	}

}